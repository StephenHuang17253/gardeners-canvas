package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.component.DailyWeather;
import nz.ac.canterbury.seng302.gardenersgrove.component.WeatherResponseData;
import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.model.FriendModel;
import nz.ac.canterbury.seng302.gardenersgrove.model.RecentGardenModel;
import nz.ac.canterbury.seng302.gardenersgrove.model.RecentPlantModel;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;
import nz.ac.canterbury.seng302.gardenersgrove.util.GridItemType;
import nz.ac.canterbury.seng302.gardenersgrove.util.ItemType;
import nz.ac.canterbury.seng302.gardenersgrove.util.PlantCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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
    private final WeatherService weatherService;

    private final GridItemLocationService gridItemLocationService;

    private static final int PAGE_SIZE = 5;

    /**
     * Constructor for the HomePageController with {@link Autowired} to connect this
     * controller with other services
     *
     * @param userService
     */
    @Autowired
    public HomePageController(UserService userService, AuthenticationManager authenticationManager,
                              GardenService gardenService, PlantService plantService,
                              FriendshipService friendshipService, SecurityService securityService, WeatherService weatherService,
                              UserInteractionService userInteractionService,
                              GridItemLocationService gridItemLocationService) {
        this.userService = userService;
        this.gardenService = gardenService;
        this.plantService = plantService;
        this.friendshipService = friendshipService;
        this.securityService = securityService;
        this.userInteractionService = userInteractionService;
        this.weatherService = weatherService;
        this.gridItemLocationService = gridItemLocationService;
    }

    /**
     * Adds the loggedIn attribute to the model for all requests
     *
     * @param model
     */
    @ModelAttribute
    public void addLoggedInAttribute(Model model) {
        model.addAttribute("loggedIn", securityService.isLoggedIn());
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
    public void addDefaultContent() {
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
                Plant newPlant = plantService.addPlant("Test Plant #" + k, 2,
                        "test", LocalDate.now(), sampleGarden.getGardenId(), PlantCategory.TREE);
                GridItemLocation newLocation = new GridItemLocation(
                        newPlant.getPlantId(),
                        GridItemType.PLANT,
                        sampleGarden,
                        k % 5,
                        k / 5);
                gridItemLocationService.addGridItemLocation(newLocation);
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
                            "test", LocalDate.now(), sampleGarden.getGardenId(), PlantCategory.TREE);
                }

            }
            Friendship friendship = friendshipService.addFriendship(janeDoe, johnDoe);
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
            addDefaultContent();
        }

        User user = securityService.getCurrentUser();

        if (user == null) {
            return "homePage";
        }

        String username = user.getFirstName() + " " + user.getLastName();
        String profilePicture = user.getProfilePictureFilename();

        model.addAttribute("profilePicture", profilePicture);
        model.addAttribute("username", username);

        // Add home page layout for user
        HomePageLayout layout = user.getHomePageLayout();
        model.addAttribute("layout", layout);

        // Add recent gardens
        List<RecentGardenModel> recentGardens = createRecentGardenModels(user.getId());

        List<RecentGardenModel> recentGardensPage1 = null;
        if (!recentGardens.isEmpty()) {
            recentGardensPage1 = recentGardens.subList(0,
                    Math.min(recentGardens.size(), PAGE_SIZE));
        }
        model.addAttribute("recentGardensPage1", recentGardensPage1);

        List<RecentGardenModel> recentGardensPage2 = null;
        if (recentGardens.size() > PAGE_SIZE) {
            recentGardensPage2 = recentGardens.subList(PAGE_SIZE,
                    Math.min(recentGardens.size(), PAGE_SIZE * 2));
        }
        model.addAttribute("recentGardensPage2", recentGardensPage2);

        // Check gardens that need watering and add them
        List<Garden> gardens = gardenService.getAllUsersGardens(user.getId());
        List<Garden> gardensNeedWatering = getGardensForWatering(gardens);

        model.addAttribute("gardensNeedWatering", gardensNeedWatering);
        model.addAttribute("gardens", gardens);

        // Add all friend requests
        List<User> pendingFriends = new ArrayList<>();
        List<Friendship> friendships = friendshipService.getAllUsersFriends(user.getId());
        List<User> friends = friendships.stream()
                .map(Friendship::getUser1)
                .toList();

        for (User friend : friends) {
            if (!friend.getId().equals(user.getId())
                    && friendshipService.findFriendship(friend, user).getStatus() == FriendshipStatus.PENDING) {
                pendingFriends.add(friend);
            }
        }

        model.addAttribute("friendRequests", pendingFriends);
        model.addAttribute("notificationMessage", "You have friend requests");

        // Add all recently interated with plants
        List<UserInteraction> plantInteractions = userInteractionService.getAllUsersUserInteractionsByItemType(
                user.getId(),
                ItemType.PLANT);
        List<Plant> recentPlants = plantService.getPlantsByInteraction(plantInteractions);

        List<RecentPlantModel> recentPlantModels = createRecentPlantModels(recentPlants);

        if (!recentPlantModels.isEmpty()) {
            updateModelWithRecentPlants(model, recentPlantModels);
        }

        // Add recently added friends
        List<FriendModel> recentFriends = createFriendModel(user.getId());
        List<FriendModel> sublistRecentFriends = recentFriends.subList(0, Math.min(PAGE_SIZE, recentFriends.size()));
        model.addAttribute("recentFriends", sublistRecentFriends);

        return "mainPage";

    }

    /**
     * creates a list of recent garden models to display
     *
     * @param userId id of current user
     * @return
     */
    private List<RecentGardenModel> createRecentGardenModels(Long userId) {
        List<UserInteraction> gardenInteractions = userInteractionService.getAllUsersUserInteractionsByItemType(userId,
                ItemType.GARDEN);
        List<Garden> gardenList = gardenService.getGardensByInteraction(gardenInteractions);

        return gardenList.stream()
                .map(garden -> new RecentGardenModel(garden, garden.getOwner(),
                        securityService.isOwner(garden.getOwner().getId())))
                .toList();
    }

    /**
     * Sets RecentPlantModels for home page
     *
     * @param plantList List of recently accessed Plant objects
     * @return List of RecentPlantModels
     */
    private List<RecentPlantModel> createRecentPlantModels(List<Plant> plantList) {
        return plantList.stream()
                .map(plant -> new RecentPlantModel(plant, plant.getGarden(), plant.getGarden().getOwner(),
                        securityService.isOwner(plant.getGarden().getOwner().getId())))
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

    /**
     * Helper function to add recent plants to give model
     *
     * @param model        Model to add plants to
     * @param recentPlants List of RecentPlantModels
     */
    private void updateModelWithRecentPlants(Model model, List<RecentPlantModel> recentPlants) {
        List<RecentPlantModel> recentPlantsPage1 = recentPlants.subList(0,
                Math.min(recentPlants.size(), PAGE_SIZE));
        model.addAttribute("recentPlantsPage1", recentPlantsPage1);

        List<RecentPlantModel> recentPlantsPage2 = null;
        if (recentPlants.size() > PAGE_SIZE) {
            recentPlantsPage2 = recentPlants.subList(PAGE_SIZE,
                    Math.min(recentPlants.size(), PAGE_SIZE * 2));
        }
        model.addAttribute("recentPlantsPage2", recentPlantsPage2);
    }

    /**
     * Helper method that retrieves gardens that need watering.
     *
     * @param gardens list of gardens that need to be checked for
     *                watering
     */
    private List<Garden> getGardensForWatering(List<Garden> gardens) {
        if(gardens.isEmpty()){
            return gardens;
        }

        List<Garden> gardensNeedWatering = new ArrayList<>();

        List<WeatherResponseData> weatherDataList = weatherService.getWeatherForGardens(gardens);

        for (int i = 0; i < gardens.size(); i++) {
            Garden garden = gardens.get(i);
            boolean needsWater = gardenNeedsWatering(garden, weatherDataList.get(i));

            if(!Objects.equals(needsWater, garden.getNeedsWatering())) {
                garden.setNeedsWatering(needsWater);
                gardenService.changeGardenNeedsWatering(garden.getGardenId(), needsWater);
            }
            if (needsWater) {
                gardensNeedWatering.add(garden);
            }
        }
        return gardensNeedWatering;
    }

    /**
     * Checks a garden for if it needs watering
     *
     * @param garden      the garden being checked for watering status
     * @param weatherData the weather data being checked for the garden
     * @return boolean for if the garden need watering
     */
    private boolean gardenNeedsWatering(Garden garden, WeatherResponseData weatherData) {
        if (weatherData == null) {
            return false;
        }

        List<DailyWeather> weatherList = weatherData.getRetrievedWeatherData();

        if (weatherList.size() <= 2) {
            return false;
        }

        DailyWeather beforeYesterdayWeather = weatherList.get(0);
        DailyWeather yesterdayWeather = weatherList.get(1);
        DailyWeather currentWeather = weatherList.get(2);

        return garden.getNeedsWatering()
                || beforeYesterdayWeather.getDescription().equals("Sunny") &&
                yesterdayWeather.getDescription().equals("Sunny") &&
                currentWeather.getDescription().equals("Sunny");

    }

    /**
     * Provides the get endpoint for the edit home page form
     *
     * @param model
     * @return
     */
    @GetMapping("home/edit")
    public String editHomePage(Model model) {
        logger.info("GET /home/edit");
        User user = securityService.getCurrentUser();
        HomePageLayout layout = user.getHomePageLayout();
        if (layout == null) {
            HomePageLayout newLayout = new HomePageLayout();
            userService.updateHomePageLayout(user.getId(), newLayout);
            layout = user.getHomePageLayout();
        }
        model.addAttribute("layout", layout);
        return "editHomePage";
    }

    /**
     * This function is called when a POST request is made to /home/edit it handles
     * updating the home page layout
     *
     * @param acceptedFriends show the accepted friends section boolean
     * @param recentPlants    show the recent plants section boolean
     * @param recentGardens   show the recent gardens section boolean
     * @param notifications   show the notifications section boolean
     * @param model           the model to add attributes to
     * @return redirect to /home
     */
    @PostMapping("home/edit")
    public String saveHomePage(
            @RequestParam(name = "acceptedFriends", required = false, defaultValue = "false") boolean acceptedFriends,
            @RequestParam(name = "recentPlants", required = false, defaultValue = "false") boolean recentPlants,
            @RequestParam(name = "recentGardens", required = false, defaultValue = "false") boolean recentGardens,
            @RequestParam(name = "notifications", required = false, defaultValue = "false") boolean notifications,
            Model model) {
        logger.info("POST /home/edit");
        User user = securityService.getCurrentUser();
        HomePageLayout newLayout = new HomePageLayout(acceptedFriends, recentPlants, recentGardens,
                notifications);
        userService.updateHomePageLayout(user.getId(), newLayout);
        return "redirect:/home";
    }

}
