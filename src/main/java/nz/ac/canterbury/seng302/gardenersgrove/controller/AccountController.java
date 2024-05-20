package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Token;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.TokenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.ui.Model;

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

    // For development to avoid sending signup emails but print the signup token to
    // the terminal instead, set to true or remove for production
    private final boolean SEND_EMAIL = true;

    /**
     * Constructor for the RegistrationFormController with {@link Autowired} to
     * connect this
     * controller with other services
     * 
     * @param userService           to use for checking persistence to validate
     *                              email and password
     * @param authenticationManager to login user after registration
     */
    @Autowired
    public AccountController(UserService userService, AuthenticationManager authenticationManager,
            EmailService emailService, TokenService tokenService, GardenService gardenService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.gardenService = gardenService;
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
    public String registrationForm(@RequestParam(name = "firstName", defaultValue = "") String firstName,
            @RequestParam(name = "lastName", required = false, defaultValue = "") String lastName,
            @RequestParam(name = "noLastName", required = false, defaultValue = "false") boolean noLastName,
            @RequestParam(name = "dateOfBirth", required = false) LocalDate dateOfBirth,
            @RequestParam(name = "emailAddress", defaultValue = "") String emailAddress,
            @RequestParam(name = "password", defaultValue = "") String password,
            @RequestParam(name = "repeatPassword", defaultValue = "") String repeatPassword, Model model) {
        logger.info("GET /register");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("noLastName", noLastName);
        model.addAttribute("dateOfBirth", dateOfBirth);
        model.addAttribute("emailAddress", emailAddress);
        model.addAttribute("password", password);
        model.addAttribute("repeatPassword", repeatPassword);
        return "registrationForm";
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
            @RequestParam(name = "firstName", defaultValue = "") String firstName,
            @RequestParam(name = "lastName", required = false, defaultValue = "") String lastName,
            @RequestParam(name = "noLastName", required = false, defaultValue = "false") boolean noLastName,
            @RequestParam(name = "dateOfBirth", required = false) LocalDate dateOfBirth,
            @RequestParam(name = "emailAddress", defaultValue = "") String emailAddress,
            @RequestParam(name = "password", defaultValue = "") String password,
            @RequestParam(name = "repeatPassword", defaultValue = "") String repeatPassword,
            Model model) {
        logger.info("POST /register");

        // Create a map of validation results for each input
        Map<String, ValidationResult> validationMap = new HashMap<>();

        validationMap.put("firstName", InputValidator.validateName(firstName));

        ValidationResult lastNameValidation = InputValidator.validateName(lastName);
        if (noLastName) {
            lastNameValidation = ValidationResult.OK;
        }
        validationMap.put("lastName", lastNameValidation);

        removeIfExpired(emailAddress);

        validationMap.put("emailAddress", InputValidator.validateUniqueEmail(emailAddress));

        ValidationResult dateOfBirthValidation;
        if (dateOfBirth == null) {
            dateOfBirthValidation = ValidationResult.OK;
        } else {
            String dateString = dateOfBirth.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            dateOfBirthValidation = InputValidator.validateDOB(dateString);
        }
        validationMap.put("dateOfBirth", dateOfBirthValidation);

        List<String> otherFields = new ArrayList<>();
        otherFields.add(firstName);
        if (noLastName == false) {
            otherFields.add(lastName);
        }
        if (!(dateOfBirth == null)) {
            otherFields.add(dateOfBirth.toString());
        }
        otherFields.add(emailAddress);
        InputValidator.validatePassword(password, otherFields);
        validationMap.put("password", InputValidator.validatePassword(password, otherFields));

        // Check that all inputs are valid
        boolean valid = true;
        for (Map.Entry<String, ValidationResult> entry : validationMap.entrySet()) {
            if (!entry.getValue().valid()) {

                String error = entry.getValue().toString();

                if (entry.getKey().equals("firstName")) {
                    error = "First name " + error;
                } else if (entry.getKey().equals("lastName")) {
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
            model.addAttribute("firstName", firstName);
            model.addAttribute("lastName", lastName);
            model.addAttribute("emailAddress", emailAddress);
            model.addAttribute("dateOfBirth", dateOfBirth);
            model.addAttribute("noLastName", noLastName);
            return "registrationForm";
        }

        User user = new User(firstName, lastName, emailAddress, dateOfBirth);
        userService.addUser(user, password);

        Token token = new Token(user, null);

        tokenService.addToken(token);

        if (SEND_EMAIL) {
            try {
                emailService.sendRegistrationEmail(token);
            } catch (MessagingException e) {
                logger.info("could not send email to " + user.getEmailAddress());
            }
        } else {
            logger.info("Here is the token: " + token.toString());
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
            model.addAttribute("message", inputFlashMap.get("message"));
        }

        model.addAttribute("emailAddress", emailAddress);
        model.addAttribute("loggedIn", false);

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
            @RequestParam(name = "emailAddress") String emailAddress, Model model,
            RedirectAttributes redirectAttributes) {
        logger.info("POST /verify");

        Token token = tokenService.getTokenByTokenString(tokenString);

        if (token == null || token.isExpired() || !token.getUser().getEmailAddress().equals(emailAddress)) {
            redirectAttributes.addFlashAttribute("message", "Signup code invalid");
            return "redirect:/verify/" + emailAddress;
        }

        User user = token.getUser();
        userService.verifyUser(user);
        tokenService.deleteToken(token);

        redirectAttributes.addFlashAttribute("message", "Your account has been activated, please log in");
        return "redirect:/login";
    }

    /**
     * Gets the login page
     * 
     * @return thymeleaf loginPage
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(name = "emailAddress", defaultValue = "") String emailAddress,
            @RequestParam(name = "password", defaultValue = "") String password, Model model,
            HttpServletRequest request) {
        logger.info("GET /login");

        // Check if there is a message in the flash map and add it to the model
        // Used for displaying messages after a redirect e.g. from the verify page
        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if (inputFlashMap != null) {
            model.addAttribute("message", inputFlashMap.get("message"));
        }

        model.addAttribute("validEmail", true);
        model.addAttribute("validLogin", true);
        model.addAttribute("emailAddress", emailAddress);
        model.addAttribute("password", password);
        model.addAttribute("loggedIn", false);

        return "loginPage";
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

        model.addAttribute("emailAddress", emailAddress);
        model.addAttribute("password", password);

        if (!validEmail.valid()) {
            model.addAttribute("emailAddressError", validEmail);
            return "loginPage";
        }

        User user = userService.getUserByEmailAndPassword(emailAddress, password);

        if (user == null) {
            model.addAttribute("loginError", "The email address is unknown, or the password is invalid");
            return "loginPage";
        }

        if (!user.isVerified()) {
            return "redirect:/verify/" + user.getEmailAddress();
        }

        setSecurityContext(emailAddress, password, request.getSession());
        session.setAttribute("userGardens", gardenService.getAllUsersGardens(user.getId()));

        return "redirect:/home";

    }

}
