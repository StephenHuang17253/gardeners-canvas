package nz.ac.canterbury.seng302.gardenersgrove.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nz.ac.canterbury.seng302.gardenersgrove.component.DailyWeather;
import nz.ac.canterbury.seng302.gardenersgrove.component.WeatherResponseData;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTag;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTagRelation;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class GardensControllerIntegrationTests {

    @Autowired
    private GardenService gardenService;

    @Autowired
    private UserService userService;

    @Autowired
    private PlantService plantService;

    @Autowired
    private GardenTagService gardenTagService;

    @MockBean
    WeatherService weatherService;

    private final MockMvc mockMvc;
    private List<Garden> gardenList = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
    LocalDate date = LocalDate.parse("01/01/2001", formatter);
    private static final Long MAX_LONG = 10000L;

    @Autowired
    GardensControllerIntegrationTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeAll
    void before_or_after_all() {
        gardenList = new ArrayList<>();
        User user1 = new User("John", "Doe", "johnDoe@email.com", date);
        User user2 = new User("Jane", "Doe", "janeDoe@email.com", date);
        User user3 = new User("Bruce", "Wayne", "bruceWayne@email.com", date);
        userService.addUser(user1, "1es1P@ssword");
        userService.addUser(user2, "1es1P@ssword");
        userService.addUser(user3, "1es1P@ssword");
        Garden garden1 = gardenService.addGarden(new Garden(
                "John's Garden",
                "",
                "114 Ilam Road",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                10.0,
                false,
                "-43.5214643",
                "172.5796159",
                userService.getUserByEmail(user1.getEmailAddress())));

        Garden garden2 = gardenService.addGarden(new Garden(
                "Jane's Garden",
                "",
                "20 Kirkwood Avenue",
                "Upper Riccarton",
                "Christchurch",
                "8041",
                "New Zealand",
                20.0,
                false,
                "-43.5214643",
                "172.5796159",
                userService.getUserByEmail(user2.getEmailAddress())));
        Garden garden3 = gardenService.addGarden(new Garden(
                "John's Garden",
                "Some description",
                "Some Real Address",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                10.0,
                false,
                "-43.5214643",
                "172.5796159",
                userService.getUserByEmail(user3.getEmailAddress())));
        Garden garden4 = gardenService.addGarden(new Garden(
                "John's Garden",
                "Some description",
                "Some Real Address",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                10.0,
                false,
                "-43.5214643",
                "172.5796159",
                userService.getUserByEmail(user3.getEmailAddress())));

        plantService.addPlant("Java Tree", 1, "Grows Java Plums", date, garden2.getGardenId());
        plantService.addPlant("Java Tree", 1, "Grows Java Plums", date, garden2.getGardenId());
        gardenList.add(gardenService.getGardenById(garden1.getGardenId()).get());
        gardenList.add(gardenService.getGardenById(garden2.getGardenId()).get());
        gardenList.add(gardenService.getGardenById(garden3.getGardenId()).get());
        gardenList.add(gardenService.getGardenById(garden4.getGardenId()).get());

    }

    @Test
    void GetMyGardens_UserNotAuthorized_Return403() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "johnDoe@email.com")
    void GetMyGardens_UserAuthorized_Return200() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username = "bruceWayne@email.com")
    void GetAnIndividualGarden_GardenHasBadLocationValue_Return200ButHasWeatherError() throws Exception {

        WeatherResponseData mockResponseData = Mockito.mock(WeatherResponseData.class);
        Mockito.when(mockResponseData.getPastWeather()).thenThrow(new NullPointerException("No such location"));
        Mockito.when(mockResponseData.getCurrentWeather())
                .thenThrow(new NullPointerException("No such location"));
        Mockito.when(mockResponseData.getForecastWeather())
                .thenThrow(new NullPointerException("No such location"));
        Mockito.when(weatherService.getWeather(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(mockResponseData);

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/my-gardens/{gardenId}", gardenList.get(2).getGardenId()))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        String expectedErrorMessage = "Location not found, please update your location to see the weather";
        Assertions.assertEquals(expectedErrorMessage,
                ((List<DailyWeather>) result.getModelAndView().getModel().get("weather")).get(0)
                        .getWeatherError());
    }

    @Test
    @WithMockUser(username = "bruceWayne@email.com")
    void GetAnIndividualGarden_GardenHasGoodLocationValue_Return200WithGoodWeatherInfo() throws Exception {

        String mockResponse = "{\n" +
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

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/my-gardens/{gardenId}", gardenList.get(3).getGardenId()))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        Assertions.assertNull(((List<DailyWeather>) result.getModelAndView().getModel().get("weather")).get(0)
                .getWeatherError());
    }

    @Test
    @WithAnonymousUser
    void GetGardenDetailsPage_UserNotAuthenticated_Return403() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/1"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "janeDoe@email.com")
    void GetGardenDetailsPage_UserNotAuthorizedAndGardenDoesNotExist_Return404() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/" + MAX_LONG))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(username = "bruceWayne@email.com")
    void GetGardenDetailsPage_UserNotAuthorizedAndGardenExists_Return403() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/1"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "johnDoe@email.com")
    void GetGardenDetailsPage_UserAuthorizedAndGardenExists_Return200() throws Exception {
        Garden garden = gardenList.get(0);
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("gardenName",
                        is(garden.getGardenName())))
                .andExpect(MockMvcResultMatchers.model().attribute("gardenLocation",
                        is(garden.getGardenLocation())))
                .andExpect(MockMvcResultMatchers.model().attribute("gardenSize",
                        is(garden.getGardenSize())))
                .andExpect(MockMvcResultMatchers.model().attribute("totalPlants",
                        is(garden.getPlants().size())))
                .andExpect(MockMvcResultMatchers.model().attribute("makeGardenPublic",
                        is(garden.getIsPublic())));
        ;
    }

    @Test
    @WithMockUser(username = "janeDoe@email.com")
    void GetGardenDetailsPage_UserAuthorizedAndGardenExistsWithPlants_Return200() throws Exception {
        Garden garden = gardenList.get(1);
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/2"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("gardenName",
                        is(garden.getGardenName())))
                .andExpect(MockMvcResultMatchers.model().attribute("gardenLocation",
                        is(garden.getGardenLocation())))
                .andExpect(MockMvcResultMatchers.model().attribute("gardenSize",
                        is(garden.getGardenSize())))
                .andExpect(MockMvcResultMatchers.model().attribute("totalPlants",
                        is(garden.getPlants().size())))
                .andExpect(MockMvcResultMatchers.model().attribute("makeGardenPublic",
                        is(garden.getIsPublic())));
        ;
    }

    @Test
    @WithMockUser(username = "johnDoe@email.com")
    void GetGardenDetailsPage_UserAuthorizedAndGardenExistsAndIsPublic_Return200() throws Exception {
        Garden garden = gardenList.get(0);
        // make an initial request to ensure garden is private as set in @before
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("makeGardenPublic", false));

        // set the garden to public
        mockMvc.perform(MockMvcRequestBuilders.post("/my-gardens/1/public").with(csrf())
                .param("makeGardenPublic", "true"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

        // check the garden is now public
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("gardenName",
                        is(garden.getGardenName())))
                .andExpect(MockMvcResultMatchers.model().attribute("gardenLocation",
                        is(garden.getGardenLocation())))
                .andExpect(MockMvcResultMatchers.model().attribute("gardenSize",
                        is(garden.getGardenSize())))
                .andExpect(MockMvcResultMatchers.model().attribute("totalPlants",
                        is(garden.getPlants().size())))
                .andExpect(MockMvcResultMatchers.model().attribute("makeGardenPublic", true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Vegetable Garden", "Flower Bed", "Herb Garden", "Succulent Area", "Fruit Orchard",
            "Rose Collection", "Perennial Patch", "Shade Garden", "Rock Garden", "Tropical Zone", "Cottage Garden",
            "aaaaaaaaaaaaaaaaaaaaaaaaa"})
    @WithMockUser(username = "johnDoe@email.com")
    void PostGardenDetailsPage_AddValidTag_Return302(String tagInput) throws Exception {
        Garden garden = gardenList.get(0);


        // Add tag
        mockMvc
                .perform(MockMvcRequestBuilders.post("/my-gardens/1/tag").with(csrf())
                .param("tag", tagInput))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andReturn();

        // Garden Tag relation should exist now
        GardenTag tag = gardenTagService.getByName(tagInput).get();
        Assertions.assertTrue(gardenTagService.getGardenTagRelationByGardenAndTag(garden, tag).isPresent());

        // And the tag is visible on the Garden's Details page.
        List<String> expectedTagsList = gardenTagService.getGardenTagRelationByGarden(garden).stream()
                .map(GardenTagRelation::getTag)
                .map(GardenTag::getTagName)
                .toList();

        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/1"))
                .andExpect(MockMvcResultMatchers.model().attribute("tagsList",
                        is((expectedTagsList))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Flower@Bed", "Herb$Garden", "Succulent#Area", "Fruit%Orchard", "Rose^Collection",
            "Perennial&Patch", "Shade*Garden", "Rock(Garden", "Tropical)Zone", "Cottage+Garden", "Garden=Area",
            "Vegetable|Garden", "Herb~Garden", "Fruit`Orchard", "Succulent;Area", "Tropical)Zone", "Rock<Area",
            "Shade>Garden", "Cottage/Garden", "Flower?Bed", "Perennial[Patch", "Rose]Collection", "Herb{Garden",
            "Tropical}Zone", "Succulent;Area", "Fruit`Orchard", "Rock.Garden", "Shade!Garden", "Cottage@Zone",
            "aaaaaaaaaaaaaaaaaaaaaaaaaa"})
    @WithMockUser(username = "johnDoe@email.com")
    void PostGardenDetailsPage_AddInvalidTag_DontAddTag(String tagInput) throws Exception {
        Garden garden = gardenList.get(0);

        // Attempt to add tag
        mockMvc
                .perform(MockMvcRequestBuilders.post("/my-gardens/1/tag").with(csrf())
                .param("tag", tagInput))
                .andExpect(status().isOk());

        // Tag shouldn't be added to the garden
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/1"))
                .andExpect(MockMvcResultMatchers.model().attribute("tagsList",
                        is(empty())));

        // Tag shouldn't be added to the system
        Assertions.assertTrue(gardenTagService.getGardenTagRelationByGarden(garden).isEmpty());
    }

}
