package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidator.InputValidator;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidator.ValidationResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * This is a basic spring boot controller for the edit form page,
 * note the {@link Controller} annotation which defines this.
 * This controller defines endpoints as functions with specific HTTP mappings
 */
@Controller
public class EditFormController {

    Logger logger = LoggerFactory.getLogger(EditFormController.class);
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    /**
     * Constructor for the EditFormController with {@link Autowired} to connect this
     * controller with other services
     * 
     * @param userService
     * @param authenticationManager
     */
    @Autowired
    public EditFormController(UserService userService,
            AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Set the security context for the user
     * This method is shared functionality between the login and registration pages
     * possibly should be moved to a different class? As not correct to be here
     * 
     * @param email
     * @param password
     * @param session http session to set the cookies with the context key
     */
    public void setSecurityContext(String email, String password, HttpSession session) {
        logger.info(email);
        User user = userService.getUserByEmail(email);

        logger.info(user.toString());

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getEmailAddress(),
                user.getEncodedPassword());

        logger.info(token.toString());

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
     * Redirects GET url '/edit' to the edit form
     * 
     * @param model - (map-like) representation of user's input (above parameters)
     * @return redirect to edit form with prefilled details
     */
    @GetMapping("/edit")
    public String editForm(Model model) {
        logger.info("GET /edit");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        User u = userService.getUserByEmail(currentPrincipalName);
        model.addAttribute("firstName", u.getFirstName());
        model.addAttribute("lastName", u.getLastName());
        model.addAttribute("emailAddress", u.getEmailAddress());
        String formattedDateOfBirth = "";
        LocalDate dateOfBirth = u.getDateOfBirth();
        if (dateOfBirth != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            formattedDateOfBirth = dateOfBirth.format(formatter);
        }

        model.addAttribute("dateOfBirth", formattedDateOfBirth);
        boolean noLastName = Objects.equals(u.getLastName(), "");
        model.addAttribute("noLastName", noLastName);
        return "editForm";
    }

    /**
     * Redirects POST url '/edit' to the edit form if invalid input
     * or to user's profile page if edit completed
     *
     * @param firstName    - user's first name
     * @param lastName     - user's last name
     * @param noLastName   - checkbox for whether user has a last name
     * @param dateOfBirth  - user's date of birth (optional)
     * @param emailAddress - user's email address
     * @param model        - (map-like) representation of user's input (above
     *                     parameters)
     * @return redirect to edit form or to profile page
     */
    @PostMapping("/edit")
    public String submitEditForm(HttpServletRequest request,
            @RequestParam(name = "firstName", defaultValue = "") String firstName,
            @RequestParam(name = "lastName", required = false, defaultValue = "") String lastName,
            @RequestParam(name = "noLastName", required = false, defaultValue = "false") boolean noLastName,
            @RequestParam(name = "dateOfBirth", required = false, defaultValue = "") String dateOfBirth,
            @RequestParam(name = "emailAddress", defaultValue = "") String emailAddress,
            Model model) {
        logger.info("POST /edit");

        addUserAttributes(firstName, lastName, noLastName, dateOfBirth, emailAddress, model);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        User currentUser = userService.getUserByEmail(currentPrincipalName);

        ValidationResult firstNameValidation = InputValidator.validateName(firstName);
        ValidationResult lastNameValidation = InputValidator.validateName(lastName);
        ValidationResult emailAddressValidation = InputValidator.validateUniqueEmail(emailAddress);
        if (emailAddress.equals(currentPrincipalName)) {
            emailAddressValidation = ValidationResult.OK;
        }
        ValidationResult dateOfBirthValidation = InputValidator.validateDOB(dateOfBirth);
        if (Objects.equals(dateOfBirth, "")) {
            dateOfBirthValidation = ValidationResult.OK;
        }

        boolean valid = checkAllValid(firstNameValidation, lastNameValidation, String.valueOf(noLastName),
                emailAddressValidation, dateOfBirthValidation, model);

        if (!valid) {
            return "editForm";
        } else {
            LocalDate date;
            if (!Objects.equals(dateOfBirth, "")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
                date = LocalDate.parse(dateOfBirth, formatter);
            } else {
                date = null;
            }
            User user = new User(firstName, lastName, emailAddress, date);
            userService.updateUser(user, currentUser.getId());

            logger.info("got to setting context");
            setSecurityContext(currentUser.getEmailAddress(), currentUser.getEncodedPassword(), request.getSession());

            System.out.println("User updated");
            System.out.println(SecurityContextHolder.getContext().getAuthentication().getName());

            return "redirect:/profile";
        }
    }

    /**
     * Function to add user's inputs to the model
     *
     * @param firstName    - user's first name
     * @param lastName     - user's last name
     * @param noLastName   - checkbox for whether user has a last name
     * @param dateOfBirth  - user's date of birth (optional)
     * @param emailAddress - user's email address
     * @param model        - (map-like) representation of user's input (above
     *                     parameters)
     */
    private void addUserAttributes(@RequestParam(name = "firstName", defaultValue = "") String firstName,
            @RequestParam(name = "lastName", required = false, defaultValue = "") String lastName,
            @RequestParam(name = "noLastName", required = false, defaultValue = "false") boolean noLastName,
            @RequestParam(name = "dateOfBirth", required = false, defaultValue = "") String dateOfBirth,
            @RequestParam(name = "emailAddress", defaultValue = "") String emailAddress,
            Model model) {
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("noLastName", noLastName);
        model.addAttribute("dateOfBirth", dateOfBirth);
        model.addAttribute("emailAddress", emailAddress);
    }

    /**
     * Runs OldValidationResult.isvalid() on all the user's input
     *
     * @param firstNameValidation    - OldValidationResult for user's first name
     * @param lastNameValidation     - OldValidationResult for user's last name
     * @param noLastName             - boolean checking if user has last name
     * @param emailAddressValidation - OldValidationResult for user's email address
     * @param dateOfBirthValidation  - OldValidationResult for user's DOB
     * @param model                  - (map-like) representation of user's input
     *                               (above parameters)
     * @return valid
     */
    public Boolean checkAllValid(ValidationResult firstNameValidation,
                                 ValidationResult lastNameValidation,
                                 String noLastName,
                                 ValidationResult emailAddressValidation,
                                 ValidationResult dateOfBirthValidation,
                                 Model model) {
        boolean valid = true;

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

        return valid;
    }

}