package dd2480.ciserver;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Server {

	
    public static void listen(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/webhook", Server::handleWebhook);

        server.setExecutor(null);
        server.start();

        System.out.println("Server started.");
    }

    private static void handleWebhook(HttpExchange exchange) throws IOException {
        System.out.println("Received request");

        String response = "Hello from /webhook\n";

        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
