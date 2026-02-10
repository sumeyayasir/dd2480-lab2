package dd2480.ciserver.model;

/**
 * This class is a data object that contains the results of a CI run. It
 * includes the commit SHA, branch name, build success status, tests success
 * status, and an error message.
 */
public class CIResultObject {

    private String commitSHA;
    private String branchName;
    private boolean buildSuccessful;
    private boolean testsSuccessful;
    private String errorMessage;
    private String buildLog;

    /**
     * Constructor for initializing the parameters of the result object.
     * 
     * @param currentCommitSHA  is the ID of the commit that is being run in the
     *                          CI.
     * @param currentBranchName is the name of the branch that the commit belongs
     *                          to.
     */
    public CIResultObject(String currentCommitSHA, String currentBranchName) {
        this.commitSHA = currentCommitSHA;
        this.branchName = currentBranchName;
        this.buildSuccessful = false;
        this.testsSuccessful = false;
        this.errorMessage = null;
        this.buildLog = "";
    }

    /**
     * Method to check if the CI is successfull.
     * 
     * @return true if both build and tests are successful, false otherwise.
     */
    public boolean isCIResultSuccessful() {
        if (buildSuccessful && testsSuccessful) {
            return true;
        }
        return false;
    }

    /**
     * Getter to retrieve the commit SHA.
     * 
     * @return the commit SHA as a string.
     */
    public String getCommitSHA() {
        return commitSHA;
    }

    /**
     * Getter to retrieve the branch name.
     * 
     * @return the branch name as a string.
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * Getter to retreive the build success status.
     * 
     * @return the boolean value of build success status.
     */
    public boolean isBuildSuccessful() {
        return buildSuccessful;
    }

    /**
     * Getter to retrieve the tests success status.
     * 
     * @return the boolean value of tests success status.
     */
    public boolean isTestsSuccessful() {
        return testsSuccessful;
    }

    /**
     * Getter to retrieve the error message.
     * 
     * @return error message as a string if CI fails, otherwise null.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Setter to update the buildSuccessful value.
     * 
     * @param buildSuccessful is true if build is successful, false otherwise.
     */
    public void setBuildSuccessful(boolean buildSuccessful) {
        this.buildSuccessful = buildSuccessful;
    }

    /**
     * Setter to update the testsSuccessful value.
     * 
     * @param testsSuccessful is true if tests are successful, false otherwise.
     */
    public void setTestsSuccessful(boolean testsSuccessful) {
        this.testsSuccessful = testsSuccessful;
    }

    /**
     * Setter to update the error message.
     * 
     * @param errorMessage contains information about what failed in the CI run.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Getter to retrieve the build log.
     * 
     * @return the full build log output as a string.
     */
    public String getBuildLog() {
        return buildLog;
    }

    /**
     * Setter to update the build log.
     * 
     * @param buildLog the full output from the build and test processes.
     */
    public void setBuildLog(String buildLog) {
        this.buildLog = buildLog;
    }

    /**
     * Appends additional output to the existing build log.
     * 
     * @param log the additional log output to append.
     */
    public void appendBuildLog(String log) {
        if (this.buildLog == null || this.buildLog.isEmpty()) {
            this.buildLog = log;
        } else {
            this.buildLog += "\n" + log;
        }
    }

}
