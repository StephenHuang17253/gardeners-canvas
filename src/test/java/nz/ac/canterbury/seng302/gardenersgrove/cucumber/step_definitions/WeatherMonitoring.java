package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import nz.ac.canterbury.seng302.gardenersgrove.component.DailyWeather;
import nz.ac.canterbury.seng302.gardenersgrove.controller.MyGardensController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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


import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doThrow;

@SpringBootTest
public class WeatherMonitoring {
    public static MockMvc MOCK_MVC;

    @Autowired
    public GardenRepository gardenRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationManager authenticationManager;

    public SecurityService securityService;

    private static GardenService gardenService;

    private static UserService userService;

    private static PlantService plantService;

    private static FileService fileService;

    private Map<String, Object> model;

    private List<DailyWeather> weather;

    @Mock
    private WeatherService weatherService;
    @Before
    public void before_or_after_all() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(passwordEncoder, userRepository);
        gardenService = new GardenService(gardenRepository, userService);
        securityService = new SecurityService(userService, authenticationManager);

        MyGardensController myGardensController = new MyGardensController(gardenService, securityService, plantService, fileService, weatherService);
        MOCK_MVC = MockMvcBuilders.standaloneSetup(myGardensController).build();
    }

    @Given("I as user {string} is on my garden details page")
    public void iAsUserIsOnMyGardenDetailsPageFor(String userEmail) throws Exception {
        User user = userService.getUserByEmail(userEmail);

        doThrow(new Error("Weather service error"))
                .when(weatherService)
                .getWeather(user.getGardens().get(0).getGardenLatitude(), user.getGardens().get(0).getGardenLongitude());

        String gardenId = String.valueOf(user.getGardens().get(0).getGardenId());
        MvcResult result = MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .get("/my-gardens/{gardenId}", gardenId)
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        ModelAndView modelAndView = result.getModelAndView();

        model = modelAndView.getModel();
        weather = (List<DailyWeather>) model.get("weather");

    }
    @Then("Current weather for my location is shown")
    public void currentWeatherForMyLocationIsShown(){
        Assertions.assertNotNull(weather);
        Assertions.assertTrue(weather.size() > 0);
        System.out.println("Weather data:" + weather.get(0).getHumidity());
    }

    @Then("Future weather for my location is shown")
    public void futureWeatherForMyLocationIsShown() {
        Assertions.assertNotNull(weather);
        Assertions.assertTrue(weather.size() >= 1);

        // Print weather data
        System.out.println("Weather data:" + weather.get(0).getHumidity());
    }



    @Then("No weather is shown")
    public void noWeatherIsShown() {
        Assertions.assertNotNull(weather);
        Assertions.assertTrue(weather.size() == 1);
        Assertions.assertEquals("Location not found, please update your location to see the weather", weather.get(0).getWeatherError());

    }
}
