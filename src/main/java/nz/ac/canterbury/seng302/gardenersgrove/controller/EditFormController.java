package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.ImageService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.InputValidator.FileValidator;
import nz.ac.canterbury.seng302.gardenersgrove.validation.InputValidator.InputValidator;
import nz.ac.canterbury.seng302.gardenersgrove.validation.InputValidator.ValidationResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

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
    private final ImageService imageService;

    /**
     * Constructor for the EditFormController with {@link Autowired} to connect this
     * controller with other services
     * 
     * @param userService
     * @param authenticationManager
     */
    @Autowired
    public EditFormController(UserService userService,
            AuthenticationManager authenticationManager, ImageService imageService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.imageService = imageService;
    }

    /**
     * Gets the resource url for the profile picture, or the default profile picture
     * if the user does not have one
     * 
     * @param filename string filename
     * @return string of the profile picture url
     */
    public String getProfilePictureString(String filename) {

        String profilePictureString = "/Images/default_profile_picture.png";

        if (filename != null && filename.length() != 0) {
            profilePictureString = MvcUriComponentsBuilder.fromMethodName(EditFormController.class,
                    "serveFile", filename).build().toUri().toString();
        }
        return profilePictureString;
    }

    /**
     * Set the security context for the user
     * This method is shared functionality between the login and registration pages
     * possibly should be moved to a different class? As not correct to be here
     * 
     * @param email
     * @param password
     * @param session  http session to set the cookies with the context key
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
        String email = authentication.getName();

        User user = userService.getUserByEmail(email);
        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("emailAddress", user.getEmailAddress());
        String formattedDateOfBirth = "";
        LocalDate dateOfBirth = user.getDateOfBirth();
        if (dateOfBirth != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            formattedDateOfBirth = dateOfBirth.format(formatter);
        }

        model.addAttribute("dateOfBirth", formattedDateOfBirth);
        boolean noLastName = Objects.equals(user.getLastName(), "");
        model.addAttribute("noLastName", noLastName);

        String filename = user.getProfilePictureFilename();
        String profilePicture = getProfilePictureString(filename);
        model.addAttribute("profilePicture", profilePicture);

        return "editProfileForm";
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
            @RequestParam("profilePictureInput") MultipartFile profilePicture,
            Model model) {
        logger.info("POST /edit");

        addUserAttributes(firstName, lastName, noLastName, dateOfBirth, emailAddress, profilePicture, model);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email);

        ValidationResult firstNameValidation = InputValidator.validateName(firstName);
        ValidationResult lastNameValidation = InputValidator.validateName(lastName);
        ValidationResult emailAddressValidation = InputValidator.validateUniqueEmail(emailAddress);
        if (emailAddress.equals(email)) {
            emailAddressValidation = ValidationResult.OK;
        }
        ValidationResult dateOfBirthValidation = InputValidator.validateDOB(dateOfBirth);
        if (Objects.equals(dateOfBirth, "")) {
            dateOfBirthValidation = ValidationResult.OK;
        }

        ValidationResult profilePictureValidation = FileValidator.validateFile(profilePicture);
        if (profilePicture.isEmpty()) {
            profilePictureValidation = ValidationResult.OK;
        }

        boolean valid = checkAllValid(firstNameValidation, lastNameValidation, String.valueOf(noLastName),
                emailAddressValidation, dateOfBirthValidation, profilePictureValidation, model);

        if (!valid) {
            return "editProfileForm";
        }

        // Update user

        if (!profilePicture.isEmpty()) {

            String fileExtension = profilePicture.getOriginalFilename().split("\\.")[1];
            try {
                String[] allFiles = imageService.getAllImages();
                // Delete past profile image/s
                for (String file : allFiles) {
                    if (file.contains("user_" + currentUser.getId() + "_profile_picture")) {
                        imageService.deleteImage(file);
                    }
                }

                String fileName = "user_" + currentUser.getId() + "_profile_picture." + fileExtension;

                userService.updateProfilePictureFilename(fileName, currentUser.getId());

                imageService.saveImage(fileName, profilePicture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } 

        LocalDate date;
        if (!Objects.equals(dateOfBirth, "")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
            date = LocalDate.parse(dateOfBirth, formatter);
        } else {
            date = null;
        }
        User user = new User(firstName, lastName, emailAddress, date);
        userService.updateUser(user, currentUser.getId());

        setSecurityContext(currentUser.getEmailAddress(), currentUser.getEncodedPassword(), request.getSession());

        return "redirect:/profile";

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
            @RequestParam("profilePicture") MultipartFile profilePicture,
            Model model) {
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("noLastName", noLastName);
        model.addAttribute("dateOfBirth", dateOfBirth);
        model.addAttribute("emailAddress", emailAddress);
        model.addAttribute("profilePicture", profilePicture);
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
            ValidationResult profilePictureValidation,
            Model model) {
        boolean valid = true;

        if (!firstNameValidation.valid()) {
            model.addAttribute("firstNameError", "First Name " + firstNameValidation);
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
        if (!profilePictureValidation.valid()) {
            model.addAttribute("profilePictureError", profilePictureValidation);
            valid = false;
        }

        return valid;
    }

    /**
     * Serves the file from the image service
     * 
     * @param filename file to retrieve
     * @return response with the file
     */
    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        logger.info("GET /files/" + filename);

        try {
            Resource file = imageService.loadImage(filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
}