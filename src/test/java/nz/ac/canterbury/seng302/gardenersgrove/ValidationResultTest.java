package nz.ac.canterbury.seng302.gardenersgrove;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;

/**
 * Tests the ValidationResult class
 */
public class ValidationResultTest {

    /**
     * Tests that the error message is blank when the result is valid
     */
    @Test
    public void IsValid_GetErrorMessage_MessageBlank() {
        ValidationResult result = new ValidationResult(true, "This is a test");
        Assertions.assertTrue(result.getErrorMessage().equals(""));
    }

    /**
     * Tests that the error message is not blank when the result is invalid
     */
    @Test
    public void IsInvalid_GetErrorMessage_MessageNotBlank() {
        String errorMessage = "This is a test";
        ValidationResult result = new ValidationResult(false, errorMessage);
        Assertions.assertTrue(result.getErrorMessage().equals(errorMessage));
    }
}