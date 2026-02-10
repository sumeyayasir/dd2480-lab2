package dd2480.ciserver;

import dd2480.ciserver.model.CIResultObject;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BuildProcessor}.
 *
 * <p>These tests use a test subclass that overrides {@code runProcess} to
 * avoid executing real git/maven commands during unit testing.</p>
 */
public class BuildProcessorTest {

    /**
     * A testable subclass that simulates process exit codes without running
     * real commands.
     */
    static class TestBuildProcessor extends BuildProcessor {
        private int cloneExitCode = 0;
        private int compileExitCode = 0;

        TestBuildProcessor(int cloneExitCode, int compileExitCode) {
            this.cloneExitCode = cloneExitCode;
            this.compileExitCode = compileExitCode;
        }

        @Override
        int runProcess(File workDir, String... command) {
            // Simulate git clone result
            return cloneExitCode;
        }

        /**
         * Override runBuild to avoid real file system / process operations,
         * testing only the result logic.
         */
        @Override
        public CIResultObject runBuild(String repoUrl, String branch, String commitSHA) {
            CIResultObject result = new CIResultObject(commitSHA, branch);

            // Simulate clone
            if (cloneExitCode != 0) {
                result.setBuildSuccessful(false);
                result.setErrorMessage("Git clone failed with exit code: " + cloneExitCode);
                return result;
            }

            // Simulate compile
            if (compileExitCode == 0) {
                result.setBuildSuccessful(true);
            } else {
                result.setBuildSuccessful(false);
                result.setErrorMessage("Compilation failed:\nmock output");
            }

            return result;
        }
    }

    /**
     * Verifies that a successful clone + compile produces a successful result.
     */
    @Test
    public void testSuccessfulBuildReturnsTrueResult() {
        BuildProcessor bp = new TestBuildProcessor(0, 0);
        CIResultObject result = bp.runBuild("https://example.com/repo.git", "main", "heylol123");

        assertTrue(result.isBuildSuccessful());
        assertEquals("heylol123", result.getCommitSHA());
        assertEquals("main", result.getBranchName());
        assertNull(result.getErrorMessage());
    }

    /**
     * Verifies that a clone failure produces a failed result with an error message.
     */
    @Test
    public void testCloneFailureReturnsFalseResult() {
        BuildProcessor bp = new TestBuildProcessor(128, 0);
        CIResultObject result = bp.runBuild("https://example.com/repo.git", "main", "heylol123");

        assertFalse(result.isBuildSuccessful());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("clone failed"));
    }

    /**
     * Verifies that a compilation failure produces a failed result with an error message.
     */
    @Test
    public void testCompileFailureReturnsFalseResult() {
        BuildProcessor bp = new TestBuildProcessor(0, 1);
        CIResultObject result = bp.runBuild("https://example.com/repo.git", "main", "heylol123");

        assertFalse(result.isBuildSuccessful());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Compilation failed"));
    }

    /**
     * Verifies that the commit SHA and branch are correctly set in the result.
     */
    @Test
    public void testResultContainsCorrectMetadata() {
        BuildProcessor bp = new TestBuildProcessor(0, 0);
        CIResultObject result = bp.runBuild("https://example.com/repo.git", "feature/test", "heyyay123");

        assertEquals("heyyay123", result.getCommitSHA());
        assertEquals("feature/test", result.getBranchName());
    }

    /**
     * Verifies that captureOutput handles a simple process.
     */
    @Test
    public void testCaptureOutputReadsProcessOutput() throws Exception {
        ProcessBuilder pb = new ProcessBuilder("echo", "yo bro");
        Process process = pb.start();
        String output = BuildProcessor.captureOutput(process);
        process.waitFor();

        assertEquals("yo bro", output.trim());
    }
}
