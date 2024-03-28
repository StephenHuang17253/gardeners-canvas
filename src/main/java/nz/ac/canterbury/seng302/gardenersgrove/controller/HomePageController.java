package nz.ac.canterbury.seng302.gardenersgrove.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.mail.MessagingException;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * This is a basic spring boot controller for the home page,
 * note the {@link Controller} annotation which defines this.
 * This controller defines endpoints as functions with specific HTTP mappings
 */
@Controller
public class HomePageController {
    Logger logger = LoggerFactory.getLogger(HomePageController.class);
    private final UserService userService;

    private boolean onStart = false;

    /**
     * Constructor for the HomePageController with {@link Autowired} to connect this
     * controller with other services
     *
     * @param userService
     * @param authenticationManager
     * @param emailService // Added email service
     */
    @Autowired
    public HomePageController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
    }

    /**
     * Redirects GET default url '/' to '/home'
     * 
     * @return redirect to /home
     * @throws MessagingException 
     */
    @GetMapping("/")
    public String home() throws MessagingException {
        logger.info("GET /");

        return "redirect:./home";
    }
    
    /**
     * This function is called when a GET request is made to /home
     * 
     * @param model
     * @return The homePage html page
     */
    @GetMapping("/home")
    public String home(Model model) {

        logger.info("GET /home");

        if (!onStart) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
            LocalDate date = LocalDate.parse("01/01/2001", formatter);
            userService.addUser(new User("John",
                    "Doe",
                    "johndoe@email.com",
                    date),
                    "DefaultUser10!");
            onStart = true;
        }

        // If no users exist then clear the security context,
        // useful for testing without persistent storage,
        // otherwise a user can be logged in without being in the database
        if (userService.getAllUsers().isEmpty()) {
            SecurityContextHolder.clearContext();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";

        model.addAttribute("loggedIn", loggedIn);

        String welcomeString = "";

        if (loggedIn) {
            User user = userService.getUserByEmail(authentication.getName());
            if (user != null) {
                welcomeString = "Welcome " + user.getFirstName() + " " + user.getLastName();
            }
        }

        model.addAttribute("username", welcomeString);

        return "homePage";
    }
}
