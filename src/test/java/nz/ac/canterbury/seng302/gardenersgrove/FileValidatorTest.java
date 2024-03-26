package nz.ac.canterbury.seng302.gardenersgrove;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import nz.ac.canterbury.seng302.gardenersgrove.validation.InputValidator.FileValidator;
import nz.ac.canterbury.seng302.gardenersgrove.validation.InputValidator.ValidationResult;

public class FileValidatorTest {

    private static MultipartFile validNameFile;
    private static MultipartFile invalidNameFile;
    private static MultipartFile validSizeFile;
    private static MultipartFile invalidSizeFile;

    @BeforeAll
    private static void setUp() {
        String name = "data";
        String contentType = "text/plain";

        String validFilename = "filename.png";
        String invalidFilename = "filename.txt";

        int validFileSize = 10000000;
        int invalidFileSize = 10000001;

        byte[] validData = new byte[validFileSize];
        byte[] invalidData = new byte[invalidFileSize];

        validNameFile = new MockMultipartFile(name, validFilename, contentType, validData);
        invalidNameFile = new MockMultipartFile(name, invalidFilename, contentType, validData);
        validSizeFile = new MockMultipartFile(name, validFilename, contentType, validData);
        invalidSizeFile = new MockMultipartFile(name, validFilename, contentType, invalidData);
    }

    @Test
    public void validNameFile_return_OK() {
        assertEquals(ValidationResult.OK, FileValidator.validateImage(validNameFile, 10));
    }

    @Test
    public void invalidNameFile_return_INVALID_FILE_TYPE() {
        assertEquals(ValidationResult.INVALID_FILE_TYPE, FileValidator.validateImage(invalidNameFile, 10));
    }

    @Test
    public void validDataFile_return_OK() {
        assertEquals(ValidationResult.OK, FileValidator.validateImage(validSizeFile, 10));
    }

    @Test
    public void invalidDataFile_return_INVALID_FILE_SIZE() {
        assertEquals(ValidationResult.INVALID_FILE_SIZE, FileValidator.validateImage(invalidSizeFile, 10));
    }
}
