package nz.ac.canterbury.seng302.gardenersgrove.validation.fileValidation;

/**
 * Enum for file types, used to validate files based on their type
 */
public enum FileType {
    IMAGES(new String[] { "png", "jpg", "svg", "jpeg", "jfif", "pjpeg", "pjp" }),
    DOCUMENTS(new String[] { "pdf", "doc", "docx", "txt" }),
    VIDEOS(new String[] { "mp4", "avi", "mov", "wmv" });

    private String[] extensions;

    /**
     * Constructor for FileType
     * @param extensions
     */
    FileType(String[] extensions) {
        this.extensions = extensions;
    }

    /**
     * Returns the extensions of the file type
     * @return extensions
     */
    public String[] getExtensions() {
        return extensions;
    }
}