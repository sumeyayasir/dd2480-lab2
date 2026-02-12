package dd2480.ciserver;

import org.json.JSONObject;

/**
 * Parses a GitHub push event webhook payload, extracting the repository URL,
 * branch name, and commit SHA which we need for triggering CI builds.
 *
 * <p>Expected JSON structure (subset):</p>
 * <pre>{@code
 * {
 *   "ref": "refs/heads/main",
 *   "after": "heylol123",
 *   "repository": {
 *     "clone_url": "https://github.com/owner/repo.git",
 *     "full_name": "owner/repo"
 *   }
 * }
 * }</pre>
 */
public class WebhookPayload {

    private final String cloneUrl;
    private final String branch;
    private final String commitSHA;
    private final String repoFullName;

    /**
     * Constructs a WebhookPayload by parsing a raw JSON string from a GitHub
     * push event.
     *
     * @param jsonBody the raw JSON body of the GitHub push webhook.
     * @throws org.json.JSONException if required fields are missing or the JSON
     *                                 is malformed.
     */
    public WebhookPayload(String jsonBody) {
        JSONObject json = new JSONObject(jsonBody);

        if (!json.has("ref") || !json.has("after")) 
        {
        throw new org.json.JSONException(
            "Invalid push event payload - missing required fields"
        );
        }
        JSONObject repo = json.getJSONObject("repository");
        this.cloneUrl = repo.getString("clone_url");
        this.repoFullName = repo.getString("full_name");
        this.branch = parseBranchFromRef(json.getString("ref"));
        this.commitSHA = json.getString("after");
        
    }

    /**
     * Extracts the short branch name from a full Git ref string.
     * For example, {@code "refs/heads/main"} becomes {@code "main"}.
     *
     * @param ref the full ref string (e.g. {@code "refs/heads/feature/foo"}).
     * @return the branch name portion after {@code "refs/heads/"}.
     */
    static String parseBranchFromRef(String ref) {
        String prefix = "refs/heads/";
        if (ref.startsWith(prefix)) {
            return ref.substring(prefix.length());
        }
        return ref;
    }

    /**
     * Returns the clone URL of the repository.
     *
     * @return the HTTPS clone URL.
     */
    public String getCloneUrl() {
        return cloneUrl;
    }

    /**
     * Returns the branch name where the push occurred.
     *
     * @return the short branch name (e.g. {@code "main"}).
     */
    public String getBranch() {
        return branch;
    }

    /**
     * Returns the SHA of the head commit in the push.
     *
     * @return the 40-character commit SHA.
     */
    public String getCommitSHA() {
        return commitSHA;
    }

    /**
     * Returns the full name of the repository (owner/repo).
     *
     * @return the full repository name (e.g. {@code "owner/repo"}).
     */
    public String getRepoFullName() {
        return repoFullName;
    }
}
