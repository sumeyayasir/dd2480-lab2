package dd2480.ciserver.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CIResultObject class.
 */
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
     * Unit test to check that the getCommitSHA method returns the correct commit
     * SHA.
     */
    @Test
    public void testGetCommitSHA() {
        CIResultObject mockResult = new CIResultObject("commitSHA", "branch name");
        assertEquals("commitSHA", mockResult.getCommitSHA());
    }

    /**
     * Unit test to check that the getBranchName method returns the correct branch
     * name.
     */
    @Test
    public void testGetBranchName() {
        CIResultObject mockResult = new CIResultObject("commitSHA", "branch name");
        assertEquals("branch name", mockResult.getBranchName());
    }

    /**
     * Unit test to check that the setBuildSuccessful method correctly updates the
     * build success status. Also confirms that isBuildSuccessful returns the
     * correct value.
     */
    @Test
    public void testSetAndIsBuildSuccessful() {
        CIResultObject mockResult = new CIResultObject("commitSHA", "branch name");
        mockResult.setBuildSuccessful(true);
        assertTrue(mockResult.isBuildSuccessful());
    }

    /**
     * Unit test to check that the setTestsSuccessful method correctly updates the
     * tests success status. Also confirms that isTestsSuccessful returns the
     * correct value.
     */
    @Test
    public void testSetAndIsTestsSuccessful() {
        CIResultObject mockResult = new CIResultObject("commitSHA", "branch name");
        mockResult.setTestsSuccessful(true);
        assertTrue(mockResult.isTestsSuccessful());
    }

    /**
     * Unit test to check that the setErrorMessage method correctly updates the
     * error message. Also confirms that getErrorMessage returns the correct value.
     */
    @Test
    public void testSetAndGetErrorMessage() {
        CIResultObject mockResult = new CIResultObject("commitSHA", "branch name");
        mockResult.setErrorMessage("Test error message");
        assertEquals("Test error message", mockResult.getErrorMessage());
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
     * Unit test to verify that setBuildLog replaces the existing log. Also confirms
     * that getBuildLog returns the correct value.
     */
    @Test
    public void testSetBuildLogOverwrites() {
        CIResultObject result = new CIResultObject("sha", "main");
        result.appendBuildLog("old");
        result.setBuildLog("new");
        assertEquals("new", result.getBuildLog());
    }

}
