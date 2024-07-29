package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.UnavailableException;
import nz.ac.canterbury.seng302.gardenersgrove.component.DailyWeather;
import nz.ac.canterbury.seng302.gardenersgrove.component.WeatherResponseData;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.entity.UserInteraction;
import nz.ac.canterbury.seng302.gardenersgrove.model.RecentGardenModel;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.*;

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

    private static int PAGE_SIZE = 5;

    private final WeatherService weatherService;


    /**
     * Constructor for the HomePageController with {@link Autowired} to connect this
     * controller with other services
     *
     * @param userService
     */
    @Autowired
    public HomePageController(UserService userService, AuthenticationManager authenticationManager,
            GardenService gardenService, PlantService plantService,
            FriendshipService friendshipService, SecurityService securityService, WeatherService weatherService, UserInteractionService userInteractionService) {
        this.userService = userService;
        this.gardenService = gardenService;
        this.plantService = plantService;
        this.friendshipService = friendshipService;
        this.securityService = securityService;
        this.userInteractionService = userInteractionService;
        this.weatherService = weatherService;
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
    public String home(Model model) throws UnavailableException {

        logger.info("GET /home");

        model.addAttribute("myGardens", gardenService.getGardens());

        // Add a test user with test gardens and test plants
        if (!userService.emailInUse("gardenersgrovetest@gmail.com")) {
            addDefaultContent();
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

    private List<RecentGardenModel> setRecentGardenModels(List<Garden> gardenList) {
        if(gardenList.isEmpty()){
            return null;
        }
        return gardenList.stream()
                .map(garden -> new RecentGardenModel(garden, garden.getOwner(), securityService.isOwner(garden.getOwner().getId())))
                .collect(Collectors.toList());
    }


    private String loadUserMainPage(User user, Model model) throws UnavailableException {

        String username = user.getFirstName() + " " + user.getLastName();
        String profilePicture = user.getProfilePictureFilename();

        List<RecentGardenModel> recentGardens = setRecentGardenModels(getRecentGardens(user.getId()));

        model.addAttribute("profilePicture", profilePicture);
        model.addAttribute("username", username);
        if (recentGardens == null) {
            return "mainPage";
        }
        if (recentGardens.size() < PAGE_SIZE) {
            model.addAttribute("recentGardensPage1", recentGardens.subList(0, recentGardens.size()));
        } else {
            model.addAttribute("recentGardensPage1", recentGardens.subList(0, 5));
        }
        if (recentGardens.size() > PAGE_SIZE) {
            List<RecentGardenModel> recentGardensPage2 = recentGardens.subList(PAGE_SIZE, Math.min(PAGE_SIZE * 2, recentGardens.size()));
            model.addAttribute("recentGardensPage2", recentGardensPage2);
        } else {
            model.addAttribute("recentGardensPage2", null);
            boolean loggedIn = securityService.isLoggedIn();

            if (loggedIn) {
                user = securityService.getCurrentUser();
                username = user.getFirstName() + " " + user.getLastName();
                profilePicture = user.getProfilePictureFilename();

                List<Garden> gardens = gardenService.getAllUsersGardens(user.getId());
                List<Garden> gardensNeedWatering = new ArrayList<>();
                List<WeatherResponseData> weatherDataList = weatherService.getWeatherForGardens(gardens);

                Map<Long, WeatherResponseData> gardenWeatherMap = new HashMap<>();
                for (int i = 0; i < gardens.size(); i++) {
                    gardenWeatherMap.put(gardens.get(i).getGardenId(), weatherDataList.get(i));
                }

                for (Garden garden : gardens) {
                    WeatherResponseData weatherData = gardenWeatherMap.get(garden.getGardenId());
                    if (weatherData != null) {
                        List<DailyWeather> weatherList = weatherData.getRetrievedWeatherData();
                        if (weatherList.size() > 1) {
                            DailyWeather beforeYesterdayWeather = weatherList.get(0);
                            DailyWeather yesterdayWeather = weatherList.get(1);
                            if (Objects.equals(beforeYesterdayWeather.getDescription(), "Sunny")
                                    && Objects.equals(yesterdayWeather.getDescription(), "Sunny")) {
                                gardensNeedWatering.add(garden);
                            }
                        }
                    }
                }
                model.addAttribute("gardensNeedWatering", gardensNeedWatering);
                model.addAttribute("profilePicture", profilePicture);
                model.addAttribute("username", username);
                model.addAttribute("gardens", gardens);
                model.addAttribute("gardenWeatherMap", gardenWeatherMap);
            }
        }

        return "mainPage";
    }
}
