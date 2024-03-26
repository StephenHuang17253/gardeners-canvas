package nz.ac.canterbury.seng302.gardenersgrove;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import nz.ac.canterbury.seng302.gardenersgrove.validation.fileValidation.FileType;
import nz.ac.canterbury.seng302.gardenersgrove.validation.fileValidation.FileValidator;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;

public class FileValidatorTest {

    private static MultipartFile validNameFile;
    private static MultipartFile invalidNameFile;
    private static MultipartFile validSizeFile;
    private static MultipartFile invalidSizeFile;
    private static int maxSize = 10;
    private static FileType fileType = FileType.IMAGES;

    @BeforeAll
    private static void setUp() {

        String validFilename = "filename.png";
        String invalidFilename = "filename.txt";

        int validFileSize = 10000000;
        int invalidFileSize = 10000001;

        byte[] validData = new byte[validFileSize];
        byte[] invalidData = new byte[invalidFileSize];

        validNameFile = new MockMultipartFile(validFilename, validData);
        invalidNameFile = new MockMultipartFile(invalidFilename, validData);
        validSizeFile = new MockMultipartFile(validFilename, validData);
        invalidSizeFile = new MockMultipartFile(validFilename, invalidData);
    }

    @Test
    public void validNameFile_return_OK() {
        assertEquals(ValidationResult.OK, FileValidator.validateImage(validNameFile, maxSize, fileType));
    }

    @Test
    public void invalidNameFile_return_INVALID_FILE_TYPE() {
        assertEquals(ValidationResult.INVALID_FILE_TYPE, FileValidator.validateImage(invalidNameFile, maxSize, fileType));
    }

    @Test
    public void validDataFile_return_OK() {
        assertEquals(ValidationResult.OK, FileValidator.validateImage(validSizeFile, maxSize, fileType));
    }

    @Test
    public void invalidDataFile_return_INVALID_FILE_SIZE() {
        assertEquals(ValidationResult.INVALID_FILE_SIZE, FileValidator.validateImage(invalidSizeFile, maxSize, fileType));
    }
}
