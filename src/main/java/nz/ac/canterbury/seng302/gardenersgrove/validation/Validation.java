package nz.ac.canterbury.seng302.gardenersgrove.validation;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;

/**
 * Service class for Validation, defined by the {@link Service} annotation.
 */
@Service
public class Validation {

    Logger logger = LoggerFactory.getLogger(Validation.class);
    private final UserService userService;

    /**
     * Constructor for the Validation with {@link Autowired} to
     * connect this
     * controller with other services
     * 
     * @param userService
     */
    @Autowired
    public Validation(UserService userService) {
        this.userService = userService;
    }

    /**
     * Checks if the given name is valid
     * 
     * @param name
     * @param firstName true if first name, false if last name
     * @return ValidationResult with this.isValid() returning true if valid, false
     *         otherwise and this.getErrorMessage() returning the error message
     */
    public ValidationResult validateName(String name, boolean firstName) {
        // alpha or hyphen, space or apostrophe
        String namePrefix = firstName ? "First" : "Last";

        if (!name.matches("[a-zA-Z\\-\\s']+")) {
            return new ValidationResult(false,
                    namePrefix + " name cannot be empty and must only include letters, spaces, hyphens or apostrophes");
        }

        if (name.length() > 64) {
            return new ValidationResult(false, namePrefix + " name must be 64 characters long or less");
        }

        return new ValidationResult(true, "");
    }

    /**
     * Checks if the given email is valid
     * 
     * @param email
     * @param unique true if email must be unique, false otherwise
     * @return ValidationResult with this.isValid() returning true if valid, false
     *         otherwise and this.getErrorMessage() returning the error message
     */
    public ValidationResult validateEmail(String email, boolean unique) {
        // widely used email regex
        boolean valid = email.matches("^[a-zA-Z0-9\\._-]+@[a-zA-Z0-9\\.-]+\\.[a-zA-Z]{2,4}$");
        String errorMessage = "Email address must be in the form ‘jane@doe.nz’";

        if (userService.emailInUse(email) && unique) {
            valid = false;
            errorMessage = "This email address is already in use";
        }

        return new ValidationResult(valid, errorMessage);
    }

    /**
     * Checks if the given password is valid
     * 
     * @param password
     * @return ValidationResult with this.isValid() returning true if valid, false
     *         otherwise and this.getErrorMessage() returning the error message
     */
    public ValidationResult validatePassword(String password) {
        // >= 8 characters, at least one lower case letter, one
        // upper case letter, one number, and one special character
        boolean valid = password.length() >= 8
                && password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9\\s]).+$");
        String errorMessage = "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.";
        return new ValidationResult(valid, errorMessage);
    }

    /**
     * Checks if the given dob is valid
     * 
     * @param dob
     * @return ValidationResult with this.isValid() returning true if valid, false
     *         otherwise and this.getErrorMessage() returning the error message
     */
    public ValidationResult validateDOB(String dob) {
        // dob format DD/MM/YYYY

        // Checking correct formatting
        List<String> dobList = Arrays.asList(dob.split("/"));

        String errorMessage = "Date in not in valid format, DD/MM/YYYY";

        if (dobList.size() != 3) {
            return new ValidationResult(false, errorMessage);
        }

        if (dobList.get(0).length() != 2 || dobList.get(1).length() != 2 || dobList.get(2).length() != 4) {
            return new ValidationResult(false, errorMessage);
        }

        for (String s : dobList) {
            if (!s.matches("[0-9]+")) {
                return new ValidationResult(false, errorMessage);
            }
        }

        int[] intDobList = dobList.stream().mapToInt(Integer::parseInt).toArray();

        int year = java.time.LocalDate.now().getYear();

        // This code is commented out as it's functionality is useful but not required
        // under the stories in sprint 1

        // int month = java.time.LocalDate.now().getMonthValue();
        // int day = java.time.LocalDate.now().getDayOfMonth();
        // // Checking if date days/months/years valid, not required under U1
        // if (intDobList[0] > 31 || intDobList[0] < 1 || intDobList[1] > 12 ||
        // intDobList[1] < 1 || intDobList[2] > year || intDobList[1] < 1) {
        // return new ValidationResult(false, "Not a valid date");
        // }
        //
        // // Checking if date in future, not required under U1
        // errorMessage = "Cannot enter a date of birth in the future";
        // // year in future
        // if (intDobList[2] > year) {
        // return new ValidationResult(false, errorMessage);
        // }
        // // this year
        // if (intDobList[2] == year) {
        // // month in future
        // if (intDobList[1] > month) {
        // return new ValidationResult(false, errorMessage);
        // }
        // // this month
        // if (intDobList[1] == month) {
        // // day in future
        // if (intDobList[0] > day) {
        // return new ValidationResult(false, errorMessage);
        // }
        // }
        // }

        // Checking Age
        int age = year - intDobList[2];

        if (age < 13) {
            return new ValidationResult(false, "You must be 13 years or older to create an account");
        } else if (age > 120) {
            return new ValidationResult(false, "The maximum age allowed is 120 years");
        }

        return new ValidationResult(true, "");
    }

}
