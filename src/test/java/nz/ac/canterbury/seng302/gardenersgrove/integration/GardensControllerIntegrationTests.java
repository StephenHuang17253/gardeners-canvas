package nz.ac.canterbury.seng302.gardenersgrove.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nz.ac.canterbury.seng302.gardenersgrove.component.DailyWeather;
import nz.ac.canterbury.seng302.gardenersgrove.component.WeatherResponseData;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.service.WeatherService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GardensControllerIntegrationTests {
    @Autowired
    private GardenService gardenService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private PlantService plantService;
    @MockBean
    WeatherService weatherService;
    private final MockMvc mockMvc;
    private List<Garden> gardenList = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
    LocalDate date = LocalDate.parse("01/01/2001", formatter);

    @Autowired
    public GardensControllerIntegrationTests(MockMvc mockMvc){
        this.mockMvc = mockMvc;
    }

    @BeforeEach
    void ClearRepository_AddUsersAndGardens() {
        gardenList = new ArrayList<>();
        userRepository.deleteAll();
        User user1 = new User("John","Doe","johnDoe@email.com", date);
        User user2 = new User("Jane","Doe","janeDoe@email.com", date);
        User user3 = new User("Bruce","Wayne","bruceWyane@email.com", date);
        userService.addUser(user1,"1es1P@ssword");
        userService.addUser(user2,"1es1P@ssword");
        userService.addUser(user3,"1es1P@ssword");
        Garden garden1 = new Garden(
                "John's Garden",
                "114 Ilam Road",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                10.0,
                false,
                "-43.5214643",
                "172.5796159",
                user1);
        Garden garden2 = new Garden(
                "Jane's Garden",
                "20 Kirkwood Avenue",
                "Upper Riccarton",
                "Christchurch",
                "8041",
                "New Zealand",
                20.0,
                false,
                "-43.5214643",
                "172.5796159",
                user2);
        gardenService.addGarden(garden1);
        gardenService.addGarden(garden2);
        plantService.addPlant("Java Tree",1,"Grows Java Plums",date,garden2.getGardenId());
        plantService.addPlant("Java Tree",1,"Grows Java Plums",date,garden2.getGardenId());
        gardenList.add(gardenService.getGardenById(1L).get());
        gardenList.add(gardenService.getGardenById(2L).get());

    }

    @Test
    public void GetMyGardens_UserNotAuthorized_Return403() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
    @Test
    @WithMockUser(username = "johnDoe@email.com")
    public void GetMyGardens_UserAuthorized_Return200() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userGardens", gardenService.getAllUsersGardens(1L));
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens").session(session))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username = "johnDoe1234@email.com")
    public void GetAnIndividualGarden_GardenHasBadLocationValue_Return200ButHasWeatherError() throws Exception {

        WeatherResponseData mockResponseData = Mockito.mock(WeatherResponseData.class);
        Mockito.when(mockResponseData.getCurrentWeather()).thenThrow(new NullPointerException("No such location"));
        Mockito.when(mockResponseData.getForecastWeather()).thenThrow(new NullPointerException("No such location"));
        Mockito.when(weatherService.getWeather(Mockito.anyString(),Mockito.anyString())).thenReturn(mockResponseData);

        User user1 = new User("John","Doe","johnDoe1234@email.com", date);
        userService.addUser(user1,"1es1P@ssword");
        User addedUser = userService.getUserByEmail("johnDoe1234@email.com");
        Garden garden1 = new Garden(
                "John's Garden",
                "Some Fake Address",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                10.0,
                false,
                "-43.5214643",
                "172.5796159",
                addedUser);
        Garden addedGarden = gardenService.addGarden(garden1);

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/my-gardens/{gardenId}", addedGarden.getGardenId())
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        String expectedErrorMessage = "Location not found, please update your location to see the weather";
        Assertions.assertEquals(expectedErrorMessage ,((List<DailyWeather>) result.getModelAndView().getModel().get("weather")).get(0).getWeatherError());
    }

    @Test
    @WithMockUser(username = "johnDoe1234@email.com")
    public void GetAnIndividualGarden_GardenHasGoodLocationValue_Return200WithGoodWeatherInfo() throws Exception {

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

        User user1 = new User("John","Doe","johnDoe1234@email.com", date);
        userService.addUser(user1,"1es1P@ssword");
        User addedUser = userService.getUserByEmail("johnDoe1234@email.com");
        Garden garden1 = new Garden(
                "John's Garden",
                "Some Real Address",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                10.0,
                false,
                "-43.5214643",
                "172.5796159",
                addedUser);
        Garden addedGarden = gardenService.addGarden(garden1);

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/my-gardens/{gardenId}", addedGarden.getGardenId())
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        String expectedErrorMessage = "Location not found, please update your location to see the weather";
        Assertions.assertNull(((List<DailyWeather>) result.getModelAndView().getModel().get("weather")).get(0).getWeatherError());
    }
    @Test
    @WithAnonymousUser
    public void GetGardenDetailsPage_UserNotAuthenticated_Return403() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/1"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
    @Test
    @WithMockUser(username = "janeDoe@email.com")
    public void GetGardenDetailsPage_UserNotAuthorizedAndGardenDoesNotExist_Return404() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/4"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    @Test
    @WithMockUser(username = "bruceWyane@email.com")
    public void GetGardenDetailsPage_UserNotAuthorizedAndGardenExists_Return403() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/1"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
    @Test
    @WithMockUser(username = "johnDoe@email.com")
    public void GetGardenDetailsPage_UserAuthorizedAndGardenExists_Return200() throws Exception {
        Garden garden = gardenList.get(0);
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("gardenName", is(garden.getGardenName())))
                .andExpect(MockMvcResultMatchers.model().attribute("gardenLocation", is(garden.getGardenLocation())))
                .andExpect(MockMvcResultMatchers.model().attribute("gardenSize", is(garden.getGardenSize())))
                .andExpect(MockMvcResultMatchers.model().attribute("totalPlants", is(garden.getPlants().size())));
    }
    @Test
    @WithMockUser(username = "janeDoe@email.com")
    public void GetGardenDetailsPage_UserAuthorizedAndGardenExistsWithPlants_Return200() throws Exception {
        Garden garden = gardenList.get(1);
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/2"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("gardenName", is(garden.getGardenName())))
                .andExpect(MockMvcResultMatchers.model().attribute("gardenLocation", is(garden.getGardenLocation())))
                .andExpect(MockMvcResultMatchers.model().attribute("gardenSize", is(garden.getGardenSize())))
                .andExpect(MockMvcResultMatchers.model().attribute("totalPlants", is(garden.getPlants().size())));
    }

}
