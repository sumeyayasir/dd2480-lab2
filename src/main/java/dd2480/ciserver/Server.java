package dd2480.ciserver;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import java.nio.file.Files;


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
        
        // Tell the server to listen to /builds 
        server.createContext("/builds", Server::handleHistory);

        server.setExecutor(null);
        server.start();

        System.out.println("Server started on port " + port);
    }
    /** SAVES the build result to a JSON-file. */
    private static void saveBuildResult(dd2480.ciserver.model.CIResultObject result) {
        try 
        {
            //1. Create a folder for build history if it doesn't exist
            java.io.File folder = new java.io.File("build_history");
            if (!folder.exists()) 
            {
                folder.mkdir();// Create folder if id dose not exist

            }
            
            //2. create a UNIQUE file name
            String filePath="build_history/build_"+ result.getCommitSHA() + "_" + System.currentTimeMillis() + ".json";

            //3. build the JSON.object
            org.json.JSONObject json = new org.json.JSONObject();// Create a JSON object to hold the build result
            json.put("commitSHA", result.getCommitSHA());
            json.put("branch", result.getBranchName());
            json.put("log", result.getBuildLog());
            json.put("date", new java.util.Date().toString());// Gives date and time of the build

            //4. Write to disk 
            java.nio.file.Files.writeString(java.nio.file.Path.of(filePath), json.toString(4));
            System.out.println("Build result saved to " + filePath);

        } catch (java.io.IOException e) 
        {
            System.err.println("Failed to save build result: " + e.getMessage());
        }
    }

    /** Handles requests to /builds endpoint, 
     * lists all saved build results in the build_history folder and returns them as a JSON array.
     */
    private static void handleHistory(HttpExchange exchange) throws IOException
    {
        //1. Path to the build history folder
        java.io.File folder = new java.io.File("build_history");

        //2. List all JSON files in the folder
        java.io.File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));

        //3. HTML response
        StringBuilder html = new StringBuilder("<html><body><h1>Build History</h1><ul>");

        String query = exchange.getRequestURI().getQuery();

        // IF a SPECIFIC file is requested
        if (query != null && query.startsWith("file=")) 
        {
            String fileName=query.split("file=")[1];//extract filename

            try
            {
                // Read the content of the file
                String content = java.nio.file.Files.readString(java.nio.file.Path.of("build_history/" + fileName));   
                //Link to go back to the history page
                html.append("<p><a href='/builds'>&larr; Back to History</a></p>"); 
                //Display the name of the file
                html.append("<h2>Build Details for ").append(fileName).append("</h2>");
                // Display the content of the file
                html.append("<pre style='background:#f4f4f4; padding:10px; border:1px solid #ccc;'>")
                .append(content)
                .append("</pre>");
            } catch (java.io.IOException e)
            {
                html.append("<p>Error reading file: ").append(e.getMessage()).append("</p>");
        }
        }
        // If no specific file is requested, list all build files
        else 
        {
            html.append("<p>Click on a build to see details:</p>");
            html.append("<ul style='list-style-type: none; padding: 0;'>"); // Start the list

            if (files != null && files.length > 0) 
            {
                // Sort files by last modified date (newest first)
                java.util.Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                
                // Loop through each file and create a unique link for each build
                for (java.io.File f : files) 
                {
                    html.append("<li style='margin-bottom: 10px; padding: 5px; background: #f9f9f9; border-radius: 4px;'>")
                        .append("<a href='/builds?file=").append(f.getName()).append("' style='text-decoration: none; color: #007bff; font-weight: bold;'>")
                        .append(f.getName())
                        .append("</a>")
                        .append("</li>");
                }
            } 
            else 
            {
                // If no files are found, display a friendly message
                html.append("<li style='color: #666;'>No build history found yet.</li>");
            }
            html.append("</ul>"); // Closing the list 
        }
        html.append("</body></html>");
            
        // Convert the HTML string to bytes
        byte[] responseBytes = html.toString().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }

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
        
        //1.Check if it's a ping event and respond accordingly
        String eventType = exchange.getRequestHeaders().getFirst("X-GitHub-Event"); 
        if ("ping".equals(eventType)) {
            System.out.println("Received ping from GitHub");
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
            return;
        }

        // 2. If its not a ping, Parse the request body into a WebhookPayload object
        System.out.println("Received webhook request");
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

        // 3. Start the build in a background thread
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
                saveBuildResult(result);// Save the result
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
