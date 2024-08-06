package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.servlet.UnavailableException;
import nz.ac.canterbury.seng302.gardenersgrove.component.DailyWeather;
import nz.ac.canterbury.seng302.gardenersgrove.component.WeatherResponseData;
import nz.ac.canterbury.seng302.gardenersgrove.controller.PublicGardensController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.model.FriendModel;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.ItemType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import io.cucumber.java.Before;
import nz.ac.canterbury.seng302.gardenersgrove.controller.HomePageController;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.HomePageLayoutRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;

import java.io.File;
import java.time.LocalDate;
import java.util.*;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class U25_MainPage {

    @Autowired
    public GardenRepository gardenRepository;

    @Autowired
    public PlantRepository plantRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public FriendshipRepository friendshipRepository;

    @Autowired
    public HomePageLayoutRepository homePageLayoutRepository;

    @Autowired
    public SecurityService securityService;

    @Autowired
    public FileService fileService;

    public static GardenService gardenService;

    public static UserService userService;

    public static PlantService plantService;

    private static FriendshipService friendshipService;

    @Autowired
    public WeatherService weatherService;

    public static AuthenticationManager authenticationManager;

    @Autowired
    public UserInteractionService userInteractionService;

    public MockMvc mockMVC;

    private MvcResult mvcResult;

    private Map<String, Object> model;

    private String drySampleWeather = "{\n" +
            "  \"latitude\": -43.5,\n" +
            "  \"longitude\": 172.625,\n" +
            "  \"generationtime_ms\": 0.07200241088867188,\n" +
            "  \"utc_offset_seconds\": 0,\n" +
            "  \"timezone\": \"GMT\",\n" +
            "  \"timezone_abbreviation\": \"GMT\",\n" +
            "  \"elevation\": 7.0,\n" +
            "  \"current_units\": {\n" +
            "    \"time\": \"iso8601\",\n" +
            "    \"interval\": \"seconds\",\n" +
            "    \"temperature_2m\": \"°C\",\n" +
            "    \"relative_humidity_2m\": \"%\",\n" +
            "    \"precipitation\": \"mm\",\n" +
            "    \"weather_code\": \"wmo code\"\n" +
            "  },\n" +
            "  \"current\": {\n" +
            "    \"time\": \"2024-05-21T06:15\",\n" +
            "    \"interval\": 900,\n" +
            "    \"temperature_2m\": 9.6,\n" +
            "    \"relative_humidity_2m\": 92,\n" +
            "    \"precipitation\": 0.00,\n" +
            "    \"weather_code\": 3\n" +
            "  },\n" +
            "  \"daily_units\": {\n" +
            "    \"time\": \"iso8601\",\n" +
            "    \"weather_code\": \"wmo code\",\n" +
            "    \"temperature_2m_max\": \"°C\",\n" +
            "    \"temperature_2m_min\": \"°C\",\n" +
            "    \"precipitation_sum\": \"mm\"\n" +
            "  },\n" +
            "  \"daily\": {\n" +
            "    \"time\": [\n" +
            "      \"2024-05-19\",\n" +
            "      \"2024-05-20\",\n" +
            "      \"2024-05-21\",\n" +
            "      \"2024-05-22\",\n" +
            "      \"2024-05-23\",\n" +
            "      \"2024-05-24\",\n" +
            "      \"2024-05-25\",\n" +
            "      \"2024-05-26\",\n" +
            "      \"2024-05-27\"\n" +
            "    ],\n" +
            "    \"weather_code\": [45, 63, 80, 80, 45, 45, 3, 3, 61],\n" +
            "    \"temperature_2m_max\": [11.1, 12.3, 10.3, 11.1, 11.2, 11.0, 13.1, 9.7, 10.9],\n" +
            "    \"temperature_2m_min\": [7.0, 8.2, 7.9, 7.8, 2.8, 3.5, 7.7, 2.5, 6.6],\n" +
            "    \"precipitation_sum\": [0.00, 16.50, 1.00, 1.40, 0.00, 0.00, 0.00, 0.00, 4.20]\n" +
            "  }\n" +
            "}\n";

    private Garden targetGarden;

    Logger logger = LoggerFactory.getLogger(U25_MainPage.class);
    // Setup
    @Before
    public void before_or_after_all() {

        userService = new UserService(passwordEncoder, userRepository, homePageLayoutRepository);
        gardenService = new GardenService(gardenRepository, userService);
        plantService = new PlantService(plantRepository, gardenService, fileService);
        friendshipService = new FriendshipService(friendshipRepository, userService);

        HomePageController homePageController = new HomePageController(userService, authenticationManager, gardenService, plantService, friendshipService, securityService, weatherService, userInteractionService);

        // Allows us to bypass spring security
        mockMVC = MockMvcBuilders
                .standaloneSetup(homePageController)
                .build();
    }

    @Given("I am a user with email {string} and no recently added friends")
    public void i_am_a_user_with_email_and_no_recently_added_friends(String email) {
        User newUser = userService.getUserByEmail(email);
        Long userId = newUser.getId();
        assertNotNull(newUser);
        List<Friendship> allFriendships = friendshipService.getAllUsersFriends(newUser.getId());
        for (Friendship friendship : allFriendships) {
            Long friendId = friendship.getUser1().getId();
            if (userId.equals(friendId)) {
                friendId = friendship.getUser2().getId();
            }
            userInteractionService.removeUserInteraction(userId, friendId, ItemType.USER);
            userInteractionService.removeUserInteraction(friendId, userId, ItemType.USER);
            friendshipService.deleteFriendship(friendship.getId());
        }

        List<Friendship> emptyFriendships = friendshipService.getAllUsersFriends(newUser.getId());
        assertTrue(emptyFriendships.isEmpty());
    }

    @When("I look at the recent friends list on the home page")
    public void i_look_at_the_recent_friends_list_on_the_home_page() throws Exception {
        mvcResult = mockMVC.perform(get("/home")).andExpect(status().isOk()).andReturn();
    }

    @Then("I see that my friends with emails {string} and {string} are listed in order")
    public void i_see_that_my_friends_with_emails_and_are_listed_in_order(String email1, String email2) {
        ModelAndView model = mvcResult.getModelAndView();
        Assertions.assertNotNull(model);
        List<FriendModel> friendList = (List<FriendModel>) model.getModelMap().getAttribute("recentFriends");
        User user1 = userService.getUserByEmail(email1);
        User user2 = userService.getUserByEmail(email2);
        assertNotNull(friendList);
        assertEquals(2, friendList.size());
        // Most to least recent
        assertEquals(friendList.get(0).getFriendId(), user2.getId());
        assertEquals(friendList.get(1).getFriendId(), user1.getId());
    }

    @Then("There are no recently accessed friends")
    public void there_are_no_recently_accessed_friends() {
        ModelAndView model = mvcResult.getModelAndView();
        Assertions.assertNotNull(model);
        List<FriendModel> friendList = (List<FriendModel>) model.getModelMap().getAttribute("recentFriends");
        assertNotNull(friendList);
        assertTrue(friendList.isEmpty());
    }


    @And("I am on the home page")
    public void iAmOnTheHomePage() throws Exception {

        mvcResult = mockMVC.perform(
                        MockMvcRequestBuilders
                                .get("/home"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        Assertions.assertNotNull(modelAndView);

        model = modelAndView.getModel();
    }

    @When("I have garden called {string} that needs watering for user {string}")
    public void iHaveGardenThatNeedsWateringForUser(String gardenName, String email) throws Exception {
        User user = userService.getUserByEmail(email);
        Garden targetGarden = new Garden(gardenName, "This garden is thirsty", "", "", "", "", "", 0.0, false, "",
                "", user);
        gardenService.addGarden(targetGarden);

        targetGarden.setNeedsWatering(true);
        Optional<Garden> testGarden = gardenService.getGardenById(targetGarden.getGardenId());
        gardenService.changeGardenNeedsWatering(testGarden.get().getGardenId(), true);

        mvcResult = mockMVC.perform(get("/home")).andExpect(status().isOk()).andReturn();
    }

    @Then("I can see that {string} need watering in the watering notifications for {string}")
    public void iCanSeeTheNamesAndGardensOfPlantsThatNeedWatering(String gardenName, String email) throws Exception {
        User user = userService.getUserByEmail(email);
        List<Garden> gardensNeedWatering = (List<Garden>) mvcResult.getModelAndView().getModelMap().getAttribute("gardensNeedWatering");
        assertNotNull(gardensNeedWatering, "The list of gardens that need water should not be null");
        assertEquals(gardensNeedWatering.get(0).toString(), user.getGardens().get(0).toString());
    }

    @Then("I can see a button and message that says {string}")
    public void iCanSeeAButtonAndMessageThatSays(String message) throws Exception {
        assertEquals(message, model.get("notificationMessage"));
    }

    @Then("I see my recently accessed gardens, friends list, and notifications")
    public void iSeeMyRecentlyAccessedGardensFriendsListAndNotifications() {
        assertTrue(model.containsKey("recentFriends"));
        assertTrue(model.containsKey("notificationMessage"));
        assertTrue(model.containsKey("recentGardensPage2"));
    }

    @Then("I see my most recently accessed gardens")
    public void iSeeMyTopMostRecentlyAccessedGardens() {
        assertTrue(model.containsKey("recentGardensPage1"));
    }

}
