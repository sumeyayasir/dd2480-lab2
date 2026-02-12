package dd2480.ciserver;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordNotifier {

    public static void notify(String status, String branch, String message) {
        // 1. Try to get from Environment (Production way)
        String webhookUrl = System.getenv("DISCORD_WEBHOOK_URL");

        // 2. If null, try to get from System Properties (Test way)
        if (webhookUrl == null) {
            webhookUrl = System.getProperty("DISCORD_WEBHOOK_URL");
        }

        if (webhookUrl == null) {
            System.err.println("DISCORD_WEBHOOK_URL is missing!");
            return;
        }

        try {
            // Escape newlines and quotes to prevent invalid JSON
            String safeMessage = message.replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\\"");

            String jsonContent = String.format(
                    "{\"content\": \"**CI Build Update**\\n**Status:** %s\\n**Branch:** %s\\n**Message:** %s\"}",
                    status, branch, safeMessage
            );

            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000); // Add timeout so it doesn't hang
            connection.setReadTimeout(5000);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonContent.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int code = connection.getResponseCode();
            if (code >= 200 && code < 300) {
                System.out.println("Discord notification sent! Response: " + code);
            } else {
                System.err.println("Discord sent error code: " + code);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}