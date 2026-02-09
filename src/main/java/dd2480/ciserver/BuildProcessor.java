package dd2480.ciserver;
import java.io.File;//
import java.nio.file.Files;
import java.nio.file.Path;// 

public class BuildProcessor 
{
    //Create temporary directory for cloning the repository
    //Get the repository URL and branch name from the webhook payload
    //runs mvn compile
    public void runBuild(String repoUrl, String branch)
    {
        try {
        // Create a temporary directory for cloning the repository
        Path tempDir = Files.createTempDirectory("ci-build-");
        File repoDir = tempDir.toFile();
        System.out.println("Building in: " + repoDir.getAbsolutePath());

        // Clone the repository using JGit
        ProcessBuilder clonePb = new ProcessBuilder("git", "clone", "-b", branch, repoUrl, ".");
            clonePb.directory(repoDir);
            clonePb.start().waitFor();

        // Run mvn compile in the cloned repository
        ProcessBuilder processBuilder = new ProcessBuilder("mvn", "compile");
        processBuilder.directory(repoDir);
        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            System.out.println("Build successful!");
        } else {
            System.out.println("Build failed with exit code: " + exitCode);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }


    }
    
}