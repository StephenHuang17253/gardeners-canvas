package nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidation;

import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Tests inputs on a variety of rules to check if values are valid
 * Returns a type ValidationResult Enum which informs about if a field passes
 * and if not, why it failed
 * Anotated with @Component such that an autowired method can be used to inject
 * the user service in for email validation
 */
@Component
public class InputValidator {
    private ValidationResult validationResult;
    private boolean passState = true;
    String testedValue;

    private static final int MAX_EMAIL_LENGTH = 320;

    private static UserService userService;
    private static ProfanityService profanityService;

    /**
     * Constructor for the Validation with {@link Autowired} to
     * connect this
     * controller with other services
     *
     * @param inputUserService
     */

    /**
     * Warning, constructor only for automated use, for manual use, use
     * InputValidator(String)
     * Empty constructor so that the spring framework can create an input validator
     * object
     * when injecting the above @Autowired UserService object.
     * Creating an object of this type manually will have no effect and no use
     * except when testing
     */
    @Autowired
    public InputValidator(UserService inputUserService, ProfanityService inputProfanityService) {
        userService = inputUserService;
        profanityService = inputProfanityService;
    }

    /**
     * A private constructor for the input validator,
     * this is used to run the static methods for input validation
     *
     * @param valueToTest the text undergoing validation
     */
    private InputValidator(String valueToTest) {
        testedValue = valueToTest;
        validationResult = ValidationResult.OK;
    }

    /**
     * Checks input against a criteria:
     * This function does not allow blank strings but otherwise the strings can
     * contain anything.
     *
     * @param text - text to validate
     * @return ValidationResult enum state (Enum explains pass/Fail and why if fail)
     */
    public static ValidationResult compulsoryTextField(String text) {

        return new InputValidator(text)
                .blankHelper()
                .lengthHelper(200)
                .getResult();
    }

    /**
     * Checks input against a criteria:
     * This function does not allow blank strings, but otherwise the strings can
     * contain anything.
     * As long as the string does not contain more characters than the length limit
     * parameter.
     *
     * @param text   - text to validate
     * @param length - int, the character limit of string
     * @return ValidationResult enum state (Enum explains pass/Fail and why if fail)
     */
    public static ValidationResult compulsoryTextField(String text, int length) {
        return new InputValidator(text)
                .blankHelper()
                .lengthHelper(length)
                .getResult();
    }

    /**
     * Checks input against a criteria:
     * This function allows blank strings.
     * The string can contain any characters.
     * It checks the string against a default character limit.
     *
     * @param text - text to validate
     * @return ValidationResult enum state (Enum explains pass/Fail and why if fail)
     */
    public static ValidationResult optionalTextField(String text) {
        return new InputValidator(text)
                .lengthHelper(200)
                .getResult();
    }

    /**
     * Checks input against a criteria:
     * This function allows blank strings.
     * The string can contain any characters.
     * It checks the string against a character limit of param length.
     *
     * @param text   - text to validate
     * @param length - int, the character limit of string
     * @return ValidationResult enum state (Enum explains pass/Fail and why if fail)
     */
    public static ValidationResult optionalTextField(String text, int length) {
        return new InputValidator(text)
                .lengthHelper(length)
                .getResult();
    }

    /**
     * This function is called by methods which require checking against a set
     * length.
     * e.g. text fields with length limits
     *
     * @param length - int, the character limit
     * @return the calling object
     */
    private InputValidator lengthHelper(int length) {
        // if this validators input has already failed once, this test wont be run
        if (!this.passState) {
            return this;
        }

        if (testedValue.length() > length) {
            this.validationResult = ValidationResult.LENGTH_OVER_LIMIT;
            validationResult.updateMessage("must be less than or equal to  " + length + " characters");
            this.passState = false;
            return this;
        }
        this.validationResult = ValidationResult.OK;
        return this;
    }

    /**
     * This function is called by methods which require checking a input length
     * containing emojis
     *
     * @param length - int, the character limit
     * @return the calling object
     */
    private InputValidator lengthHelperWithEmojis(int length) {
        // if this validators input has already failed once, this test wont be run
        if (!this.passState) {
            return this;
        }

        String cleanedValue = testedValue.replaceAll("\\s+", "");
        if (cleanedValue.codePointCount(0, cleanedValue.length()) > length) {
            this.validationResult = ValidationResult.LENGTH_OVER_LIMIT;
            validationResult.updateMessage("must be less than or equal to  " + length + " characters");
            this.passState = false;
            return this;
        }
        this.validationResult = ValidationResult.OK;
        return this;
    }

    /**
     * Checks input against a criteria:
     * This function only allows non blank strings containing only alphanumeric
     * characters and select punctuation
     *
     * @param text text to validate
     * @return ValidationResult enum state (Enum explains pass/Fail and why if fail)
     */
    public static ValidationResult compulsoryAlphaPlusTextField(String text) {
        return new InputValidator(text)
                .blankHelper()
                .alphaPlusHelper()
                .getResult();
    }

    /**
     * Checks input against a criteria:
     * This function only allows non blank strings containing only alphanumeric
     * characters and select punctuation
     *
     * @param text   text to validate
     * @param length max number of characters in text
     * @return ValidationResult enum state (Enum explains pass/Fail and why if fail)
     */
    public static ValidationResult compulsoryAlphaPlusTextField(String text, int length) {
        return new InputValidator(text)
                .blankHelper()
                .alphaPlusHelper()
                .lengthHelper(length)
                .getResult();
    }

    /**
     * Checks input against a criteria:
     * This function only allows alphanumeric characters and select punctuation
     *
     * @param text text to validate
     * @return ValidationResult enum state (Enum explains pass/Fail and why if fail)
     */
    public static ValidationResult optionalAlphaPlusTextField(String text) {
        return new InputValidator(text)
                .alphaPlusHelper()
                .getResult();
    }

    /**
     * Checks input against a criteria:
     * This function only allows alphanumeric characters and select punctuation
     * Checks input against character limit of param length.
     *
     * @param text   text to validate
     * @param length int, the character limit of input
     * @return ValidationResult enum state (Enum explains pass/Fail and why if fail)
     */
    public static ValidationResult optionalAlphaPlusTextField(String text, int length) {
        return new InputValidator(text)
                .alphaPlusHelper()
                .lengthHelper(length)
                .getResult();
    }

    /**
     * Checks input against a criteria:
     * This function only allows numeric values and up to 1 comma
     *
     * @param text text to validate
     * @return ValidationResult enum state (Enum explains pass/Fail and why if fail)
     */
    public static ValidationResult validateGardenAreaInput(String text) {
        return new InputValidator(text)
                .numberCommaSingleHelper()
                .gardenAreaHelper(8000000.0, 0.01)
                .getResult();
    }

    public static ValidationResult validatePostcodeInput(String text) {
        return new InputValidator(text)
                .postcodeHelper()
                .getResult();
    }

    public static ValidationResult numberCommaSingleTextField(String text, int length) {
        return new InputValidator(text)
                .numberCommaSingleHelper()
                .lengthHelper(length)
                .getResult();
    }

    /**
     * Checks if the given name is valid
     *
     * @param name string input in text field
     * @return ValidationResult with this.isValid() returning true if valid, false
     *         otherwise and this.getErrorMessage() returning the error message
     */
    public static ValidationResult validateName(String name) {
        return new InputValidator(name)
                .nameHelper()
                .lengthHelper(64)
                .getResult();
    }

    /**
     * Checks if the given decsription is valid 512 char or less and contains at
     * least one letter if not empty will return invalid description message in
     * either case
     *
     * @param text
     * @return
     */
    public static ValidationResult validateDescription(String text) {
        ValidationResult result = new InputValidator(text)
                .lengthHelperWithEmojis(512)
                .NotOnlyNumOrSpecChar()
                .getResult();

        if (!result.valid()) {
            result.updateMessage(ValidationResult.INVALID_DESCRIPTION.toString());
            return result;
        }

        ValidationResult result2 = new InputValidator(text)
                .profanityHelper()
                .getResult();
        if (!result2.valid()) {
            return result2;
        }

        return result;

    }

    /**
     * Checks if the given email is valid and unique
     *
     * @param email
     * @return ValidationResult with this.isValid() returning true if valid, false
     *         otherwise and this.getErrorMessage() returning the error message
     */
    public static ValidationResult validateUniqueEmail(String email) {

        return new InputValidator(email)
                .emailSyntaxHelper()
                .emailUniquenessHelper()
                .getResult();
    }

    /**
     * Checks if the given email is valid
     *
     * @param email
     * @return ValidationResult with this.isValid() returning true if valid, false
     *         otherwise and this.getErrorMessage() returning the error message
     */
    public static ValidationResult validateEmail(String email) {
        return new InputValidator(email)
                .emailSyntaxHelper()
                .getResult();
    }

    /**
     * Checks if the given password is valid
     *
     * @param password
     * @param otherFields other input fields to test likeness with password
     * @return ValidationResult with this.isValid() returning true if valid, false
     *         otherwise and this.getErrorMessage() returning the error message
     */
    public static ValidationResult validatePassword(String password, List<String> otherFields) {
        ValidationResult result = new InputValidator(password)
                .passwordLikenessHelper(otherFields)
                .passwordSyntaxHelper()
                .minimumLengthHelper(8)
                .getResult();
        if (result == ValidationResult.LENGTH_UNDER_MINIMUM) {
            result = ValidationResult.INVALID_PASSWORD;
        }
        return result;
    }

    /**
     * Checks if the given dob is valid
     *
     * @param dob
     * @return ValidationResult with this.isValid() returning true if valid, false
     *         otherwise and this.getErrorMessage() returning the error message
     */
    public static ValidationResult validateDOB(String dob) {
        return new InputValidator(dob)
                .dateFormatHelper()
                .dateAgeHelper()
                .getResult();
    }

    public static ValidationResult validatePlantDate(String date) {
        return new InputValidator(date)
                .dateFormatHelper()
                .plantAgeHelper()
                .getResult();
    }

    /**
     * Checks if the given date is in a valid format
     *
     * @param date
     * @return ValidationResult with this.isValid() returning true if valid, false
     *         otherwise and this.getErrorMessage() returning the error message
     */
    public static ValidationResult validateDate(String date) {
        return new InputValidator(date)
                .dateFormatHelper()
                .getResult();
    }

    /**
     * Checks if the given plantCount is a valid integer between 1 and 1,000,000 or if empty string
     *
     * @param plantCount the plant count to validate
     * @return ValidationResult with this.isValid() returning true if valid, false
     *         otherwise and this.getErrorMessage() returning the error message
     */
    public static ValidationResult validatePlantCount(String plantCount) {
        if (plantCount.equals("")) {
            return ValidationResult.OK;
        }
        ValidationResult result = new InputValidator(String.valueOf(plantCount))
                .validWholeNumberHelper()
                .maxNumberHelper(1000000)
                .minNumberHelper(1)
                .getResult();
        if (!result.valid()) {
            result = ValidationResult.INVALID_PLANT_COUNT;
        }
        return result;
    }

    /**
     * Checks if a string represents a valid float that is also a whole number
     * updates local variables with results
     * ignored if string failed any previous validation
     *
     * @return the calling object
     */
    private InputValidator validWholeNumberHelper() {
        if (!this.passState) {
            return this;
        }

        try {
            float floatValue = Float.parseFloat(testedValue.replace(",", "."));
            if (floatValue % 1 != 0) { // Checks if float isn't a whole number (i.e. its not an integer)
                this.validationResult = ValidationResult.INVALID_PLANT_COUNT;
                this.passState = false;
                return this;
            }
        } catch (NumberFormatException e) {
            this.validationResult = ValidationResult.INVALID;
            this.passState = false;
            return this;
        }

        this.validationResult = ValidationResult.OK;
        return this;
    }

    /**
     * Checks if a string is blank or not if a string is
     * updates local variables with results
     * ignored if string failed any previous validation
     *
     * @return the calling object
     */
    private InputValidator blankHelper() {
        // if this validators input has already failed once, this test wont be run
        if (!this.passState) {
            return this;
        }

        if (testedValue.isBlank()) {
            this.validationResult = ValidationResult.BLANK;
            this.passState = false;
            return this;
        }
        this.validationResult = ValidationResult.OK;
        return this;
    }

    /**
     * Checks if a string is a valid integer
     * updates local variables with results
     * ignored if string failed any previous validation
     *
     * @return the calling object
     */
    private InputValidator validIntegerHelper() {
        // if this validators input has already failed once, this test wont be run
        if (!this.passState) {
            return this;
        }

        try {
            Integer.parseInt(testedValue);
        } catch (NumberFormatException error) {
            this.validationResult = ValidationResult.INVALID;
            this.passState = false;
            return this;
        }
        this.validationResult = ValidationResult.OK;
        return this;
    }

    /**
     * Checks if a string only contains letters, spaces, hyphens or apostrophes
     * updates local variables with results
     * ignored if string failed any previous validation
     *
     * @return the calling object
     */
    private InputValidator nameHelper() {
        // if this validators input has already failed once, this test wont be run
        if (!this.passState) {
            return this;
        }

        if (!testedValue.matches("^\\p{L}[\\p{L} \\-'’]*$")) {
            this.validationResult = ValidationResult.INVALID_USERNAME;
            this.passState = false;
            return this;
        }
        this.validationResult = ValidationResult.OK;
        return this;
    }

    /**
     * Checks if a string matches proper email syntax
     * updates local variables with results
     * ignored if string failed any previous validation
     *
     * @return the calling object
     */
    private InputValidator emailSyntaxHelper() {
        // if this validators input has already failed once, this test wont be run
        if (!this.passState) {
            return this;
        }
        if (testedValue.length() > MAX_EMAIL_LENGTH) {
            this.validationResult = ValidationResult.EMAIL_TO_LONG;
            this.passState = false;
            return this;
        }

        String emailRegex = "^[\\p{L}\\p{M}\\p{N}]{1,}(?:[._-][\\p{L}\\p{M}\\p{N}]+)*@[a-zA-Z0-9-]{1,}\\.[a-zA-Z]{2,}(?:\\.[a-zA-Z]{2,})?$";

        if (!testedValue.matches(emailRegex)) {

            this.validationResult = ValidationResult.INVALID_EMAIL;
            this.passState = false;
            return this;
        }
        String[] parts = testedValue.split("@", 2);
        String localPart = parts[0];
        String domainPart = parts[1];
        if (localPart.length() > 64 || domainPart.length() > 255) {
            this.validationResult = ValidationResult.EMAIL_TO_LONG;
            this.passState = false;
            return this;
        }

        this.validationResult = ValidationResult.OK;
        return this;
    }

    /**
     * Checks if a string representing an email is unique
     * updates local variables with results
     * ignored if string failed any previous validation
     *
     * @return the calling object
     */
    private InputValidator emailUniquenessHelper() {
        // if this validators input has already failed once, this test wont be run
        if (!this.passState) {
            return this;
        }

        if (userService.emailInUse(testedValue)) {
            this.validationResult = ValidationResult.NON_UNIQUE_EMAIL;
            this.passState = false;
            return this;
        }
        this.validationResult = ValidationResult.OK;
        return this;
    }

    /**
     * Checks if a string matches proper password syntax
     * updates local variables with results
     * ignored if string failed any previous validation
     *
     * @return the calling object
     */
    private InputValidator passwordSyntaxHelper() {
        // if this validators input has already failed once, this test wont be run
        if (!this.passState) {
            return this;
        }

        if (!testedValue.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9\\s]).+$")) {
            this.validationResult = ValidationResult.INVALID_PASSWORD;
            this.passState = false;
            return this;
        }
        this.validationResult = ValidationResult.OK;
        return this;
    }

    /**
     * Checks if a string representing an password is like any other user inputs
     * updates local variables with results
     * ignored if string failed any previous validation
     *
     * @param fields the list of fields to test likeness with password
     * @return the calling object
     */
    private InputValidator passwordLikenessHelper(List<String> fields) {
        // if this validators input has already failed once, this test wont be run
        if (!this.passState) {
            return this;
        }

        for (String field : fields) {
            String lower_case_field = field.toLowerCase();
            String lower_case_tested_value = testedValue.toLowerCase();
            if (lower_case_tested_value.contains(lower_case_field) && !field.equals("")) {
                this.validationResult = ValidationResult.INVALID_PASSWORD;
                this.passState = false;
                return this;
            }
        }
        this.validationResult = ValidationResult.OK;
        return this;
    }

    private InputValidator minimumLengthHelper(int minimumNumberOfDigits) {
        // if this validators input has already failed once, this test wont be run
        if (!this.passState) {
            return this;
        }

        if (testedValue.length() < minimumNumberOfDigits) {
            this.passState = false;
            this.validationResult = ValidationResult.LENGTH_UNDER_MINIMUM;
            return this;
        }
        this.validationResult = ValidationResult.OK;
        return this;
    }

    /**
     * Checks if a string matches proper date syntax
     * updates local variables with results
     * ignored if string failed any previous validation
     *
     * @return the calling object
     */
    private InputValidator dateFormatHelper() {
        // if this validators input has already failed once, this test wont be run
        if (!this.passState) {
            return this;
        }

        List<String> dateList = Arrays.asList(testedValue.split("/"));

        if (dateList.size() != 3) {
            this.validationResult = ValidationResult.INVALID_DATE_FORMAT;
            this.passState = false;
            return this;
        }

        if (dateList.get(0).length() != 2 || dateList.get(1).length() != 2 || dateList.get(2).length() != 4) {
            this.validationResult = ValidationResult.INVALID_DATE_FORMAT;
            this.passState = false;
            return this;
        }

        try {
            LocalDate date;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
            date = LocalDate.parse(testedValue, formatter);
            if (!date.isLeapYear() && Objects.equals(dateList.get(0), "29") && Objects.equals(dateList.get(1), "02")) {
                this.validationResult = ValidationResult.INVALID_DATE_FORMAT;
                this.passState = false;
                return this;
            }
            if (date.lengthOfMonth() < Integer.parseInt(dateList.get(0))) {
                this.validationResult = ValidationResult.INVALID_DATE_FORMAT;
                this.passState = false;
                return this;
            }
        } catch (Exception e) {
            this.validationResult = ValidationResult.INVALID_DATE_FORMAT;
            this.passState = false;
            return this;
        }

        for (String s : dateList) {
            if (!s.matches("[0-9]+")) {
                this.validationResult = ValidationResult.INVALID_DATE_FORMAT;
                this.passState = false;
                return this;
            }
        }

        this.validationResult = ValidationResult.OK;
        return this;
    }

    /**
     * Checks if an entered date is more than 120 years ago or less than 13 years
     * ago
     * updates local variables with results
     * ignored if string failed any previous validation
     *
     * @return the calling object
     */
    private InputValidator dateAgeHelper() {
        // if this validators input has already failed once, this test wont be run
        if (!this.passState) {
            return this;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
        LocalDate inputtedDate = LocalDate.parse(testedValue, formatter);

        long yearsDifference = ChronoUnit.YEARS.between(
                inputtedDate,
                LocalDate.now());
        if (yearsDifference < 13) {
            this.validationResult = ValidationResult.AGE_BELOW_13;
            this.passState = false;
            return this;
        } else if (yearsDifference > 120) {
            this.validationResult = ValidationResult.AGE_ABOVE_120;
            this.passState = false;
            return this;
        }

        this.validationResult = ValidationResult.OK;
        return this;
    }

    private InputValidator plantAgeHelper() {
        // if this validator's input has already failed once, this test won't be run
        if (!this.passState) {
            return this;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
        LocalDate inputtedDate = LocalDate.parse(testedValue, formatter);

        LocalDate oneYearFromNow = LocalDate.now().plusYears(1);

        if (inputtedDate.isAfter(oneYearFromNow)) {
            this.validationResult = ValidationResult.PLANT_DATE_MORE_THAN_ONE_YEAR_IN_FUTURE;
            this.passState = false;
            return this;
        }

        long yearsDifference = ChronoUnit.YEARS.between(
                inputtedDate,
                LocalDate.now());

        if (yearsDifference > 400) {
            this.validationResult = ValidationResult.PLANT_AGE_ABOVE_400;
            this.passState = false;
            return this;
        }

        this.validationResult = ValidationResult.OK;
        return this;
    }

    /**
     * Checks if a string contains any non ( alphanumeric plus allowed punctuation)
     * characters
     * updates local variables with results
     * ignored if string failed any previous validation
     *
     * @return the calling object
     */
    private InputValidator alphaPlusHelper() {
        // if this validators input has already failed once, this test wont be run
        if (!this.passState) {
            return this;
        }

        boolean stringPasses = true;
        String[] allowedPunctuation = new String[] { " ", ",", ".", "'", "-" };
        // checks if all letters in this string are alpha numeric, if a letter fails it
        // checks it against
        // the allowed punctuation list, if that fails the string is marked as invalid
        for (Character letter : testedValue.toCharArray()) {
            if (!Character.isLetterOrDigit(letter)) {

                for (String punctuation : allowedPunctuation) {
                    stringPasses = false;
                    if (punctuation.equals(letter.toString())) {
                        stringPasses = true;
                        break;
                    }
                }
                if (!stringPasses) {
                    break;
                }
            }
        }

        // sets this items result to ok if string passes and to "Non alpha plus" if it
        // fails
        if (stringPasses) {
            this.validationResult = ValidationResult.OK;
        } else {
            this.validationResult = ValidationResult.NON_ALPHA_PLUS;
            this.passState = false;
        }
        return this;

    }

    /**
     * Checks if a string contains only numbers and up to 1 comma character
     * updates local variables with results
     * ignored if string failed any previous validation
     *
     * @return the calling object
     */
    private InputValidator numberCommaSingleHelper() {
        // if this validators input has already failed once, this test wont be run
        if (!this.passState) {
            return this;
        }

        // checks if number is a negative number
        if (!testedValue.isBlank() && testedValue.strip().charAt(0) == '-') {
            validationResult = ValidationResult.NON_NUMERIC_COMMA;
            passState = false;
            return this;
        }

        boolean stringPasses = true;
        int allowedCommaNumber = 1; // allowed number of commas or full stops

        // checks if string contains only numbers and up to 1 comma or full stop
        for (Character letter : testedValue.toCharArray()) {
            if (!Character.isDigit(letter)) {
                if (Character.toLowerCase(letter) == 'e') {
                    continue;
                }

                if (letter.equals("-".toCharArray()[0])) {
                    try {
                        Double.parseDouble(testedValue.replace(',', '.'));
                    } catch (Exception e) {
                        validationResult = ValidationResult.NON_NUMERIC_COMMA;
                        passState = false;
                        return this;
                    }
                    continue;
                }

                stringPasses = false;
                if ((letter.toString().equals(",") || letter.toString().equals(".")) && allowedCommaNumber > 0) {
                    stringPasses = true;
                    allowedCommaNumber -= 1;
                }
            }

        }

        // forbids that input numbers can be just a single comma (because that breaks
        // things)
        if (testedValue.length() == 1 && allowedCommaNumber == 0) {
            stringPasses = false;
        }
        try {
            if (!testedValue.isBlank()) {
                Float.parseFloat(testedValue.replace(',', '.'));
            }
        } catch (NumberFormatException parseException) {
            stringPasses = false;
        }

        // sets this items result to ok if string passes and to "Non Numeric Comma" if
        // it fails
        if (stringPasses) {
            this.validationResult = ValidationResult.OK;
        } else {
            this.validationResult = ValidationResult.NON_NUMERIC_COMMA;
            this.passState = false;
        }
        return this;
    }

    /**
     * Checks if a garden area is a valid number
     * updates local variables with results
     * ignored if string failed any previous validation
     *
     * @param maxArea max area of garden
     * @param minArea min area of garden
     * @return the calling object
     */
    private InputValidator gardenAreaHelper(Double maxArea, Double minArea) {
        // if this validators input has already failed once, this test wont be run
        if (!this.passState) {
            return this;
        }

        double doubleGardenSize;
        if (testedValue.isBlank()) {
            this.validationResult = ValidationResult.OK;
            return this;
        } else {
            doubleGardenSize = Double.parseDouble(testedValue.replace(",", "."));
        }

        if (doubleGardenSize < minArea) {
            this.validationResult = ValidationResult.AREA_TOO_SMALL;
            this.passState = false;
            return this;
        }
        if (doubleGardenSize > maxArea) {
            this.validationResult = ValidationResult.AREA_TOO_LARGE;
            this.passState = false;
            return this;
        }
        this.validationResult = ValidationResult.OK;
        return this;
    }

    private InputValidator postcodeHelper() {
        if (!this.passState) {
            return this;
        }

        if (!testedValue.matches("^[a-zA-Z0-9 ]*$")) {
            this.validationResult = ValidationResult.INVALID_POSTCODE;
            this.passState = false;
            return this;
        }
        this.validationResult = ValidationResult.OK;
        return this;
    }

    private InputValidator NotOnlyNumOrSpecChar() {
        if (!this.passState) {
            return this;
        }

        String filteredValue = testedValue.replaceAll("\\s+", "");

        if (!filteredValue.equals("") && !filteredValue.matches(".*[a-zA-Z].*")) {
            this.validationResult = ValidationResult.INVALID_DESCRIPTION;
            this.passState = false;
            return this;
        }
        this.validationResult = ValidationResult.OK;
        return this;

    }

    /**
     * Sends A string to the bad words API, then checks if the return has
     * bad word count over 0, if so set to TEXT_CONTAINS_PROFANITY
     * ignored if string failed any previous validation
     *
     * @return the calling object
     */
    public InputValidator profanityHelper() {
        if (!this.passState) {
            return this;
        }
        boolean containsProfanity = profanityService.containsProfanity(testedValue);

        if (containsProfanity) {
            this.validationResult = ValidationResult.DESCRIPTION_CONTAINS_PROFANITY;
            this.passState = false;
            return this;
        }

        this.validationResult = ValidationResult.OK;
        return this;
    }

    /**
     * Checks if a string is a valid integer less than the given minValue
     * updates local variables with results
     * ignored if string failed any previous validation
     *
     * @param minValue the minimum value the number can be
     * @return the calling object
     */
    private InputValidator minNumberHelper(int minValue) {
        int snippedTestValue = (int) Double.parseDouble(testedValue.replace(",", ".")); // snips decimal value of floats off
        if (!this.passState) {
            return this;
        }

        if (snippedTestValue < minValue) {
            this.validationResult = ValidationResult.INVALID;
            this.passState = false;
            return this;
        }

        this.validationResult = ValidationResult.OK;
        return this;
    }

    /**
     * Checks if a string is a valid integer less than the given maxValue
     * updates local variables with results
     * ignored if string failed any previous validation
     *
     * @param maxValue the maximum value the number can be
     * @return the calling object
     */
    private InputValidator maxNumberHelper(int maxValue) {
        int snippedTestValue = (int) Double.parseDouble(testedValue.replace(",", ".")); // snips decimal value of floats off
        if (!this.passState) {
            return this;
        }

        if (snippedTestValue > maxValue) {
            this.validationResult = ValidationResult.INVALID;
            this.passState = false;
            return this;
        }

        this.validationResult = ValidationResult.OK;
        return this;
    }

    /**
     * returns this objects validation result
     *
     * @return validationResult variable of object
     */
    private ValidationResult getResult() {
        return this.validationResult;
    }

}
