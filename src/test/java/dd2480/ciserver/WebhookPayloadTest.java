package dd2480.ciserver;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link WebhookPayload}.
 */
public class WebhookPayloadTest {

    /**
     * A minimal valid GitHub push event payload used across tests.
     */
    private static final String VALID_PAYLOAD = """
            {
              "ref": "refs/heads/main",
              "after": "heylol123",
              "repository": {
                "clone_url": "https://github.com/owner/repo.git",
                "full_name": "owner/repo"
              }
            }
            """;

    /**
     * Verifies that the clone URL is correctly extracted from the payload.
     */
    @Test
    public void testExtractsCloneUrl() {
        WebhookPayload payload = new WebhookPayload(VALID_PAYLOAD);
        assertEquals("https://github.com/owner/repo.git", payload.getCloneUrl());
    }

    /**
     * Verifies that the branch name is correctly extracted from the ref field.
     */
    @Test
    public void testExtractsBranchName() {
        WebhookPayload payload = new WebhookPayload(VALID_PAYLOAD);
        assertEquals("main", payload.getBranch());
    }

    /**
     * Verifies that the commit SHA is correctly extracted from the after field.
     */
    @Test
    public void testExtractsCommitSHA() {
        WebhookPayload payload = new WebhookPayload(VALID_PAYLOAD);
        assertEquals("heylol123", payload.getCommitSHA());
    }

    /**
     * Verifies that a branch with slashes is parsed correctly.
     */
    @Test
    public void testExtractsBranchWithSlashes() {
        String payload = """
                {
                  "ref": "refs/heads/feature/my-branch",
                  "after": "lol123",
                  "repository": {
                    "clone_url": "https://github.com/owner/repo.git",
                    "full_name": "owner/repo"
                  }
                }
                """;
        WebhookPayload wp = new WebhookPayload(payload);
        assertEquals("feature/my-branch", wp.getBranch());
    }

    /**
     * Verifies that the repo full name is correctly extracted from the payload.
     */
    @Test
    public void testExtractsRepoFullName() {
        WebhookPayload payload = new WebhookPayload(VALID_PAYLOAD);
        assertEquals("owner/repo", payload.getRepoFullName());
    }

    /**
     * Verifies that parseBranchFromRef strips the refs/heads/ prefix.
     */
    @Test
    public void testParseBranchFromRefStripsPrefix() {
        assertEquals("main", WebhookPayload.parseBranchFromRef("refs/heads/main"));
    }

    /**
     * Verifies that parseBranchFromRef returns the input unchanged if no prefix.
     */
    @Test
    public void testParseBranchFromRefNoPrefix() {
        assertEquals("main", WebhookPayload.parseBranchFromRef("main"));
    }

    /**
     * Verifies that an invalid JSON body throws an exception.
     */
    @Test
    public void testInvalidJsonThrowsException() {
        assertThrows(org.json.JSONException.class, () -> {
            new WebhookPayload("not valid json");
        });
    }

    /**
     * Verifies that a missing required field throws an exception.
     */
    @Test
    public void testMissingFieldThrowsException() {
        String incomplete = """
                {
                  "ref": "refs/heads/main"
                }
                """;
        assertThrows(org.json.JSONException.class, () -> {
            new WebhookPayload(incomplete);
        });
    }
}
