package dd2480.ciserver;

import dd2480.ciserver.model.CIResultObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;

/**
 * Sends commit status notifications to the GitHub Status API.
 *
 * <p>Uses the REST endpoint
 * {@code POST /repos/{owner}/{repo}/statuses/{sha}} to set the commit
 * status to {@code pending}, {@code success}, {@code failure}, or
 * {@code error}.</p>
 *
 * <p>Requires a GitHub personal access token with {@code repo:status}
 * scope, provided via the {@code GITHUB_TOKEN} environment variable.</p>
 *
 * @see <a href="https://docs.github.com/en/rest/commits/statuses">GitHub Commit Statuses API</a>
 */
public class GitHubStatusNotifier {

    private static final String GITHUB_API_BASE = "https://api.github.com";
    private static final String CONTEXT = "ci-server/dd2480";

    private final String token;

    /**
     * Constructs a notifier using the given GitHub token.
     *
     * @param token a GitHub personal access token with {@code repo:status} scope.
     */
    public GitHubStatusNotifier(String token) {
        this.token = token;
    }

    /**
     * Constructs a notifier using the {@code GITHUB_TOKEN} environment variable.
     *
     * @throws IllegalStateException if the environment variable is not set.
     */
    public GitHubStatusNotifier() {
        this.token = getTokenFromEnv();
    }

    /**
     * Reads the GitHub token from the {@code GITHUB_TOKEN} environment variable.
     *
     * @return the token string.
     * @throws IllegalStateException if the variable is not set or is empty.
     */
    static String getTokenFromEnv() {
        String token = System.getenv("GITHUB_TOKEN");
        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "GITHUB_TOKEN environment variable is not set. "
                            + "Set it to a GitHub personal access token with repo:status scope.");
        }
        return token;
    }

    /**
     * Sets the commit status on GitHub based on a {@link CIResultObject}.
     *
     * @param repoFullName the full repository name (e.g. {@code "owner/repo"}).
     * @param result       the CI result to report.
     * @return the HTTP response code from the GitHub API.
     * @throws IOException if the HTTP request fails.
     */
    public int notify(String repoFullName, CIResultObject result) throws IOException {
        String state = mapResultToState(result);
        String description = buildDescription(result);
        return sendStatus(repoFullName, result.getCommitSHA(), state, description);
    }

    /**
     * Sends a "pending" status to GitHub before the build starts.
     *
     * @param repoFullName the full repository name.
     * @param commitSHA    the commit SHA to set status on.
     * @return the HTTP response code from the GitHub API.
     * @throws IOException if the HTTP request fails.
     */
    public int notifyPending(String repoFullName, String commitSHA) throws IOException {
        return sendStatus(repoFullName, commitSHA, "pending", "CI build in progress...");
    }

    /**
     * Maps a {@link CIResultObject} to a GitHub commit status state string.
     *
     * @param result the CI result.
     * @return one of {@code "success"}, {@code "failure"}, or {@code "error"}.
     */
    static String mapResultToState(CIResultObject result) {
        if (result.isCIResultSuccessful()) {
            return "success";
        }
        if (result.getErrorMessage() != null
                && result.getErrorMessage().contains("exception")) {
            return "error";
        }
        return "failure";
    }

    /**
     * Builds a human-readable description for the commit status.
     *
     * @param result the CI result.
     * @return a short description string.
     */
    static String buildDescription(CIResultObject result) {
        if (result.isCIResultSuccessful()) {
            return "Build and tests passed";
        }
        if (!result.isBuildSuccessful()) {
            return "Compilation failed";
        }
        if (!result.isTestsSuccessful()) {
            return "Tests failed";
        }
        return "CI completed with issues";
    }

    /**
     * Sends an HTTP POST to the GitHub commit status API.
     *
     * @param repoFullName the full repository name (e.g. {@code "owner/repo"}).
     * @param commitSHA    the commit SHA to set status on.
     * @param state        the status state ({@code "pending"}, {@code "success"},
     *                     {@code "failure"}, or {@code "error"}).
     * @param description  a short description of the status.
     * @return the HTTP response code.
     * @throws IOException if the request fails.
     */
    int sendStatus(String repoFullName, String commitSHA, String state,
            String description) throws IOException {
        String url = GITHUB_API_BASE + "/repos/" + repoFullName + "/statuses/" + commitSHA;

        String jsonBody = String.format(
                "{\"state\":\"%s\",\"description\":\"%s\",\"context\":\"%s\"}",
                state, description, CONTEXT);

        HttpURLConnection conn = createConnection(url);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        int responseCode = conn.getResponseCode();
        System.out.println("GitHub status API response: " + responseCode
                + " for state: " + state + " on " + commitSHA);

        conn.disconnect();
        return responseCode;
    }

    /**
     * Creates an {@link HttpURLConnection} for the given URL. Extracted to
     * allow overriding in tests.
     *
     * @param url the URL string.
     * @return a new connection.
     * @throws IOException if the connection cannot be opened.
     */
    HttpURLConnection createConnection(String url) throws IOException {
        return (HttpURLConnection) URI.create(url).toURL().openConnection();
    }
}
