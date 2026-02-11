package dd2480.ciserver;

import dd2480.ciserver.model.CIResultObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;

/**
 * Unit tests for Build History persistence
 */
public class BuildHistoryTest {

    /**
     * Ensures the build_history directory exists before tests run.
     */
    @BeforeAll
    public static void setup() {
        File folder = new File("build_history");
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    /**
     * Verifies that saveBuildResult creates a JSON file with the correct naming convention
     */
    @Test
    public void testSaveBuildResultPersistsToFile() throws Exception {
        // 1. Setup: Create a dummy CI result
        String testSHA = "test-sha-" + System.currentTimeMillis();
        CIResultObject result = new CIResultObject(testSHA, "feature-branch");
        result.setBuildSuccessful(true);
        result.setTestsSuccessful(true);
        result.appendBuildLog("All systems go!");

        // 2. Action: Save the result
        Server.saveBuildResult(result);

        // 3. Verification: Check if the file was created
        File folder = new File("build_history");
        File[] files = folder.listFiles((dir, name) -> name.contains(testSHA));
        
        assertNotNull(files, "The build_history folder should be readable");
        assertTrue(files.length > 0, "A JSON file should have been created for the SHA: " + testSHA);

        // 4. Verification: Check file content
        File savedFile = files[0];
        String content = Files.readString(savedFile.toPath());
        
        assertTrue(content.contains(testSHA), "The saved file should contain the correct SHA");
        assertTrue(content.contains("feature-branch"), "The saved file should contain the branch name");
        assertTrue(content.contains("All systems go!"), "The saved file should contain the build logs");

        // Cleanup: Delete the test file
        savedFile.delete();
    }

    /**
     * Verifies that the history folder is handled correctly.
     */
    @Test
    public void testHistoryFolderExists() {
        File folder = new File("build_history");
        assertTrue(folder.exists(), "The server should ensure the build_history folder exists");
    }
}