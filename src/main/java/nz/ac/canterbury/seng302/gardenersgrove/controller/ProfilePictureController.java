
package nz.ac.canterbury.seng302.gardenersgrove.controller;

import java.io.IOException;
import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.ImageService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;

import org.springframework.ui.Model;

/**
 * This is a basic spring boot controller, note the @link{Controller} annotation
 * which defines this.
 * This controller defines endpoints as functions with specific HTTP mappings
 */
@Controller
public class ProfilePictureController {
    Logger logger = LoggerFactory.getLogger(ProfilePictureController.class);

    private final ImageService imageService;
    private final UserService userService;

    public ProfilePictureController(ImageService imageService, UserService userService) {
        this.imageService = imageService;
        this.userService = userService;
    }

    public String getProfilePictureString(String filename) {
        // Recieve strings of form thing and returns
        // string for the image or default profile picture
        String profilePictureString = "/Images/default_profile_picture.png";

        if (filename != null && filename.length() != 0) {
            profilePictureString = MvcUriComponentsBuilder.fromMethodName(ProfilePictureController.class,
                    "serveFile", filename).build().toUri().toString();
        }
        return profilePictureString;
    }

    /**
     * Gets the login page
     * 
     * @return thymeleaf loginPage
     */
    @GetMapping("/profile_picture")
    public String displayImage(Model model) {
        logger.info("GET /profile_picture");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userService.getUserByEmail(email);

        String filename = user.getProfilePictureFilename();

        String profileImage = getProfilePictureString(filename);

        model.addAttribute("profileImage", profileImage);

        return "profilePicture";
    }

    @PostMapping("/profile_picture")
    public String uploadImage(
            @RequestParam("imageFile") MultipartFile imageFile, Model model)
            throws IOException {
        logger.info("POST /profile_picture");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userService.getUserByEmail(email);

        String fileExtension = imageFile.getOriginalFilename().split("\\.")[1];

        String[] allFiles = imageService.getAllImages();

        // Delete past profile image/s
        for (String file : allFiles) {
            if (file.contains("user_" + user.getId() + "_profile_picture")) {
                imageService.deleteImage(file);
            }
        }

        String fileName = "user_" + user.getId() + "_profile_picture." + fileExtension; 

        userService.updateProfilePictureFilename(fileName, email);

        imageService.saveImage(fileName, imageFile);

        return "redirect:/profile_picture";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws MalformedURLException {
        logger.info("GET /files/" + filename);

        Resource file = imageService.loadImage(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

}