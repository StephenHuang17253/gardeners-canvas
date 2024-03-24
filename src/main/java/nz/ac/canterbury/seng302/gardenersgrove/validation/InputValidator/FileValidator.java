package nz.ac.canterbury.seng302.gardenersgrove.validation.InputValidator;

import org.springframework.web.multipart.MultipartFile;

public class FileValidator {

    private ValidationResult validationResult;
    private boolean passState = true;
    MultipartFile file;

    /**
     * A private constructor for the input validator,
     * this is used to run the static methods for input validation
     * 
     * @param valueToTest the text undergoing validation
     */
    private FileValidator(MultipartFile file) {
        this.file = file;
        validationResult = ValidationResult.OK;
    }

    /**
     * returns this objects validation result
     * 
     * @return validationResult variable of object
     */
    private ValidationResult getResult() {
        return this.validationResult;
    }

    /**
     * Validates a file based on its size and type
     * 
     * @param file MultiPartFile to validate
     * @return ValidationResult
     */
    public static ValidationResult validateFile(MultipartFile file) {
        String[] validFileTypes = new String[] { "png", "jpg", "svg", "jpeg" };
        int maxSize = 10;
        return new FileValidator(file).fileTypeHelper(validFileTypes).fileSizeHelper(maxSize).getResult();
    }

    /**
     * Validates a file based on its type
     * 
     * @param validFileTypes valid file types as a list of strings
     * @return the calling object
     */
    private FileValidator fileTypeHelper(String[] validFileTypes) {
        // if this validators input has already failed once, this test wont be run
        if (!this.passState) {
            return this;
        }

        String filename = file.getOriginalFilename();

        if (filename == null || !filename.matches("^[^\\s]+\\.(" + String.join("|", validFileTypes) + ")$")) {
            this.validationResult = ValidationResult.INVALID_FILE_EXT;
            this.passState = false;
        } else {
            this.validationResult = ValidationResult.OK;
        }

        return this;

    }

    /**
     * Validates a file based on its size in MB
     * 
     * @param maxSize the maximum size of the file in MB
     * @return the calling object
     */
    private FileValidator fileSizeHelper(int maxSize) {
        // if this validators input has already failed once, this test wont be run
        if (!this.passState) {
            return this;
        }

        if (file.getSize() > maxSize * Math.pow(10, 6)) {
            this.validationResult = ValidationResult.INVALID_FILE_SIZE;
            this.passState = false;
        } else {
            this.validationResult = ValidationResult.OK;
        }

        return this;
    }

}
