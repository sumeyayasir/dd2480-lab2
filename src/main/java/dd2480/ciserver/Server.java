package dd2480.ciserver;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * HTTP server that listens for GitHub webhook push events and triggers CI
 * builds. The server exposes a single endpoint {@code /webhook} that accepts
 * POST requests containing a GitHub push event JSON payload.
 */
public class Server {

    /** Private constructor to prevent instantiation. */
    private Server() {
    }

    /**
     * Starts the HTTP server on the given port and registers the webhook handler.
     *
     * @param port the port number to listen on.
     * @throws IOException if the server cannot bind to the port.
     */
    public static void listen(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/webhook", Server::handleWebhook);

        server.setExecutor(null);
        server.start();

        System.out.println("Server started on port " + port);
    }

    /**
     * Reads the full request body from an {@link HttpExchange} as a UTF-8 string.
     *
     * @param exchange the HTTP exchange containing the request body.
     * @return the request body as a string.
     * @throws IOException if reading the body fails.
     */
    static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Handles incoming webhook requests. Parses the GitHub push event payload,
     * starts a build in a background thread, and responds with 200 OK.
     *
     * @param exchange the HTTP exchange for the incoming request.
     * @throws IOException if reading the request or writing the response fails.
     */
    private static void handleWebhook(HttpExchange exchange) throws IOException {
        System.out.println("Received webhook request");

        // 1. Read and parse the GitHub push event payload
        String body = readRequestBody(exchange);
        WebhookPayload payload;
        try {
            payload = new WebhookPayload(body);
        } catch (Exception e) {
            String error = "Invalid payload: " + e.getMessage();
            System.err.println(error);
            exchange.sendResponseHeaders(400, error.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(error.getBytes());
            }
            return;
        }

        System.out.println("Branch: " + payload.getBranch()
                + " | Commit: " + payload.getCommitSHA()
                + " | Repo: " + payload.getCloneUrl());

        // 2. Start the build in a background thread
        BuildProcessor buildProcessor = new BuildProcessor();
        new Thread(() -> {
            // Attempt to create notifier (non-fatal if GITHUB_TOKEN is missing)
            GitHubStatusNotifier notifier = null;
            try {
                notifier = new GitHubStatusNotifier();
                notifier.notifyPending(payload.getRepoFullName(), payload.getCommitSHA());
            } catch (Exception e) {
                System.err.println("Warning: GitHub notification unavailable — " + e.getMessage());
            }

            try {
                var result = buildProcessor.runBuild(
                        payload.getCloneUrl(), payload.getBranch(), payload.getCommitSHA());
                System.out.println("Build finished — success: " + result.isCIResultSuccessful());

                if (result.getBuildLog() != null && !result.getBuildLog().isBlank()) {
                    System.out.println("--- Build Log ---");
                    System.out.println(result.getBuildLog());
                    System.out.println("--- End Build Log ---");
                }

                if (result.getErrorMessage() != null) {
                    System.err.println("Error: " + result.getErrorMessage());
                }

                // Notify GitHub with final status
                if (notifier != null) {
                    try {
                        notifier.notify(payload.getRepoFullName(), result);
                    } catch (Exception e) {
                        System.err.println("Warning: Failed to send final status — " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("CI pipeline error: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();

        // 3. Send acknowledgement back to GitHub
        String response = "Build started for " + payload.getBranch()
                + " @ " + payload.getCommitSHA();
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
