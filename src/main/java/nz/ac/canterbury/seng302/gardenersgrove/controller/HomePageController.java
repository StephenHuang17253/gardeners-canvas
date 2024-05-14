package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    private final PlantService plantService;
    private boolean onStart = false;

    private GardenService gardenService;

    /**
     * Constructor for the HomePageController with {@link Autowired} to connect this
     * controller with other services
     *
     * @param userService
     * @param authenticationManager
     */
    @Autowired
    public HomePageController(UserService userService, AuthenticationManager authenticationManager, GardenService gardenService, PlantService plantService) {
        this.userService = userService;
        this.gardenService = gardenService;
        this.plantService = plantService;
    }

    /**
     * Redirects GET default url '/' to '/home'
     * 
     * @return redirect to /home
     * @throws IOException
     */
    @GetMapping("/")
    public String home() throws IOException {
        logger.info("GET /");

        return "redirect:./home";
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

        if (filename != null && filename.length() != 0) {
            profilePictureString = MvcUriComponentsBuilder.fromMethodName(ProfileController.class,
                    "serveFile", filename).build().toUri().toString();
        }
        return profilePictureString;
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

        model.addAttribute("myGardens", gardenService.getGardens());

        // Add a test user with test gardens and test plants
        if (!userService.emailInUse("gardenersgrovetest@gmail.com")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
            LocalDate date = LocalDate.parse("01/01/2001", formatter);

            // Add a default user to speed up manual testing.
            User johnDoe = new User("John",
                    "Doe",
                    "gardenersgrovetest@gmail.com",
                    date);
            userService.addUser(johnDoe, "Password1!");
            userService.verifyUser(johnDoe);
            onStart = true;

            ArrayList<Plant> samplePlants = new ArrayList<>();

            for (int i = 0; i < 12; i++) {
                Garden sampleGarden = new Garden(
                        "John's Garden " + i,
                        "114 Ilam Road",
                        "Ilam",
                        "Christchurch",
                        "8041",
                        "New Zealand",
                        15.0,
                        johnDoe);
                sampleGarden = gardenService.addGarden(sampleGarden);

                for(int k = 0; k < 12; k++)
                {
                    plantService.addPlant("Test Plant #" + k,2,
                            "test", LocalDate.now(),sampleGarden.getGardenId());
                }

            }
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
        String profilePicture = "";

        if (loggedIn) {
            User user = userService.getUserByEmail(authentication.getName());
            if (user != null) {
                welcomeString = "Welcome " + user.getFirstName() + " " + user.getLastName();
                String filename = user.getProfilePictureFilename();
                profilePicture = getProfilePictureString(filename);
            }
        }

        model.addAttribute("profilePicture", profilePicture);

        model.addAttribute("username", welcomeString);

        return "homePage";
    }
}
