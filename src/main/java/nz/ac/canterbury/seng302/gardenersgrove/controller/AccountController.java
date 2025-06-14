package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.gardenersgrove.component.Constants;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Token;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.model.GardenNavModel;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidation.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a basic spring boot controller for the registration form page,
 * note the {@link Controller} annotation which defines this.
 * This controller defines endpoints as functions with specific HTTP mappings
 */
@Controller
public class AccountController {
    Logger logger = LoggerFactory.getLogger(AccountController.class);

    private final EmailService emailService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final GardenService gardenService;
    private final SecurityService securityService;

    /**
     * Constructor for the RegistrationFormController with {@link Autowired} to
     * connect this
     * controller with other services
     * 
     * @param userService           to use for checking persistence to validate
     *                              email and password
     * @param authenticationManager to login user after registration
     * @param securityService       service to access security methods
     */
    @Autowired
    public AccountController(UserService userService, AuthenticationManager authenticationManager,
            EmailService emailService, TokenService tokenService, GardenService gardenService,
            SecurityService securityService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.gardenService = gardenService;
        this.securityService = securityService;
    }

    /**
     * Set the security context for the user
     * This method is shared functionality between the login and registration pages
     * possibly should be moved to a different class? As not correct to be here
     * 
     * @param email    of user who is registering
     * @param password of user who is registering
     * @param session  http session to set the cookies with the context key
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
     * For the given email address if there is an existing unverified user whose
     * token has expired, delete them and their associated token
     * 
     * @param emailAddress the email address of the user
     */
    public void removeIfExpired(String emailAddress) {

        User existingUser = userService.getUserByEmail(emailAddress);

        if (existingUser != null && !existingUser.isVerified()) {

            Token token = tokenService.getTokenByUser(existingUser);

            if (token != null && token.isExpired()) {
                tokenService.deleteToken(token);
                userService.deleteUser(existingUser);
            }
        }
    }

    /**
     * handles GET '/register' requests
     *
     * @return registration form
     */
    @GetMapping("/register")
    public String registrationForm(@RequestParam(name = Constants.FIRST_NAME_ATTRIBUTE, defaultValue = "") String firstName,
            @RequestParam(name = Constants.LAST_NAME_ATTRIBUTE, required = false, defaultValue = "") String lastName,
            @RequestParam(name = "noLastName", required = false, defaultValue = "false") boolean noLastName,
            @RequestParam(name = Constants.DATE_OF_BIRTH_ATTRIBUTE, required = false) LocalDate dateOfBirth,
            @RequestParam(name = Constants.EMAIL_ATTRIBUTE, defaultValue = "") String emailAddress,
            @RequestParam(name = Constants.PASSWORD_ATTRIBUTE, defaultValue = "") String password,
            @RequestParam(name = "repeatPassword", defaultValue = "") String repeatPassword, Model model) {
        logger.info("GET /register");

        if (securityService.isLoggedIn()) {
            return "redirect:/home";
        }

        model.addAttribute(Constants.FIRST_NAME_ATTRIBUTE, firstName);
        model.addAttribute(Constants.LAST_NAME_ATTRIBUTE, lastName);
        model.addAttribute("noLastName", noLastName);
        model.addAttribute(Constants.DATE_OF_BIRTH_ATTRIBUTE, dateOfBirth);
        model.addAttribute(Constants.EMAIL_ATTRIBUTE, emailAddress);
        model.addAttribute(Constants.PASSWORD_ATTRIBUTE, password);
        model.addAttribute("repeatPassword", repeatPassword);
        return "registrationPage";
    }

    /**
     * Handles POST url '/register' requests, returns the registration form if
     * invalid input
     * or to verification page if valid
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
     * @return registration form or verification page
     */
    @PostMapping("/register")
    public String submitRegistrationForm(HttpServletRequest request,
            @RequestParam(name = Constants.FIRST_NAME_ATTRIBUTE, defaultValue = "") String firstName,
            @RequestParam(name = Constants.LAST_NAME_ATTRIBUTE, required = false, defaultValue = "") String lastName,
            @RequestParam(name = "noLastName", required = false, defaultValue = "false") boolean noLastName,
            @RequestParam(name = Constants.DATE_OF_BIRTH_ATTRIBUTE, required = false) LocalDate dateOfBirth,
            @RequestParam(name = Constants.EMAIL_ATTRIBUTE, defaultValue = "") String emailAddress,
            @RequestParam(name = Constants.PASSWORD_ATTRIBUTE, defaultValue = "") String password,
            @RequestParam(name = "repeatPassword", defaultValue = "") String repeatPassword,
            Model model) {
        logger.info("POST /register");

        // Create a map of validation results for each input
        Map<String, ValidationResult> validationMap = new HashMap<>();

        validationMap.put(Constants.FIRST_NAME_ATTRIBUTE, InputValidator.validateName(firstName));

        ValidationResult lastNameValidation = InputValidator.validateName(lastName);
        if (noLastName) {
            lastNameValidation = ValidationResult.OK;
            lastName = "";
        }
        validationMap.put(Constants.LAST_NAME_ATTRIBUTE, lastNameValidation);

        removeIfExpired(emailAddress);

        validationMap.put(Constants.EMAIL_ATTRIBUTE, InputValidator.validateUniqueEmail(emailAddress));

        ValidationResult dateOfBirthValidation;
        if (dateOfBirth == null) {
            dateOfBirthValidation = ValidationResult.OK;
        } else {
            String dateString = dateOfBirth.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            dateOfBirthValidation = InputValidator.validateDOB(dateString);
        }
        validationMap.put(Constants.DATE_OF_BIRTH_ATTRIBUTE, dateOfBirthValidation);

        List<String> otherFields = new ArrayList<>();
        otherFields.add(firstName);
        if (!noLastName) {
            otherFields.add(lastName);
        }
        if (dateOfBirth != null) {
            otherFields.add(dateOfBirth.toString());
        }
        otherFields.add(emailAddress);
        validationMap.put(Constants.PASSWORD_ATTRIBUTE, InputValidator.validatePassword(password, otherFields));

        // Check that all inputs are valid
        boolean valid = true;
        for (Map.Entry<String, ValidationResult> entry : validationMap.entrySet()) {
            if (!entry.getValue().valid()) {

                String error = entry.getValue().toString();

                if (entry.getKey().equals(Constants.FIRST_NAME_ATTRIBUTE)) {
                    error = "First name " + error;
                } else if (entry.getKey().equals(Constants.LAST_NAME_ATTRIBUTE)) {
                    error = "Last name " + error;
                }

                model.addAttribute(entry.getKey() + "Error", error);
                valid = false;
            }
        }

        if (!password.equals(repeatPassword)) {
            model.addAttribute("passwordMatchingError", "Passwords do not match");
            valid = false;
        }

        if (!valid) {
            model.addAttribute(Constants.FIRST_NAME_ATTRIBUTE, firstName);
            model.addAttribute(Constants.LAST_NAME_ATTRIBUTE, lastName);
            model.addAttribute(Constants.EMAIL_ATTRIBUTE, emailAddress);
            model.addAttribute(Constants.DATE_OF_BIRTH_ATTRIBUTE, dateOfBirth);
            model.addAttribute("noLastName", noLastName);
            return "registrationPage";
        }

        User user = new User(firstName, lastName, emailAddress, dateOfBirth);
        userService.addUser(user, password);

        Token token = new Token(user, null);

        tokenService.addToken(token);
        try {
            emailService.sendRegistrationEmail(token);
        } catch (MessagingException e) {
            logger.error("Couldn't send email to {}", user, e);
        }

        return "redirect:/verify/" + user.getEmailAddress();
    }

    /**
     * Handles GET '/verify/{emailAddress}' requests
     *
     * @param emailAddress - user's email address
     * @param model        - (map-like) representation of user's input (above
     *                     parameters)
     * @return verification page
     */
    @GetMapping("/verify/{emailAddress}")
    public String getVerify(@PathVariable String emailAddress, Model model, HttpServletRequest request) {
        logger.info("GET /verify");

        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if (inputFlashMap != null) {
            model.addAttribute(Constants.MESSAGE_ATTRIBUTE, inputFlashMap.get(Constants.MESSAGE_ATTRIBUTE));
            model.addAttribute(Constants.GOOD_MESSAGE_ATTRIBUTE, inputFlashMap.get(Constants.GOOD_MESSAGE_ATTRIBUTE));
        }

        model.addAttribute(Constants.EMAIL_ATTRIBUTE, emailAddress);

        return "verificationPage";
    }

    /**
     * Handles POST '/verify' requests
     *
     * @param tokenString        - user's token string
     * @param emailAddress       - user's email address
     * @param model              - (map-like) representation of user's input (above
     *                           parameters)
     * @param redirectAttributes - used to pass messages between redirects
     * @return verification page
     */
    @PostMapping("/verify")
    public String postVerify(@RequestParam(name = "tokenString") String tokenString,
            @RequestParam(name = Constants.EMAIL_ATTRIBUTE) String emailAddress, Model model,
            RedirectAttributes redirectAttributes) {
        logger.info("POST /verify");

        Token token = tokenService.getTokenByTokenString(tokenString);

        if (token == null || token.isExpired() || !token.getUser().getEmailAddress().equals(emailAddress)) {
            redirectAttributes.addFlashAttribute(Constants.MESSAGE_ATTRIBUTE, "Signup code invalid");
            redirectAttributes.addFlashAttribute(Constants.GOOD_MESSAGE_ATTRIBUTE, false);
            return "redirect:/verify/" + emailAddress;
        }

        User user = token.getUser();
        userService.verifyUser(user);
        tokenService.deleteToken(token);

        redirectAttributes.addFlashAttribute(Constants.MESSAGE_ATTRIBUTE, "Your account has been activated, please log in");
        redirectAttributes.addFlashAttribute(Constants.GOOD_MESSAGE_ATTRIBUTE, true);
        return "redirect:/login";
    }

    /**
     * Gets the login page
     * 
     * @return thymeleaf loginPage
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(name = Constants.EMAIL_ATTRIBUTE, defaultValue = "") String emailAddress,
            @RequestParam(name = Constants.PASSWORD_ATTRIBUTE, defaultValue = "") String password, Model model,
            HttpServletRequest request) {
        logger.info("GET /login");

        if (securityService.isLoggedIn()) {
            return "redirect:/home";
        }

        // Check if there is a message in the flash map and add it to the model
        // Used for displaying messages after a redirect e.g. from the verify page
        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if (inputFlashMap != null) {
            model.addAttribute(Constants.MESSAGE_ATTRIBUTE, inputFlashMap.get(Constants.MESSAGE_ATTRIBUTE));
            model.addAttribute(Constants.GOOD_MESSAGE_ATTRIBUTE, inputFlashMap.get(Constants.GOOD_MESSAGE_ATTRIBUTE));
        }

        model.addAttribute("validEmail", true);
        model.addAttribute("validLogin", true);
        model.addAttribute(Constants.EMAIL_ATTRIBUTE, emailAddress);
        model.addAttribute(Constants.PASSWORD_ATTRIBUTE, password);

        return Constants.LOGIN_PAGE_ATTRIBUTE;
    }

    /**
     * Handles the login request
     *
     * @param emailAddress the email parameter
     * @return thymeleaf loginPage
     */
    @PostMapping("/login")
    public String handleLoginRequest(HttpServletRequest request, @RequestParam String emailAddress,
            @RequestParam String password, HttpSession session, Model model) {
        logger.info("POST /login");

        removeIfExpired(emailAddress);

        ValidationResult validEmail = InputValidator.validateEmail(emailAddress);

        model.addAttribute(Constants.EMAIL_ATTRIBUTE, emailAddress);
        model.addAttribute(Constants.PASSWORD_ATTRIBUTE, password);

        if (!validEmail.valid()) {
            model.addAttribute("emailAddressError", validEmail);
            return Constants.LOGIN_PAGE_ATTRIBUTE;
        }

        User user = userService.getUserByEmailAndPassword(emailAddress, password);

        if (user == null) {
            model.addAttribute("loginError", "The email address is unknown, or the password is invalid");
            return Constants.LOGIN_PAGE_ATTRIBUTE;
        }

        if (!user.isVerified()) {
            return "redirect:/verify/" + user.getEmailAddress();
        }

        if (user.isBanned()) {
            int banTimeLeft = user.daysUntilUnban();
            String message = "Your account is blocked for " + banTimeLeft + " day" + (banTimeLeft == 1 ? "" : "s")
                    + " due to inappropriate conduct";
            model.addAttribute(Constants.GOOD_MESSAGE_ATTRIBUTE, false);
            model.addAttribute(Constants.MESSAGE_ATTRIBUTE, message);
            return Constants.LOGIN_PAGE_ATTRIBUTE;
        }

        setSecurityContext(emailAddress, password, request.getSession());
        List<Garden> gardens = gardenService.getAllUsersGardens(user.getId());
        List<GardenNavModel> gardenNavModels = new ArrayList<>();
        for (Garden garden : gardens) {
            gardenNavModels.add(new GardenNavModel(garden.getGardenId(), garden.getGardenName()));
        }
        session.setAttribute(Constants.USER_GARDENS_ATTRIBUTE, gardenNavModels);

        return "redirect:/home";

    }

}
