package nz.ac.canterbury.seng302.gardenersgrove.validation.InputValidator;

import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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

    private static UserService userService;

    /**
     * Constructor for the Validation with {@link Autowired} to
     * connect this
     * controller with other services
     *
     * @param inputUserService
     */
    @Autowired
    public void UserService(UserService inputUserService) {
        userService = inputUserService;
    }

    /**
     * Warning, constructor only for automated use, for manual use, use
     * InputValidator(String)
     * Empty constructor so that the spring framework can create an input validator
     * object
     * when injecting the above @Autowired UserService object.
     * Creating an object of this type manually will have no effect and no use
     * except when testing
     */
    public InputValidator() {
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
    public static ValidationResult compulsoryTextFieldWithLengthLimit(String text, int length) {
        return new InputValidator(text)
                .blankHelper()
                .lengthHelper(length)
                .getResult();
    }

    /**
     * Checks input against a criteria:
     * This function allows blank strings.
     * The string can contain any characters.
     * It checks the string against a character limit.
     * 
     * @param text   - text to validate
     * @param length - int, the character limit of string
     * @return ValidationResult enum state (Enum explains pass/Fail and why if fail)
     */
    public static ValidationResult optionalTextFieldWithLengthLimit(String text, int length) {
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
                .lengthHelper(200)
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
                .lengthHelper(200)
                .getResult();
    }

    /**
     * Checks input against a criteria:
     * This function only allows numeric values and up to 1 comma
     * 
     * @param text text to validate
     * @return ValidationResult enum state (Enum explains pass/Fail and why if fail)
     */
    public static ValidationResult numberCommaSingleTextField(String text) {
        return new InputValidator(text)
                .numberCommaSingleHelper()
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
     * @return ValidationResult with this.isValid() returning true if valid, false
     *         otherwise and this.getErrorMessage() returning the error message
     */
    public static ValidationResult validatePassword(String password) {
        return new InputValidator(password)
                .passwordSyntaxHelper()
                .getResult();
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

        if (!testedValue.matches("[a-zA-Z\\-\\s']+")) {
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

        if (!testedValue.matches("^[a-zA-Z0-9._-]{1,64}@[a-zA-Z0-9.-]{1,255}\\.[a-zA-Z]{2,4}$")) {
            this.validationResult = ValidationResult.INVALID_EMAIL;
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

        List<String> dobList = Arrays.asList(testedValue.split("/"));

        if (dobList.size() != 3) {
            this.validationResult = ValidationResult.INVALID_DATE_FORMAT;
            this.passState = false;
            return this;
        }

        if (dobList.get(0).length() != 2 || dobList.get(1).length() != 2 || dobList.get(2).length() != 4) {
            this.validationResult = ValidationResult.INVALID_DATE_FORMAT;
            this.passState = false;
            return this;
        }

        for (String s : dobList) {
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
        String[] allowedPunctuation = new String[] { " ", ",", ".", "'", "\"", "-" };
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
        boolean stringPasses = true;
        int allowedCommaNumber = 1; // allowed number of commas or full stops

        // checks if string contains only numbers and up to 1 comma or full stop
        for (Character letter : testedValue.toCharArray()) {
            if (!Character.isDigit(letter)) {
                stringPasses = false;
                if ((letter.toString().equals(",") || letter.toString().equals(".")) && allowedCommaNumber > 0) {
                    stringPasses = true;
                    allowedCommaNumber -= 1;
                }
            }

            if (letter.equals("-".toCharArray()[0])) {
                validationResult = ValidationResult.NON_NUMERIC_COMMA;
                passState = false;
                return this;

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
     * returns this objects validation result
     * 
     * @return validationResult variable of object
     */
    private ValidationResult getResult() {
        return this.validationResult;
    }

}
