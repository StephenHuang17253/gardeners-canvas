package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import nz.ac.canterbury.seng302.gardenersgrove.component.DailyWeather;
import nz.ac.canterbury.seng302.gardenersgrove.component.WeatherResponseData;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardensController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendshipService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.service.WeatherService;


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

    @Autowired
    public FriendshipRepository friendshipRepository;

    public SecurityService securityService;

    private static GardenService gardenService;

    private static UserService userService;

    private static PlantService plantService;

    private static FileService fileService;

    private static FriendshipService friendshipService;

    private Map<String, Object> model;

    private List<DailyWeather> weather;

    private MvcResult mvcResult;

    private String weatherDescription;

    @MockBean
    private WeatherService weatherService;

    private List<DailyWeather> pastWeather;
    private String mockResponse ="{\n" +
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

    @Before
    public void before_or_after_all() throws IOException, InterruptedException {
        userService = new UserService(passwordEncoder, userRepository);
        gardenService = new GardenService(gardenRepository, userService);
        friendshipService = new FriendshipService(friendshipRepository, userService);
        securityService = new SecurityService(userService, authenticationManager, friendshipService);
        weatherService = mock(WeatherService.class);
        GardensController myGardensController = new GardensController(gardenService, securityService, plantService, fileService, weatherService);
        MOCK_MVC = MockMvcBuilders.standaloneSetup(myGardensController).build();



    }

    @Given("I as user {string} is on my garden details page")
    public void iAsUserIsOnMyGardenDetailsPageFor(String userEmail) throws Exception {
        User user = userService.getUserByEmail(userEmail);

        String gardenId = String.valueOf(user.getGardens().get(0).getGardenId());
        MvcResult result = MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .get("/my-gardens/{gardenId}", gardenId)
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        ModelAndView modelAndView = result.getModelAndView();

        mvcResult = result;
        model = modelAndView.getModel();
        weather = (List<DailyWeather>) model.get("weather");

    }

    @Then("Current weather for my location is shown")
    public void currentWeatherForMyLocationIsShown(){
        Assertions.assertNotNull(weather.get(0));
        Assertions.assertEquals("9.6", weather.get(0).getTemp());
        Assertions.assertEquals("92", weather.get(0).getHumidity());
        Assertions.assertEquals("0.0", weather.get(0).getPrecipitation());
        Assertions.assertNull( weather.get(0).getWeatherError());
        System.out.println(model);
    }

    @Then("Future weather for my location is shown")
    public void futureWeatherForMyLocationIsShown() {
        Assertions.assertNotNull(weather);
        Assertions.assertEquals(6, weather.size());
        Assertions.assertNull(weather.get(1).getTemp());
        Assertions.assertNull(weather.get(1).getHumidity());
        Assertions.assertEquals("1.4", weather.get(1).getPrecipitation());
        Assertions.assertEquals("11.1", weather.get(1).getMaxTemp());
        Assertions.assertEquals("7.8", weather.get(1).getMinTemp());
        Assertions.assertNull(weather.get(0).getWeatherError());
        System.out.println(model);

    }

    @Given("My garden is not set to a location that the location service can find")
    public void myGardenIsNotSetToALocationThatTheLocationServiceCanFind() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonObject = objectMapper.readTree(mockResponse);
        WeatherResponseData weatherData = new WeatherResponseData(jsonObject);
        when(weatherService.getWeather(anyString(), anyString())).thenReturn(weatherData);
    }

    @Given("My garden is not set to a location that the location service can not find")
    public void myGardenIsNotSetToALocationThatTheLocationServiceCanNotFind() {
        WeatherResponseData mockResponseData = mock(WeatherResponseData.class);
        Mockito.when(mockResponseData.getCurrentWeather()).thenThrow(new NullPointerException("No such location"));
        Mockito.when(mockResponseData.getForecastWeather()).thenThrow(new NullPointerException("No such location"));
        Mockito.when(weatherService.getWeather(Mockito.anyString(),Mockito.anyString())).thenReturn(mockResponseData);
    }

    @Then("A Weather error message tells me “Location not found, please update your location to see the weather”")
    public void aWeatherErrorMessageTellsMeLocationNotFoundPleaseUpdateYourLocationToSeeTheWeather() {
        System.out.println(model);
        Assertions.assertNotNull(weather.get(0));
        Assertions.assertEquals("Location not found, please update your location to see the weather", weather.get(0).getWeatherError());
    }


    @Given("The past two days have been sunny in my location")
    public void thePastTwoDaysHaveBeenSunnyInMyLocation() throws Exception {
        WeatherResponseData mockedWeatherData = mock(WeatherResponseData.class);

        DailyWeather sunnyWeatherToday = new DailyWeather("sunny.png", LocalDate.now(), "Sunny");
        DailyWeather sunnyWeatherYesterday = new DailyWeather("sunny.png", LocalDate.now().minusDays(1), "Sunny");
        DailyWeather sunnyWeatherBeforeYesterday = new DailyWeather("sunny.png", LocalDate.now().minusDays(2), "Sunny");

        List<DailyWeather> pastWeather = new ArrayList<>();
        pastWeather.add(sunnyWeatherBeforeYesterday);
        pastWeather.add(sunnyWeatherYesterday);

        Mockito.when(mockedWeatherData.getPastWeather()).thenReturn(pastWeather);
        Mockito.when(mockedWeatherData.getCurrentWeather()).thenReturn(sunnyWeatherToday);
        Mockito.when(weatherService.getWeather(anyString(), anyString())).thenReturn(mockedWeatherData);
    }

    @Given("The current weather is rainy")
    public void theCurrentWeatherIsRainy() {
        WeatherResponseData mockedWeatherData = mock(WeatherResponseData.class);

        DailyWeather rainyWeatherToday = new DailyWeather("rainy.png", LocalDate.now(), "Rainy");
        DailyWeather sunnyWeatherYesterday = new DailyWeather("sunny.png", LocalDate.now().minusDays(1), "Sunny");
        DailyWeather sunnyWeatherBeforeYesterday = new DailyWeather("sunny.png", LocalDate.now().minusDays(2), "Sunny");

        List<DailyWeather> pastWeather = new ArrayList<>();
        pastWeather.add(sunnyWeatherBeforeYesterday);
        pastWeather.add(sunnyWeatherYesterday);

        Mockito.when(mockedWeatherData.getPastWeather()).thenReturn(pastWeather);
        Mockito.when(mockedWeatherData.getCurrentWeather()).thenReturn(rainyWeatherToday);
        Mockito.when(weatherService.getWeather(anyString(), anyString())).thenReturn(mockedWeatherData);
    }

    @Then("An element tells me {string}")
    public void anElementTellsMe(String message) {
        String modelMessage = (String) model.get("message");
        Assertions.assertEquals(modelMessage, message);
    }
}
