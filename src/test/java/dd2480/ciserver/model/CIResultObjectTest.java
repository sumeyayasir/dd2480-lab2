package dd2480.ciserver.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CIResultObjectTest {

    /**
     * Unit test to check that the constructor initializes the parameters correctly.
     */
    @Test
    public void testDefaultValues() {
        CIResultObject mockResult = new CIResultObject("commitSHA", "branch name");
        assertEquals("commitSHA", mockResult.getCommitSHA());
        assertEquals("branch name", mockResult.getBranchName());
        assertFalse(mockResult.isBuildSuccessful());
        assertFalse(mockResult.isTestsSuccessful());
        assertNull(mockResult.getErrorMessage());
        assertEquals("", mockResult.getBuildLog());
    }

    /**
     * Unit test to check that the isCIResultSuccessful method returns true only
     * when both build and tests are successful.
     */
    @Test
    public void testIsCIResultSuccessful() {
        CIResultObject mockResult = new CIResultObject("commitSHA", "branch name");
        mockResult.setBuildSuccessful(true);
        mockResult.setTestsSuccessful(true);
        assertTrue(mockResult.isCIResultSuccessful());
    }

    /**
     * Unit test to verify that appendBuildLog concatenates log entries.
     */
    @Test
    public void testAppendBuildLog() {
        CIResultObject result = new CIResultObject("sha", "main");
        result.appendBuildLog("compile output");
        result.appendBuildLog("test output");
        assertEquals("compile output\ntest output", result.getBuildLog());
    }

    /**
     * Unit test to verify that setBuildLog replaces the existing log.
     */
    @Test
    public void testSetBuildLogOverwrites() {
        CIResultObject result = new CIResultObject("sha", "main");
        result.appendBuildLog("old");
        result.setBuildLog("new");
        assertEquals("new", result.getBuildLog());
    }

}
