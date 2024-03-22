package nz.ac.canterbury.seng302.gardenersgrove.validation;

/**
 * A class to represent the result of a validation check
 * It contains a boolean to represent if the validation was successful
 * and a string to represent the error message if the validation was not
 * successful
 */
public class OldValidationResult {
    private boolean valid;
    private String errorMessage;

    /**
     * Constructor for a OldValidationResult
     * 
     * @param valid
     * @param errorMessage
     */
    public OldValidationResult(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessage = valid ? "" : errorMessage;
    }

    /**
     * Validation result outcome getter
     * 
     * @return true if the validation was successful
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Sets the validation to be successful, only used in special cases
     */
    public void setValid() {
        this.valid = true;
    }

    /**
     * Validation result error message getter
     * @return the error message if the validation was not successful
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
