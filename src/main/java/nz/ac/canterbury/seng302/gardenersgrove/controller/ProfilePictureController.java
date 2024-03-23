
package nz.ac.canterbury.seng302.gardenersgrove.controller;

import java.io.IOException;
import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import nz.ac.canterbury.seng302.gardenersgrove.service.ImageService;

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

    public ProfilePictureController(ImageService imageService) {
        this.imageService = imageService;
    }

    public String getProfilePictureString(String filename) {
        // Recieve strings of form thing and returns
        // string for the image or default profile picture
        String profilePictureString = "/images/default_profile_picture.png";

        if (filename.length() != 0) {

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
    public String displayImage(@RequestParam(name = "filename", defaultValue = "") String filename, Model model) {
        logger.info("GET /profile_picture");

        String profileImage = getProfilePictureString(filename);

        model.addAttribute("profileImage", profileImage);

        return "profilePicture";
    }

    @PostMapping("/profile_picture")
    public String uploadImage(
            @RequestParam("imageFile") MultipartFile imageFile, Model model)
            throws IOException {
        logger.info("POST /profile_picture");

        String fileName = imageFile.getOriginalFilename();

        imageService.saveImage(fileName, imageFile);

        return "redirect:/profile_picture?filename=" + fileName;
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