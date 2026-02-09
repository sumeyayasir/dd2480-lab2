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

}
