package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Objects;

/**
 * This is a basic spring boot controller for the profile page,
 * note the {@link Controller} annotation which defines this.
 * This controller defines endpoints as functions with specific HTTP mappings
 */
@Controller
public class ProfilePageController {

    Logger logger = LoggerFactory.getLogger(ProfilePageController.class);
    private final UserService userService;

    /**
     * Constructor for the ProfilePageController with {@link Autowired} to connect this
     * controller with other services
     * 
     * @param userService
     */
    @Autowired
    public ProfilePageController(UserService userService) {
        this.userService = userService;
    }

    /**
     * This function is called when a GET request is made to /profile
     * @param model
     * @return The profilePage html page
     */
    @GetMapping("/profile")
    public String profile(Model model) {
        logger.info("GET /profile");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && !Objects.equals(authentication.getName(), "anonymousUser");
        boolean noDoB = false;

        if (loggedIn) {
            User user = userService.getUserByEmail(authentication.getName());
            if (user != null) {
                String userName = user.getFirstName() + " " + user.getLastName();
                if (user.getDateOfBirth() == null) {
                    noDoB = true;
                }
                model.addAttribute("userName", userName);

                model.addAttribute("noDoB", noDoB);
                model.addAttribute("dateOfBirth", user.getDateOfBirth());

                model.addAttribute("emailAddress", user.getEmailAddress());

            }
        }
        return "profilePage";
    }
}
