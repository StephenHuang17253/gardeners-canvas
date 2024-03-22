package nz.ac.canterbury.seng302.gardenersgrove.validation.InputValidator;

/**
 * result returned by the input validator to inform if an input is valid or no.
 * also informs why input is invalid
 */
public enum ValidationResult {
    OK("valid"),
    BLANK("cannot be empty"),
    NOT_PARSABLE("cannot be parsed to a number"),
    NON_ALPHA_PLUS("must only include letters, numbers, spaces, commas, dots, hyphens or apostrophes"),

    INVALID_USERNAME("cannot be empty and must only include letters, spaces, hyphens or apostrophes"),

    INVALID_EMAIL("Email address must be in the form ‘jane@doe.nz"),

    INVALID_PASSWORD("Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character."),

    AGE_BELOW_13("You must be 13 years or older to create an account"),

    AGE_ABOVE_120("The maximum age allowed is 120 years"),

    NON_UNIQUE_EMAIL("This email address is already in use"),
    NON_NUMERIC_COMMA("must be a positive number"),
    LENGTH_OVER_LIMIT("must be less than limit characters");




    private String message;

    ValidationResult(String inMessage)
    {
        this.message = inMessage;
    }

    /**
     * Returns if the string passed validation
     * @return pass status (true for yes)
     */
    public boolean valid()
    {
        return this == OK;
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
