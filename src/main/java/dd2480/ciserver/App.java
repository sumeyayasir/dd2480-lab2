package dd2480.ciserver;

/**
 * Entry point for the CI server application. Starts the HTTP server
 * on port 8001 (group 1 convention: 8000 + group number) to listen
 * for GitHub webhook events.
 */
public class App {

    /** Private constructor to prevent instantiation. */
    private App() {
    }

    /**
     * Starts the CI server.
     *
     * @param args command-line arguments (not used).
     * @throws Exception if the server fails to start.
     */
    public static void main(String[] args) throws Exception {
		Server.listen(8001);
    }
}
