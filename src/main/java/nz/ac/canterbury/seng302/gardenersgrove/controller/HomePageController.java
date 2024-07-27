package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.UserInteraction;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendshipService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;
import nz.ac.canterbury.seng302.gardenersgrove.util.ItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    private GardenService gardenService;

    private FriendshipService friendshipService;

    private SecurityService securityService;

    private UserInteractionService userInteractionService;

    /**
     * Constructor for the HomePageController with {@link Autowired} to connect this
     * controller with other services
     *
     * @param userService
     */
    @Autowired
    public HomePageController(UserService userService, GardenService gardenService, PlantService plantService,
            FriendshipService friendshipService, SecurityService securityService, UserInteractionService userInteractionService) {
        this.userService = userService;
        this.gardenService = gardenService;
        this.plantService = plantService;
        this.friendshipService = friendshipService;
        this.securityService = securityService;
        this.userInteractionService = userInteractionService;
    }

    /**
     * Redirects GET default url '/' to '/home'
     * 
     * @return redirect to /home
     */
    @GetMapping("/")
    public String home() {
        logger.info("GET /");
        return "redirect:./home";
    }

    /**
     * Adds a default user and gardens to the database for testing purposes
     */
    public void addDefautContent() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
        LocalDate date = LocalDate.parse("01/01/2001", formatter);

        // Add a default user to speed up manual testing.
        User johnDoe = new User("John",
                "Doe",
                "gardenersgrovetest@gmail.com",
                date);
        userService.addUser(johnDoe, "Password1!");
        userService.verifyUser(johnDoe);

        for (int i = 1; i < 12; i++) {
            Garden sampleGarden = new Garden(
                    "John's Garden " + i,
                    "Some Description here",
                    "114 Ilam Road",
                    "Ilam",
                    "Christchurch",
                    "8041",
                    "New Zealand",
                    15.0,
                    true,
                    "-43.5214643",
                    "172.5796159",
                    johnDoe);
            sampleGarden = gardenService.addGarden(sampleGarden);

            for (int k = 0; k < 12; k++) {
                plantService.addPlant("Test Plant #" + k, 2,
                        "test", LocalDate.now(), sampleGarden.getGardenId());
            }
        }

        Garden sampleGarden2 = new Garden(
                "John's Private garden ",
                "Some Description here",
                "114 Ilam Road",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                15.0,
                false,
                "-43.5214643",
                "172.5796159",
                johnDoe);

        gardenService.addGarden(sampleGarden2);

        if (!userService.emailInUse("janedoe@email.com")) {

            // Add a default user to speed up manual testing.
            User janeDoe = new User("Jane",
                    "Doe",
                    "janedoe@email.com",
                    date);
            userService.addUser(janeDoe, "Password1!");
            userService.verifyUser(janeDoe);

            for (int i = 0; i < 1; i++) {
                Garden sampleGarden = new Garden(
                        "Jane's Garden " + i,
                        "Some Description here",
                        "114 Ilam Road",
                        "Ilam",
                        "Christchurch",
                        "8041",
                        "New Zealand",
                        15.0,
                        true,
                        "-43.5214643",
                        "172.5796159",
                        janeDoe);
                sampleGarden = gardenService.addGarden(sampleGarden);

                for (int k = 0; k < 1; k++) {
                    plantService.addPlant("Test Plant " + k, 2,
                            "test", LocalDate.now(), sampleGarden.getGardenId());
                }

            }
            Friendship friendship = friendshipService.addFriendship(johnDoe, janeDoe);
            friendshipService.updateFriendShipStatus(friendship.getId(), FriendshipStatus.ACCEPTED);
        }
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

        // Add a test user with test gardens and test plants
        if (!userService.emailInUse("gardenersgrovetest@gmail.com")) {
            addDefautContent();
        }

        User user = securityService.getCurrentUser();

        if (user != null) {
            return loadUserMainPage(user, model);
        }

        return "homePage";
    }


    private List<Garden> getRecentGardens(Long userId){
        List<UserInteraction> gardenInteractions = userInteractionService.getAllUsersUserInteractionsByItemType(userId,ItemType.GARDEN);
        return gardenService.getGardensByInteraction(gardenInteractions);
    }


    private String loadUserMainPage(User user, Model model){

        String username = user.getFirstName() + " " + user.getLastName();
        String profilePicture = user.getProfilePictureFilename();

        List<Garden> recentGardens = getRecentGardens(user.getId());

        model.addAttribute("profilePicture", profilePicture);
        model.addAttribute("username", username);
        model.addAttribute("recentGardens", recentGardens);

        return "mainPage";
    }
}
