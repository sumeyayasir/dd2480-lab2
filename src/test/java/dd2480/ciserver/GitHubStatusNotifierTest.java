package dd2480.ciserver;

import dd2480.ciserver.model.CIResultObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GitHubStatusNotifier}.
 *
 * <p>
 * Tests the mapping logic and description building without making
 * real HTTP requests to the GitHub API.
 * </p>
 */
public class GitHubStatusNotifierTest {

    /**
     * Verifies that a fully successful CI result maps to "success" state.
     */
    @Test
    public void testSuccessfulResultMapsToSuccess() {
        CIResultObject result = new CIResultObject("heylol123", "main");
        result.setBuildSuccessful(true);
        result.setTestsSuccessful(true);

        assertEquals("success", GitHubStatusNotifier.mapResultToState(result));
    }

    /**
     * Verifies that a build failure maps to "failure" state.
     */
    @Test
    public void testBuildFailureMapsToFailure() {
        CIResultObject result = new CIResultObject("heylol123", "main");
        result.setBuildSuccessful(false);
        result.setErrorMessage("Compilation failed");

        assertEquals("failure", GitHubStatusNotifier.mapResultToState(result));
    }

    /**
     * Verifies that a test failure maps to "failure" state.
     */
    @Test
    public void testTestFailureMapsToFailure() {
        CIResultObject result = new CIResultObject("heylol123", "main");
        result.setBuildSuccessful(true);
        result.setTestsSuccessful(false);
        result.setErrorMessage("Tests failed");

        assertEquals("failure", GitHubStatusNotifier.mapResultToState(result));
    }

    /**
     * Verifies that an exception during build maps to "error" state.
     */
    @Test
    public void testExceptionMapsToError() {
        CIResultObject result = new CIResultObject("heylol123", "main");
        result.setBuildSuccessful(false);
        result.setErrorMessage("Build exception: something went wrong");

        assertEquals("error", GitHubStatusNotifier.mapResultToState(result));
    }

    /**
     * Verifies the description for a fully successful result.
     */
    @Test
    public void testDescriptionForSuccess() {
        CIResultObject result = new CIResultObject("heylol123", "main");
        result.setBuildSuccessful(true);
        result.setTestsSuccessful(true);

        assertEquals("Build and tests passed", GitHubStatusNotifier.buildDescription(result));
    }

    /**
     * Verifies the description for a compilation failure.
     */
    @Test
    public void testDescriptionForCompileFailure() {
        CIResultObject result = new CIResultObject("heylol123", "main");
        result.setBuildSuccessful(false);

        assertEquals("Compilation failed", GitHubStatusNotifier.buildDescription(result));
    }

    /**
     * Verifies the description for a test failure.
     */
    @Test
    public void testDescriptionForTestFailure() {
        CIResultObject result = new CIResultObject("heylol123", "main");
        result.setBuildSuccessful(true);
        result.setTestsSuccessful(false);

        assertEquals("Tests failed", GitHubStatusNotifier.buildDescription(result));
    }

    /**
     * Verifies that missing GITHUB_TOKEN throws IllegalStateException.
     * Note: Only reliable if GITHUB_TOKEN is not set in the test environment.
     */
    @Test
    public void testMissingTokenThrowsException() {
        if (System.getenv("GITHUB_TOKEN") == null) {
            assertThrows(IllegalStateException.class, GitHubStatusNotifier::new);
        }
    }

    /**
     * Verifies that the constructor with an explicit token stores it correctly.
     */
    @Test
    public void testConstructorWithToken() {
        GitHubStatusNotifier notifier = new GitHubStatusNotifier("test-token");
        assertNotNull(notifier);
    }

    /**
     * Verifies that getTokenFromEnv returns a non empty string, assuming the token
     * is set in the environment.
     */
    /*@Test
    public void testGetTokenFromEnvReturnsToken() {
        String envToken = GitHubStatusNotifier.getTokenFromEnv();
        assertNotNull(envToken);
        assertFalse(envToken.isBlank());
    }*/

    /**
     * Verifies that the notify method exists and returns correct type.
     * 
     * @throws NoSuchMethodException if the notify method is not found.
     */
    @Test
    public void testNotifyMethodExists() throws NoSuchMethodException {
        var notifyMethod = GitHubStatusNotifier.class.getMethod("notify", String.class, CIResultObject.class);
        assertNotNull(notifyMethod);
        assertEquals(int.class, notifyMethod.getReturnType());
    }

    /**
     * Verifies that the notifyPending method exists and returns correct type.
     * 
     * @throws NoSuchMethodException if the notifyPending method is not found.
     */
    /*@Test
    public void testNotifyPendingMethodExists() throws NoSuchMethodException {
        var notifyPendingMethod = GitHubStatusNotifier.class.getMethod("notifyPending", int.class);
        assertNotNull(notifyPendingMethod);
        assertEquals(int.class, notifyPendingMethod.getReturnType());
    }*/
}
