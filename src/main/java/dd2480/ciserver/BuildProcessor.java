package dd2480.ciserver;

import dd2480.ciserver.model.CIResultObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * Handles the CI build pipeline: clones a repository, compiles it, and
 * returns a {@link CIResultObject} containing the results.
 */
public class BuildProcessor {

    /** Constructs a new BuildProcessor instance. */
    public BuildProcessor() {
    }

    /**
     * Clones the repository at the given URL and branch into a temporary
     * directory, then runs {@code mvn compile} to check compilation.
     *
     * @param repoUrl   the HTTPS clone URL of the repository.
     * @param branch    the branch to check out.
     * @param commitSHA the SHA of the commit being built.
     * @return a {@link CIResultObject} populated with the build outcome.
     */
    public CIResultObject runBuild(String repoUrl, String branch, String commitSHA) {
        CIResultObject result = new CIResultObject(commitSHA, branch);

        try {
            // Create a temporary directory for cloning the repository
            Path tempDir = Files.createTempDirectory("ci-build-");
            File repoDir = tempDir.toFile();
            System.out.println("Building in: " + repoDir.getAbsolutePath());

            // Clone the repository
            int cloneExit = runProcess(repoDir, "git", "clone", "-b", branch, repoUrl, ".");
            if (cloneExit != 0) {
                result.setBuildSuccessful(false);
                result.setErrorMessage("Git clone failed with exit code: " + cloneExit);
                return result;
            }

            // Run mvn compile
            ProcessBuilder compilePb = new ProcessBuilder("mvn", "compile");
            compilePb.directory(repoDir);
            compilePb.redirectErrorStream(true);
            Process compileProcess = compilePb.start();

            String output = captureOutput(compileProcess);
            int compileExit = compileProcess.waitFor();

            result.appendBuildLog(output);

            if (compileExit == 0) {
                result.setBuildSuccessful(true);
                System.out.println("Build successful!");
            } else {
                result.setBuildSuccessful(false);
                result.setErrorMessage("Compilation failed:\n" + output);
                System.out.println("Build failed with exit code: " + compileExit);
                return result;
            }

            // Run mvn test
            ProcessBuilder testPb = new ProcessBuilder("mvn", "test");
            testPb.directory(repoDir);
            testPb.redirectErrorStream(true);
            Process testProcess = testPb.start();

            String testOutput = captureOutput(testProcess);
            int testExit = testProcess.waitFor();

            result.appendBuildLog(testOutput);

            if (testExit == 0) {
                result.setTestsSuccessful(true);
                System.out.println("Tests passed!");
            } else {
                result.setTestsSuccessful(false);
                result.setErrorMessage("Tests failed:\n" + testOutput);
                System.out.println("Tests failed with exit code: " + testExit);
            }

        } catch (Exception e) {
            result.setBuildSuccessful(false);
            result.setErrorMessage("Build exception: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Runs a process with the given command in the specified directory and
     * returns the exit code.
     *
     * @param workDir the working directory for the process.
     * @param command the command and arguments to execute.
     * @return the process exit code.
     * @throws Exception if the process cannot be started or is interrupted.
     */
    int runProcess(File workDir, String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        // Drain output to prevent blocking
        captureOutput(process);
        return process.waitFor();
    }

    /**
     * Reads and returns all output (stdout + stderr) from a running process.
     *
     * @param process the process to read from.
     * @return the combined output as a string.
     */
    static String captureOutput(Process process) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "Failed to capture output: " + e.getMessage();
        }
    }
}