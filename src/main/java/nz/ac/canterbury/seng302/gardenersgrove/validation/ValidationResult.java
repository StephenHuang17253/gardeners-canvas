package nz.ac.canterbury.seng302.gardenersgrove.validation;

/**
 * result returned by the input validator to inform if an input is valid or no.
 * also informs why input is invalid
 */
public enum ValidationResult {
    OK("valid"),
    INVALID("invalid"),
    BLANK("cannot be empty"),
    NOT_PARSABLE("cannot be parsed to a number"),
    NON_ALPHA_PLUS("must only include letters, numbers, spaces, commas, dots, hyphens or apostrophes"),
    INVALID_STREET("must only include letters, numbers, spaces, commas, dots, hyphens, slashes or apostrophes"),
    INVALID_POSTCODE("must only contain numbers, letters, hyphens, and spaces"),
    INVALID_USERNAME("cannot be empty and must only include letters, spaces, hyphens or apostrophes"),
    INVALID_EMAIL("Email must be in the form 'jane@doe.nz'"),
    EMAIL_TO_LONG(
            "Email is too long, should be 320 characters or less. The local part should be max 64 characters and domain should be max 255 characters"),
    INVALID_PASSWORD(
            "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character."),
    AGE_BELOW_13("You must be 13 years or older to create an account"),
    AGE_ABOVE_120("The maximum age allowed is 120 years"),
    PLANT_AGE_ABOVE_400("Plant date cannot be more than 400 years ago."),
    PLANT_DATE_MORE_THAN_ONE_YEAR_IN_FUTURE("Plant date cannot be more than a year in the future."),
    INVALID_DATE_FORMAT("Date in not in valid format, DD/MM/YYYY"),
    NON_UNIQUE_EMAIL("This email address is already in use"),
    NON_NUMERIC_COMMA("must be a positive number"),
    LENGTH_OVER_LIMIT("must be less than limit characters"),
    LENGTH_UNDER_MINIMUM("must be more than minimum number of characters"),
    INVALID_FILE_TYPE("Image must be of type png, jpg or svg"),
    INVALID_FILE_SIZE("Image must be less than 10MB"),
    AREA_TOO_LARGE("must be a positive number. \n\r Must be less than or equal to 8000000"),
    AREA_TOO_SMALL("must be a positive number. \n\r Must be greater than or equal to 0.01"),
    DESCRIPTION_CONTAINS_PROFANITY("The description does not match the language standards of the app"),
    INVALID_DESCRIPTION("Description must be 512 characters or less and contain some letters"),
    INVALID_PLANT_COUNT("Plant count must be a positive whole number between 1 and 1,000,000"),
    INVALID_CATEGORY("A plant category must be selected");

    private String message;

    ValidationResult(String inMessage) {
        this.message = inMessage;
    }

    /**
     * Returns if the string passed validation
     *
     * @return pass status (true for yes)
     */
    public boolean valid() {
        return this == OK;
    }

    /**
     * returns a basic description of why the input failed
     * IMPORTANT: this is not sufficient as user feedback, more info is needed if
     * this output is shown on UI
     *
     * @return fail or ok Message
     */
    @Override
    public String toString() {
        return this.message;
    }

    public void updateMessage(String newMessage) {
        this.message = newMessage;
    }
}
