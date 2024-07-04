package nz.ac.canterbury.seng302.gardenersgrove.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;
import nz.ac.canterbury.seng302.gardenersgrove.validation.fileValidation.FileType;
import nz.ac.canterbury.seng302.gardenersgrove.validation.fileValidation.FileValidator;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidation.InputValidator;

/**
 * Controller for the profile and edit profile pages, handles viewing and
 * updating the profile
 */
@Controller
public class ProfileController {

    Logger logger = LoggerFactory.getLogger(ProfileController.class);
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final FileService fileService;
    private final EmailService emailService;

    /**
     * Constructor for the ProfileController with {@link Autowired} to connect this
     * controller with services
     * 
     * @param userService service to access repository
     * @param authenticationManager manager for user's authentication details
     * @param fileService service to manage files
     */
    @Autowired
    public ProfileController(AuthenticationManager authenticationManager,
                             UserService userService,
                             FileService fileService,
                             EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.fileService = fileService;
        this.emailService = emailService;
    }

    /**
     * Gets the resource url for the profile picture, or the default profile picture
     * if the user does not have one
     * 
     * @param filename string filename
     * @return string of the profile picture url
     */
    public String getProfilePictureString(String filename) {

        String profilePictureString = "/images/default_profile_picture.png";

        if (filename != null && !filename.isEmpty()) {
            profilePictureString = MvcUriComponentsBuilder
                    .fromMethodName(ProfileController.class, "serveFile", filename)
                    .build()
                    .toUri()
                    .toString();
        }
        return profilePictureString;
    }

    /**
     * Serves the file from the file service
     * 
     * @param filename file to retrieve
     * @return response with the file
     */
    @GetMapping("/files/users/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        logger.info("GET /files/users/" + filename);
        try {
            Resource file = fileService.loadFile(filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        } catch (MalformedURLException error) {
            logger.error("File url incorrect", error);
        }
        return null;

    }

    /**
     * Set the security context for the user
     * This method is shared functionality between the login and registration pages
     * possibly should be moved to a different class? As not correct to be here
     * 
     * @param email    email of the user
     * @param password password of the user
     * @param session  http session to set the cookies with the context key
     */
    public void setSecurityContext(String email, String password, HttpSession session) {
        User user = userService.getUserByEmail(email);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getEmailAddress(),
                user.getEncodedPassword());

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
     * Update the user's profile picture
     * 
     * @param user           user to update
     * @param profilePicture new profile picture
     */
    public void updateProfilePicture(User user, MultipartFile profilePicture) {
        String fileExtension = Objects.requireNonNull(profilePicture.getOriginalFilename()).split("\\.")[1];
        try {
            String[] allFiles = fileService.getAllFiles();
            // Delete past profile image/s
            for (String file : allFiles) {
                if (file.contains("user_" + user.getId() + "_profile_picture")) {
                    fileService.deleteFile(file);
                }
            }

            String fileName = "user_" + user.getId() + "_profile_picture." + fileExtension.toLowerCase();
            userService.updateProfilePictureFilename(fileName, user.getId());
            fileService.saveFile(fileName, profilePicture);

        } catch (IOException error) {
            logger.error("incorrect user or profilePicture file", error);
        }
    }

    /**
     * This function is called when a GET request is made to /profile
     * 
     * @param model contains all fields
     * @return The profilePage html page
     */
    @GetMapping("/profile")
    public String profile(Model model) {
        logger.info("GET /profile");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);

        String currentEmail = authentication.getName();

        User user = userService.getUserByEmail(currentEmail);
        String userName = user.getFirstName() + " " + user.getLastName();

        String filename = user.getProfilePictureFilename();
        model.addAttribute("profilePicture", filename);
        model.addAttribute("userName", userName);

        String formattedDateOfBirth = "";
        LocalDate dateOfBirth = user.getDateOfBirth();
        if (dateOfBirth != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            formattedDateOfBirth = dateOfBirth.format(formatter);
        }
        model.addAttribute("dateOfBirth", formattedDateOfBirth);
        model.addAttribute("emailAddress", user.getEmailAddress());

        logger.info(filename);

        return "profilePage";
    }

    /**
     * This function is called when a POST request is made to /profile
     * Gets authenticated user and updates their profile picture
     * 
     * @param profilePicture user's profile picture
     * @param model contains all field data
     * @return redirect to profile page
     */
    @PostMapping("/profile")
    public String updateProfilePicture(
            @RequestParam("profilePictureInput") MultipartFile profilePicture,
            Model model) {
        logger.info("POST /profile");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);

        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);

        ValidationResult profilePictureValidation = FileValidator.validateImage(profilePicture, 10, FileType.IMAGES);

        if (!profilePictureValidation.valid()) {
            String userName = user.getFirstName() + " " + user.getLastName();
            String filename = user.getProfilePictureFilename();
            model.addAttribute("userName", userName);
            model.addAttribute("dateOfBirth", user.getDateOfBirth());
            model.addAttribute("emailAddress", user.getEmailAddress());
            model.addAttribute("profilePicture", filename);
            model.addAttribute("profilePictureError", profilePictureValidation);
            return "profilePage";
        }
        updateProfilePicture(user, profilePicture);

        return "redirect:/profile";
    }

    /**
     * This function is called when a GET request is made to /profile/edit
     * 
     * @param model contains all field data
     * @return The profileEditPage html page
     */
    @GetMapping("/profile/edit")
    public String editProfile(Model model) {
        logger.info("GET /profile/edit");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);

        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("emailAddress", user.getEmailAddress());
        model.addAttribute("dateOfBirth", user.getDateOfBirth());

        boolean noLastName = user.getLastName().isEmpty();
        model.addAttribute("noLastName", noLastName);

        String filename = user.getProfilePictureFilename();
        model.addAttribute("profilePicture", filename);

        return "editProfilePage";
    }

    /**
     * Redirects POST url '/profile/edit' to the edit form if invalid input
     * or to user's profile page '/profile' if edit completed
     *
     * @param firstName      - user's first name
     * @param lastName       - user's last name
     * @param noLastName     - checkbox for whether user has a last name
     * @param dateOfBirth    - user's date of birth (optional)
     * @param emailAddress   - user's email address
     * @param profilePicture - user's profile picture
     * @param model          - (map-like) representation of user's input (above
     *                       parameters)
     * @return redirect to edit form or to profile page
     */
    @PostMapping("/profile/edit")
    public String editProfile(HttpServletRequest request,
            @RequestParam(name = "firstName") String firstName,
            @RequestParam(name = "lastName", required = false, defaultValue = "") String lastName,
            @RequestParam(name = "noLastName", required = false, defaultValue = "false") boolean noLastName,
            @RequestParam(name = "dateOfBirth", required = false) LocalDate dateOfBirth,
            @RequestParam(name = "emailAddress") String emailAddress,
            @RequestParam(name = "profilePictureInput", required = false) MultipartFile profilePicture,
            Model model) {
        logger.info("GET /profile/edit");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();
        User currentUser = userService.getUserByEmail(currentEmail);


        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);

        // Create a map of validation results for each input
        Map<String, ValidationResult> validationMap = new HashMap<>();

        validationMap.put("firstName", InputValidator.validateName(firstName));
        ValidationResult lastNameValidation = InputValidator.validateName(lastName);
        if (noLastName) {
            lastNameValidation = ValidationResult.OK;
            lastName = "";
        }
        validationMap.put("lastName", lastNameValidation);
        ValidationResult emailAddressValidation = InputValidator.validateUniqueEmail(emailAddress);
        if (emailAddress.equals(currentEmail)) {
            emailAddressValidation = ValidationResult.OK;
        }
        validationMap.put("emailAddress", emailAddressValidation);
        ValidationResult dateOfBirthValidation;
        if (dateOfBirth == null) {
            dateOfBirthValidation = ValidationResult.OK;
        } else {
            String dateString = dateOfBirth.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            dateOfBirthValidation = InputValidator.validateDOB(dateString);
        }
        validationMap.put("dateOfBirth", dateOfBirthValidation);

        ValidationResult profilePictureValidation = FileValidator.validateImage(profilePicture, 10, FileType.IMAGES);
        if (profilePicture.isEmpty()) {
            profilePictureValidation = ValidationResult.OK;
        }
        validationMap.put("profilePicture", profilePictureValidation);

        // Check that all inputs are valid
        boolean valid = true;
        for (Map.Entry<String, ValidationResult> entry : validationMap.entrySet()) {
            if (!entry.getValue().valid()) {

                String error = entry.getValue().toString();

                error = switch (entry.getKey()) {
                    case "firstName" -> "First name " + error;
                    case "lastName" -> "Last name " + error;
                    case "emailAddress" -> "Email address " + error;
                    default -> entry.getValue().toString();
                };

                model.addAttribute(entry.getKey() + "Error", error);
                valid = false;
            }
        }


        // If any input is invalid, return to the edit form
        if (!valid) {
            model.addAttribute("firstName", firstName);
            model.addAttribute("lastName", lastName);
            model.addAttribute("emailAddress", emailAddress);
            model.addAttribute("dateOfBirth", dateOfBirth);
            model.addAttribute("noLastName", noLastName);
            String filename = currentUser.getProfilePictureFilename();
            model.addAttribute("profilePicture", filename);
            return "editProfilePage";
        }

        // Update the user's profile
        if (!profilePicture.isEmpty()) {
            updateProfilePicture(currentUser, profilePicture);
        }

        userService.updateUser(currentUser.getId(), firstName, lastName, emailAddress, dateOfBirth);

        setSecurityContext(currentUser.getEmailAddress(), currentUser.getEncodedPassword(), request.getSession());
        return "redirect:/profile";
    }

    /**
     * This function is called when a GET request is made to /profile/editPassword and processes authentication
     *
     * @param model          - (map-like) representation of user's input
     * @return redirect to editPasswordForm form
     */
    @GetMapping("profile/editPassword")
    public String editPassword(Model model,
                               @RequestParam(name = "currentPassword", required = false) String currentPassword,
                               @RequestParam(name = "newPassword", required = false) String newPassword,
                               @RequestParam(name = "retypePassword", required = false) String retypePassword
                                )
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);
        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email);

        model.addAttribute("currentPassword", currentPassword);
        model.addAttribute("newPassword", newPassword);
        model.addAttribute("retypePassword", retypePassword);

        logger.info("GET profile/editPassword");
        return "editPasswordPage";
    }


    /**
     * Redirects POST url '/profile/editPassword' to the edit form if invalid input
     * or to user's profile page '/profile' if edit completed
     *
     * @param currentPassword      - user's current password
     * @param newPassword       - new password user would like to change to
     * @param retypePassword     - retyped password to see if matches new password
     * @param model          - (map-like) representation of user's input (above
     *                       parameters)
     * @return redirect to editpassword form or to profile page
     */
    @PostMapping("/profile/editPassword")
    public String editProfile(HttpServletRequest request,
                              @RequestParam(name = "currentPassword") String currentPassword,
                              @RequestParam(name = "newPassword") String newPassword,
                              @RequestParam(name = "retypePassword") String retypePassword,
                              Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);
        String currentEmail = authentication.getName();
        User currentUser = userService.getUserByEmail(currentEmail);
        String firstName = currentUser.getFirstName();
        String lastName = currentUser.getLastName();
        boolean noLastName = false;
        if (lastName == null) {
            noLastName = true;
        }
        LocalDate dateOfBirth = currentUser.getDateOfBirth();


        boolean valid = true;

        if (!userService.checkPassword(currentUser.getId(), currentPassword)) {
            model.addAttribute("currentPasswordError", "Your old password is incorrect");
            valid = false;
        }


        if (!newPassword.equals(retypePassword)) {
            model.addAttribute("passwordMatchingError", "The new passwords do not match");
            valid = false;
        }

        List<String> otherFields = new ArrayList<>();
        otherFields.add(firstName);
        if (noLastName == false) {
            otherFields.add(lastName);
        }
        if (!(dateOfBirth == null)) {
            otherFields.add(dateOfBirth.toString());
        }
        otherFields.add(currentEmail);
        ValidationResult passwordValidation = InputValidator.validatePassword(newPassword, otherFields);;

        if (!passwordValidation.valid()) {
            model.addAttribute("passwordError", passwordValidation);
            valid = false;
        }

        if (!valid) {
            model.addAttribute("currentPassword", currentPassword);
            model.addAttribute("newPassword", newPassword);
            model.addAttribute("retypePassword", retypePassword);
            return "editPasswordPage";
        }

        // Update user's password
        userService.updatePassword(currentUser.getId(), newPassword);

        try {
            logger.info("Attempting to send confirmation email");
            emailService.sendPasswordResetConfirmationEmail(currentUser);
            logger.info("Password update confirmation email was sent");
        } catch (MessagingException e) {
            logger.error("Password update confirmation email not sent");
        }

        return "redirect:/profile";
    }

}
