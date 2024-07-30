package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.UserInteraction;
import nz.ac.canterbury.seng302.gardenersgrove.model.FriendModel;
import nz.ac.canterbury.seng302.gardenersgrove.model.RecentGardenModel;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
    private final GardenService gardenService;
    private final FriendshipService friendshipService;
    private final SecurityService securityService;
    private final UserInteractionService userInteractionService;

    private static final int PAGE_SIZE = 5;

    /**
     * Constructor for the HomePageController with {@link Autowired} to connect this
     * controller with other services
     *
     * @param userService
     */
    @Autowired
    public HomePageController(UserService userService, GardenService gardenService, PlantService plantService,
            FriendshipService friendshipService, SecurityService securityService,
            UserInteractionService userInteractionService) {
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

        if (!userService.emailInUse("badguy@email.com")) {
            User badGuy = new User("Bad",
                    "Guy",
                    "badguy@email.com",
                    date);
            userService.addUser(badGuy, "Badguy1!");
            userService.verifyUser(badGuy);
            userService.banUser(badGuy, 1);
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

    private List<Garden> getRecentGardens(Long userId) {
        List<UserInteraction> gardenInteractions = userInteractionService.getAllUsersUserInteractionsByItemType(userId,
                ItemType.GARDEN);
        return gardenService.getGardensByInteraction(gardenInteractions);
    }

    private List<RecentGardenModel> setRecentGardenModels(List<Garden> gardenList) {
        if (gardenList.isEmpty()) {
            return null;
        }
        return gardenList.stream()
                .map(garden -> new RecentGardenModel(garden, garden.getOwner(), securityService.isOwner(garden.getOwner().getId())))
                .collect(Collectors.toList());
    }

    /**
     * Helper function to create a list of friend models. Used for adding to the
     * model of the Manage Friends page.
     * 
     * @param id of user to find recent friends of
     * @return friendModels
     */
    private List<FriendModel> createFriendModel(long id) {
        List<FriendModel> friendModels = new ArrayList<>();
        List<UserInteraction> userInteractions = userInteractionService.getAllUsersUserInteractionsByItemType(id,
                ItemType.USER);
        List<User> recentFriends = userService.getUsersByInteraction(userInteractions);

        for (User friend : recentFriends) {
            String friendProfilePicture = friend.getProfilePictureFilename();
            String friendsName = friend.getFirstName() + " " + friend.getLastName();
            String friendGardenLink = "/" + friend.getId() + "/gardens";
            FriendModel friendModel = new FriendModel(friendProfilePicture, friendsName, friendGardenLink);
            friendModel.setFriendId(friend.getId());
            friendModels.add(friendModel);
        }

        return friendModels;
    }

    private String loadUserMainPage(User user, Model model) {

        String username = user.getFirstName() + " " + user.getLastName();
        String profilePicture = user.getProfilePictureFilename();

        List<RecentGardenModel> recentGardens = setRecentGardenModels(getRecentGardens(user.getId()));

        model.addAttribute("profilePicture", profilePicture);
        model.addAttribute("username", username);

        if (recentGardens != null) {
            List<RecentGardenModel> recentGardensPage1 = recentGardens.subList(0,
                    Math.min(recentGardens.size(), PAGE_SIZE));
            model.addAttribute("recentGardensPage1", recentGardensPage1);

            List<RecentGardenModel> recentGardensPage2 = null;
            if (recentGardens.size() > PAGE_SIZE) {
                recentGardensPage2 = recentGardens.subList(PAGE_SIZE,
                        Math.min(recentGardens.size(), PAGE_SIZE * 2));
            }
            model.addAttribute("recentGardensPage2", recentGardensPage2);
        }

        List<FriendModel> recentFriends = createFriendModel(user.getId());
        List<FriendModel> sublistRecentFriends = recentFriends.subList(0, Math.min(PAGE_SIZE, recentFriends.size()));
        model.addAttribute("recentFriends", sublistRecentFriends);

        return "mainPage";
    }
}
