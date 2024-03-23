package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.InputValidator.InputValidator;
import nz.ac.canterbury.seng302.gardenersgrove.validation.InputValidator.ValidationResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import java.util.Objects;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * This is a basic spring boot controller for the registration form page,
 * note the {@link Controller} annotation which defines this.
 * This controller defines endpoints as functions with specific HTTP mappings
 */
@Controller
public class RegistrationFormController {
    Logger logger = LoggerFactory.getLogger(RegistrationFormController.class);
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    /**
     * Constructor for the RegistrationFormController with {@link Autowired} to
     * connect this
     * controller with other services
     * 
     * @param userService to use for checking persistence to validate email and password
     * @param authenticationManager to login user after registration
     */
    @Autowired
    public RegistrationFormController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Set the security context for the user
     * This method is shared functionality between the login and registration pages
     * possibly should be moved to a different class? As not correct to be here
     * 
     * @param email of user who is registering
     * @param password of user who is registering
     * @param session http session to set the cookies with the context key
     */
    public void setSecurityContext(String email, String password, HttpSession session) {
        User user = userService.getUserByEmailAndPassword(email, password);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getEmailAddress(),
                password);

        Authentication authentication = authenticationManager.authenticate(token);
        // Check if the authentication is actually authenticated (in this example any
        // username/password is accepted so this should never be false)
        if (authentication.isAuthenticated()) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());
        }
    }

    /**
     * Redirects GET url '/register' to the registration form
     *
     * @param firstName      - user's first name
     * @param lastName       - user's last name
     * @param noLastName     - checkbox for whether user has a last name
     * @param dateOfBirth    - user's date of birth (optional)
     * @param emailAddress   - user's email address
     * @param password       - user's password
     * @param repeatPassword - user's repeating password for confirmation
     * @param model          - (map-like) representation of user's input (above
     *                       parameters)
     * @return redirect to registration form
     */
    @GetMapping("/register")
    public String registrationForm(@RequestParam(name = "firstName", defaultValue = "") String firstName,
            @RequestParam(name = "lastName", required = false, defaultValue = "") String lastName,
            @RequestParam(name = "noLastName", required = false, defaultValue = "false") boolean noLastName,
            @RequestParam(name = "dateOfBirth", required = false, defaultValue = "") String dateOfBirth,
            @RequestParam(name = "emailAddress", defaultValue = "") String emailAddress,
            @RequestParam(name = "password", defaultValue = "") String password,
            @RequestParam(name = "repeatPassword", defaultValue = "") String repeatPassword,
            Model model) {
        logger.info("GET /register");
        addUserAttributes(firstName, lastName, noLastName, dateOfBirth,
                emailAddress, password, repeatPassword, model);
        return "registrationForm";
    }

    /**
     * Redirects POST url '/register' to the registration form if invalid input
     * or to user's profile page if registration completed
     *
     * @param firstName      - user's first name
     * @param lastName       - user's last name
     * @param noLastName     - checkbox for whether user has a last name
     * @param dateOfBirth    - user's date of birth (optional)
     * @param emailAddress   - user's email address
     * @param password       - user's password
     * @param repeatPassword - user's repeating password for confirmation
     * @param model          - (map-like) representation of user's input (above
     *                       parameters)
     * @return redirect to registration form or to profile page
     */
    @PostMapping("/register")
    public String submitRegistrationForm(HttpServletRequest request,
            @RequestParam(name = "firstName", defaultValue = "") String firstName,
            @RequestParam(name = "lastName", required = false, defaultValue = "") String lastName,
            @RequestParam(name = "noLastName", required = false, defaultValue = "false") boolean noLastName,
            @RequestParam(name = "dateOfBirth", required = false, defaultValue = "") String dateOfBirth,
            @RequestParam(name = "emailAddress", defaultValue = "") String emailAddress,
            @RequestParam(name = "password", defaultValue = "") String password,
            @RequestParam(name = "repeatPassword", defaultValue = "") String repeatPassword,
            Model model) {
        logger.info("POST /register");

        addUserAttributes(firstName, lastName, noLastName, dateOfBirth, emailAddress, password, repeatPassword, model);

        boolean valid = true;

        if (!Objects.equals(password, repeatPassword)) {
            model.addAttribute("passwordMatchingError", "Passwords do not match");
            valid = false;
        }

        ValidationResult firstNameValidation = InputValidator.validateName(firstName);
        ValidationResult lastNameValidation = InputValidator.validateName(lastName);
        ValidationResult passwordValidation = InputValidator.validatePassword(password);
        ValidationResult emailAddressValidation = InputValidator.validateUniqueEmail(emailAddress);
        ValidationResult dateOfBirthValidation = InputValidator.validateDOB(dateOfBirth);
        if (Objects.equals(dateOfBirth, "")) {
            dateOfBirthValidation = ValidationResult.OK;
        }

        valid = checkAllValid(firstNameValidation, lastNameValidation, String.valueOf(noLastName),
                emailAddressValidation, passwordValidation, dateOfBirthValidation, valid, model);

        if (!valid) {
            return "registrationForm";
        } else {
            LocalDate date;
            if (!Objects.equals(dateOfBirth, "")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
                date = LocalDate.parse(dateOfBirth, formatter);
            } else {
                date = null;
            }

            User user = new User(firstName, lastName, emailAddress, date);
            userService.addUser(user, password);

            setSecurityContext(user.getEmailAddress(), password, request.getSession());
            return "redirect:/profile"; // Placeholder for Profile Page
        }
    }

    /**
     * Function to add user's inputs to the model
     *
     * @param firstName      - user's first name
     * @param lastName       - user's last name
     * @param noLastName     - checkbox for whether user has a last name
     * @param dateOfBirth    - user's date of birth (optional)
     * @param emailAddress   - user's email address
     * @param password       - user's password
     * @param repeatPassword - user's repeating password for confirmation
     * @param model          - (map-like) representation of user's input (above
     *                       parameters)
     */
    private void addUserAttributes(@RequestParam(name = "firstName", defaultValue = "") String firstName,
            @RequestParam(name = "lastName", required = false, defaultValue = "") String lastName,
            @RequestParam(name = "noLastName", required = false, defaultValue = "false") boolean noLastName,
            @RequestParam(name = "dateOfBirth", required = false, defaultValue = "") String dateOfBirth,
            @RequestParam(name = "emailAddress", defaultValue = "") String emailAddress,
            @RequestParam(name = "password", defaultValue = "") String password,
            @RequestParam(name = "repeatPassword", defaultValue = "") String repeatPassword,
            Model model) {
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("noLastName", noLastName);
        model.addAttribute("dateOfBirth", dateOfBirth);
        model.addAttribute("emailAddress", emailAddress);
        model.addAttribute("password", password);
        model.addAttribute("repeatPassword", repeatPassword);
    }

    /**
     * Runs OldValidationResult.isvalid() on all of the user's input
     *
     * @param firstNameValidation    - OldValidationResult for user's first name
     * @param lastNameValidation     - OldValidationResult for user's last name
     * @param noLastName             - boolean checking if user has last name
     * @param emailAddressValidation - OldValidationResult for user's email address
     * @param passwordValidation     - OldValidationResult for user's password
     * @param dateOfBirthValidation  - OldValidationResult for user's DOB
     * @param valid                  - Boolean for if user's input is valid
     * @param model                  - (map-like) representation of user's input
     *                               (above parameters)
     * @return valid
     */
    public Boolean checkAllValid(ValidationResult firstNameValidation,
                                 ValidationResult lastNameValidation,
                                 String noLastName,
                                 ValidationResult emailAddressValidation,
                                 ValidationResult passwordValidation,
                                 ValidationResult dateOfBirthValidation,
                                 boolean valid,
                                 Model model) {
        if (!firstNameValidation.valid()) {
            model.addAttribute("firstNameError", "First Name " +firstNameValidation);
            valid = false;
        }
        if (!lastNameValidation.valid() && !Boolean.parseBoolean(noLastName)) {
            model.addAttribute("lastNameError", "Last Name " + lastNameValidation);
            valid = false;
        }
        if (!emailAddressValidation.valid()) {
            model.addAttribute("emailAddressError", emailAddressValidation);
            valid = false;
        }
        if (!dateOfBirthValidation.valid()) {
            model.addAttribute("dateOfBirthError", dateOfBirthValidation);
            valid = false;
        }

        if (!passwordValidation.valid()) {
            model.addAttribute("passwordError", passwordValidation);
            valid = false;
        }

        return valid;
    }

}