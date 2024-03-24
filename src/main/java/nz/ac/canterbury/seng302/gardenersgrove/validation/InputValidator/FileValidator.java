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

    public static ValidationResult validateFile(MultipartFile file) {
        String[] allowedFileTypes = new String[] { "png", "jpg", "svg", "jpeg" };
        return new FileValidator(file).fileTypeHelper(allowedFileTypes).getResult();
    }

    private FileValidator fileTypeHelper(String[] allowedFileTypes) {
        // if this validators input has already failed once, this test wont be run
        if (!this.passState) {
            return this;
        }

        String filename = file.getOriginalFilename();

        String[] splitFilename = filename.split("\\.");

        // Check has filename and extension
        if (splitFilename.length < 2) {
            this.validationResult = ValidationResult.INVALID_FILE_EXT;
            this.passState = false;
            return this;
        }

        String fileType = splitFilename[splitFilename.length - 1].toLowerCase();

        boolean valid = false;

        for (String allowedFileType : allowedFileTypes) {
            if (allowedFileType.equals(fileType)) {
                valid = true;
                break;
            }
        }

        if (!valid) {
            this.validationResult = ValidationResult.INVALID_FILE_EXT;
            this.passState = false;
        } else {
            this.validationResult = ValidationResult.OK;
        }

        return this;

    }


    private FileValidator fileSizeHelper(int maxSize) {
        // if this validators input has already failed once, this test wont be run
        if (!this.passState) {
            return this;
        }

        if (file.getSize() > maxSize) {
            this.validationResult = ValidationResult.INVALID_FILE_SIZE;
            this.passState = false;
        } else {
            this.validationResult = ValidationResult.OK;
        }

        return this;
    }
}
