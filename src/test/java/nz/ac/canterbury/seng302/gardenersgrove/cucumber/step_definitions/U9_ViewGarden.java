package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ModelMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.component.WeatherResponseData;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardensController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;


@SpringBootTest
public class U9_ViewGarden {
    public static MockMvc mockMVC;

    @Autowired
    public GardenRepository gardenRepository;

    @Autowired
    public FriendshipRepository friendshipRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationManager authenticationManager;

    @Autowired
    public UserInteractionService userInteractionService;

    @Autowired
    public ObjectMapper objectMapper;

    public SecurityService securityService;

    private static GardenService gardenService;

    private static UserService userService;

    private static PlantService plantService;

    private static FriendshipService friendshipService;

    @Mock
    private WeatherService weatherService;

    private ResultActions resultActions;

    private MvcResult mvcResult;

    @Before
    public void before_or_after_all() throws JsonProcessingException {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(passwordEncoder, userRepository);
        gardenService = new GardenService(gardenRepository, userService);
        friendshipService = new FriendshipService(friendshipRepository, userService);
        securityService = new SecurityService(userService, authenticationManager, friendshipService, userInteractionService);

        GardensController gardensController = new GardensController(gardenService, securityService, plantService, weatherService,objectMapper);
        mockMVC = MockMvcBuilders.standaloneSetup(gardensController).build();
        securityService = new SecurityService(userService, authenticationManager, friendshipService,userInteractionService);
        weatherService = Mockito.mock(WeatherService.class);

        GardensController myGardensController = new GardensController(gardenService, securityService, plantService, weatherService,objectMapper);
        mockMVC = MockMvcBuilders.standaloneSetup(myGardensController).build();

        String mockResponse ="{\n" +
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
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonObject = objectMapper.readTree(mockResponse);
        WeatherResponseData weatherData = new WeatherResponseData(jsonObject);

        when(weatherService.getWeather(anyString(), anyString())).thenReturn(weatherData);

    }

    @Given("I as user {string} is on my garden details page for {string}")
    public void iAsUserIsOnMyGardenDetailsPageFor(String userEmail, String gardenName) throws Exception {
        User user = userService.getUserByEmail(userEmail);

        String gardenId = String.valueOf(user.getGardens().get(0).getGardenId());
        mockMVC.perform(
                get("/my-gardens/{gardenId}", gardenId)).andExpect(MockMvcResultMatchers.status().isOk());

    }


    @When("I try to visit user {string}'s garden, {string}")
    public void iTryToVisitGarden(String userEmail, String gardenName) throws Exception {
        User user = userService.getUserByEmail(userEmail);
        resultActions = mockMVC.perform(
                get("/my-gardens/{gardenId}", user.getGardens().get(0).getGardenId()));
    }

    @Then("I am unable to visit the page")
    public void iAmUnableToVisitThePage() throws Exception {
        resultActions.andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Then("I am able to visit the page")
    public void iAmAbleToVisitThePage() throws Exception {
        mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    @And("The garden's name {string} and location {string}, {string} are visible")
    public void theNameLocationAndOptionallySizeAreVisible(String gardenName, String gardenCity, String gardenCountry){
        ModelMap modelMap = mvcResult.getModelAndView().getModelMap();
        Assertions.assertEquals(modelMap.getAttribute("gardenName"), gardenName);
        Assertions.assertEquals(modelMap.getAttribute("gardenLocation"), gardenCity + ", " + gardenCountry);

    }
}
