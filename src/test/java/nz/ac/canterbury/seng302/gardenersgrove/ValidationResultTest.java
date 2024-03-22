package nz.ac.canterbury.seng302.gardenersgrove;

import nz.ac.canterbury.seng302.gardenersgrove.validation.OldValidationResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests the OldValidationResult class
 */
public class ValidationResultTest {

    /**
     * Tests that the error message is blank when the result is valid
     */
    @Test
    public void IsValid_GetErrorMessage_MessageBlank() {
        OldValidationResult result = new OldValidationResult(true, "This is a test");
        Assertions.assertTrue(result.getErrorMessage().equals(""));
    }

    /**
     * Tests that the error message is not blank when the result is invalid
     */
    @Test
    public void IsInvalid_GetErrorMessage_MessageNotBlank() {
        String errorMessage = "This is a test";
        OldValidationResult result = new OldValidationResult(false, errorMessage);
        Assertions.assertTrue(result.getErrorMessage().equals(errorMessage));
    }
}