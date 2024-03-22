package nz.ac.canterbury.seng302.gardenersgrove.validation.InputValidator;

/**
 * result returned by the input validator to inform if an input is valid or no.
 * also informs why input is invalid
 */
public enum ValidationResult {
    OK("valid", true),
    BLANK("cannot be empty", false),
    NOT_PARSABLE("cannot be parsed to a number",false),
    NON_ALPHA_PLUS("must only include letters, numbers, spaces, commas, dots, hyphens or apostrophes",false),
    NON_NUMERIC_COMMA("must be a positive number",false),
    LENGTH_OVER_LIMIT("must be less than limit characters", false);




    private String message;
    private final boolean isValid;

    ValidationResult(String inMessage,Boolean inIsValid)
    {
        this.message = inMessage;
        this.isValid = inIsValid;
    }

    /**
     * Returns if the string passed validation
     * @return pass status (true for yes)
     */
    public boolean valid()
    {
        return this.isValid;
    }

    /**
     * returns a basic description of why the input failed
     * IMPORTANT: this is not sufficient as user feedback, more info is needed if this output is shown on UI
     * @return fail or ok Message
     */
    @Override
    public String toString()
    {
        return this.message;
    }

    public void updateMessage(String newMessage)
    {
        this.message = newMessage;
    }
}
