package nz.ac.canterbury.seng302.gardenersgrove.validation.fileValidation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;
/**
 * Tests MultipartFiles on a variety of rules to check if values are valid
 * Returns a type ValidationResult Enum which informs about if a field passes
 * and if not, why it failed
 */
public class FileValidator {


    Logger logger = LoggerFactory.getLogger(FileValidator.class);

    private ValidationResult validationResult;
    private boolean passState = true;
    MultipartFile file;

    /**
     * A private constructor for the file validator,
     * this is used to run the static methods for file validation
     * 
     * @param file the file undergoing validation
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
     * Validates a file based on its size given in MB and file type as a FileType enum value
     * 
     * @param file     MultiPartFile to validate
     * @param maxSize  Maximum size allowed for file in MB
     * @param fileType Enum value of FileType corresponding to the type of file to
     *                 validate as
     * @return ValidationResult
     */
    public static ValidationResult validateImage(MultipartFile file, int maxSize, FileType fileType) {

        return new FileValidator(file)
                .fileTypeHelper(fileType.getExtensions())
                .fileSizeHelper(maxSize)
                .getResult();
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
        
        String allFilenames = String.join("|", validFileTypes);

        if (filename == null || !filename.matches("^[^\\s]+\\.(" + allFilenames + "|" + allFilenames.toUpperCase() + ")$")) {
            this.validationResult = ValidationResult.INVALID_FILE_TYPE;
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
