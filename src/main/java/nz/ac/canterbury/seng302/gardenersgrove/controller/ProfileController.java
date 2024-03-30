package nz.ac.canterbury.seng302.gardenersgrove.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.fileValidation.FileType;
import nz.ac.canterbury.seng302.gardenersgrove.validation.fileValidation.FileValidator;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidation.InputValidator;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;

/**
 * Controller for the profile and edit profile pages, handles viewing and
 * updating the profile
 */
@Controller
public class ProfileController {

    Logger logger = LoggerFactory.getLogger(ProfileController.class);
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final FileService fileService;

    /**
     * Constructor for the ProfileController with {@link Autowired} to connect this
     * controller with services
     * 
     * @param userService
     * @param authenticationManager
     * @param fileService
     */
    @Autowired
    public ProfileController(UserService userService,
            AuthenticationManager authenticationManager, FileService fileService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.fileService = fileService;
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
    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        logger.info("GET /files/" + filename);
        try {
            Resource file = fileService.loadFile(filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        } catch (MalformedURLException error) {
            error.printStackTrace();
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
        String fileExtension = profilePicture.getOriginalFilename().split("\\.")[1];
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
            error.printStackTrace();
        }
    }

    /**
     * This function is called when a GET request is made to /profile
     * 
     * @param model
     * @return The profilePage html page
     */
    @GetMapping("/profile")
    public String profile(Model model) {
        logger.info("GET /profile");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String currentEmail = authentication.getName();

        User user = userService.getUserByEmail(currentEmail);
        String userName = user.getFirstName() + " " + user.getLastName();

        String filename = user.getProfilePictureFilename();
        String profilePicture = getProfilePictureString(filename);
        model.addAttribute("profilePicture", profilePicture);
        model.addAttribute("userName", userName);
        model.addAttribute("dateOfBirth", user.getDateOfBirth());
        model.addAttribute("emailAddress", user.getEmailAddress());

        return "profilePage";
    }

    /**
     * This function is called when a POST request is made to /profile
     * Gets authenticated user and updates their profile picture
     * 
     * @param profilePicture user's profile picture
     * @param model
     * @return
     */
    @PostMapping("/profile")
    public String updateProfilePicture(
            @RequestParam("profilePictureInput") MultipartFile profilePicture,
            Model model) {
        logger.info("POST /profile");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);

        ValidationResult profilePictureValidation = FileValidator.validateImage(profilePicture, 10, FileType.IMAGES);
        if (profilePicture.isEmpty()) {
            profilePictureValidation = ValidationResult.OK;
        }

        if (!profilePictureValidation.valid()) {

            
            model.addAttribute("profilePictureError", profilePictureValidation);
            String userName = user.getFirstName() + " " + user.getLastName();
            model.addAttribute("userName", userName);
            model.addAttribute("dateOfBirth", user.getDateOfBirth());
            model.addAttribute("emailAddress", user.getEmailAddress());
            String filename = user.getProfilePictureFilename();
            String currentProfilePicture = getProfilePictureString(filename);
            model.addAttribute("profilePicture", currentProfilePicture);
            return "profilePage";
        }

        updateProfilePicture(user, profilePicture);

        return "redirect:/profile";
    }

    /**
     * This function is called when a GET request is made to /profile/edit
     * 
     * @param model
     * @return The profileEditPage html page
     */
    @GetMapping("/profile/edit")
    public String editForm(Model model) {
        logger.info("GET /profile/edit");
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

        boolean noLastName = user.getLastName().equals("");
        model.addAttribute("noLastName", noLastName);

        String filename = user.getProfilePictureFilename();
        String profilePicture = getProfilePictureString(filename);
        model.addAttribute("profilePicture", profilePicture);

        return "editProfileForm";
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
            @RequestParam(name = "dateOfBirth", required = false, defaultValue = "") String dateOfBirth,
            @RequestParam(name = "emailAddress") String emailAddress,
            @RequestParam("profilePictureInput") MultipartFile profilePicture,
            Model model) {
        logger.info("GET /profile/edit");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();
        User currentUser = userService.getUserByEmail(currentEmail);

        // Create a map of validation results for each input
        Map<String, ValidationResult> validationMap = new HashMap<>();

        validationMap.put("firstName", InputValidator.validateName(firstName));
        ValidationResult lastNameValidation = InputValidator.validateName(lastName);
        if (noLastName) {
            lastNameValidation = ValidationResult.OK;
        }
        validationMap.put("lastName", lastNameValidation);
        ValidationResult emailAddressValidation = InputValidator.validateUniqueEmail(emailAddress);
        if (emailAddress.equals(currentEmail)) {
            emailAddressValidation = ValidationResult.OK;
        }
        validationMap.put("emailAddress", emailAddressValidation);
        ValidationResult dateOfBirthValidation = InputValidator.validateDOB(dateOfBirth);
        if (dateOfBirth.equals("")) {
            dateOfBirthValidation = ValidationResult.OK;
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

                if (entry.getKey().equals("firstName")) {
                    error = "First name " + error;
                } else if (entry.getKey().equals("lastName")) {
                    error = "First name " + error;
                }

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
            String profilePictureString = getProfilePictureString(filename);
            model.addAttribute("profilePicture", profilePictureString);
            return "editProfileForm";
        }

        // Update the user's profile
        if (!profilePicture.isEmpty()) {
            updateProfilePicture(currentUser, profilePicture);
        }

        LocalDate newDateOfBirth = null;
        if (!dateOfBirth.equals("")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
            newDateOfBirth = LocalDate.parse(dateOfBirth, formatter);
        }

        userService.updateUser(currentUser.getId(), firstName, lastName, emailAddress, newDateOfBirth);

        setSecurityContext(currentUser.getEmailAddress(), currentUser.getEncodedPassword(), request.getSession());

        return "redirect:/profile";
    }


    @GetMapping("/profile/validate")
    public String validateProfile(Model model) {
        logger.info("GET /profile/validate");

        return "validateProfile";
    }
}
