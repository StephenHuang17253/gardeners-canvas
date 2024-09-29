package nz.ac.canterbury.seng302.gardenersgrove.integration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


import com.fasterxml.jackson.core.JsonProcessingException;
import nz.ac.canterbury.seng302.gardenersgrove.component.WeatherResponseData;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GridItemLocation;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Decoration;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.WeatherService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GridItemLocationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.service.DecorationService;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GridItemLocationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.util.DecorationCategory;
import nz.ac.canterbury.seng302.gardenersgrove.util.GridItemType;

import nz.ac.canterbury.seng302.gardenersgrove.util.PlantCategory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class Garden3DControllerTest {

    @Autowired
    public ObjectMapper objectMapper;

    @Autowired
    private GardenService gardenService;

    @Autowired
    private PlantService plantService;

    @Autowired
    private UserService userService;

    @Mock
    private FileService fileService;

    @Autowired
    private DecorationService decorationService;

    @Autowired
    private GridItemLocationService gridItemLocationService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GridItemLocationRepository gridItemLocationRepository;

    private MvcResult mockMvcResult;

    private List<Garden> gardenList = new ArrayList<>();

    private static final Long MAX_LONG = 10000L;

    private Garden garden1;
    private Garden garden2;
    private Garden garden3;


    private static User user1;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
    LocalDate date = LocalDate.parse("01/01/2001", formatter);

    private final int itemXCoord = 5;
    private final int itemYCoord = 5;
    private final String itemName = "test plant";

    private final String johnEmail = "johnDoe@Garden3dControllerTest.com";
    private final String janeEmail = "janeDoe@Garden3dControllerTest.com";

    @MockBean
    private WeatherService weatherService;

    private WeatherResponseData weatherData;
    private final String mockResponse = "{\n" +
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

    @BeforeAll
    void before_all() throws JsonProcessingException {
        weatherService = mock(WeatherService.class);
        JsonNode jsonObject = objectMapper.readTree(mockResponse);
        weatherData = new WeatherResponseData(jsonObject);

        User user;
        gridItemLocationRepository.deleteAll();
        user = new User("John", "Doe", johnEmail, date);
        user1 = userService.addUser(user, "1es1P@ssword");
        user = new User("Jane", "Doe", janeEmail, date);
        userService.addUser(user, "1es1P@ssword");

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
                "51.5072",
                "0.1276",
                user1));
        gardenList.add(garden1);

        Plant plant = plantService.addPlant("test plant", 1, "hello", null, garden1.getGardenId(),
                PlantCategory.POT_PLANT);
        GridItemLocation newLocation = new GridItemLocation(
                plant.getPlantId(),
                GridItemType.PLANT,
                garden1,
                itemXCoord,
                itemYCoord);
        gridItemLocationService.addGridItemLocation(newLocation);

        Garden garden2 = gardenService.addGarden(new Garden(
                "John's Other Garden",
                "",
                "114 Ilam Road",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                10.0,
                true,
                "-43.5214643",
                "172.5796159",
                user1));
        gardenList.add(garden2);

        Garden garden3 = gardenService.addGarden(new Garden(
                "John's Other Other Garden",
                "",
                "114 Ilam Road",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                10.0,
                true,
                "-43.5214643",
                "172.5796159",
                user1));
        gardenList.add(garden3);

        this.garden1 = gardenList.get(0);
        this.garden2 = gardenList.get(1);
        this.garden3 = gardenList.get(2);

        //adding decorations to garden2
        Decoration decoration1 = decorationService.addDecoration(new Decoration(this.garden2, DecorationCategory.GNOME));
        gridItemLocationService.addGridItemLocation(new GridItemLocation(decoration1.getId(), GridItemType.DECORATION, this.garden2, 6, 6));
        Decoration decoration2 = decorationService.addDecoration(new Decoration(this.garden2, DecorationCategory.POND));
        gridItemLocationService.addGridItemLocation(new GridItemLocation(decoration2.getId(), GridItemType.DECORATION, this.garden2, 4, 4));
        gridItemLocationService.addGridItemLocation(new GridItemLocation(decoration2.getId(), GridItemType.DECORATION, this.garden2, 3, 5));

    }

    @Test
    @WithMockUser(username = "janeDoe@email.com")
    void Get3DGarden_UserNotAuthorisedAndGardenDoesNotExist_Return404() throws Exception {
        mockMvc
                .perform(get("/3D-garden/{gardenId}", MAX_LONG))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "janeDoe@Garden3dControllerTest.com")
    void Get3DGarden_UserNotOwnerOrFriendGardenExists_Return403() throws Exception {
        Long gardenId = gardenList.get(0).getGardenId();
        mockMvc
                .perform(get("/3D-garden/{gardenId}", gardenId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "johnDoe@Garden3dControllerTest.com")
    void Get3DGarden_UserAuthorised_Return200() throws Exception {
        Garden garden = gardenList.get(0);
        Long gardenId = garden.getGardenId();
        mockMvc
                .perform(get("/3D-garden/{gardenId}", gardenId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "janeDoe@Garden3dControllerTest.com")
    void Get3DGarden_UserNotOwnerButGardenIsPublic_Return200() throws Exception {
        Garden garden = gardenList.get(1);
        Long gardenId = garden.getGardenId();
        mockMvc
                .perform(get("/3D-garden/{gardenId}", gardenId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "johnDoe@Garden3dControllerTest.com")
    void get3DGardenLayout_noPlantsAndNoDecorations_return200AndEmptyList() throws Exception {
        // making call to endpoint
        mockMvcResult = mockMvc
                .perform(get("/3D-garden-layout/{gardenId}", garden3.getGardenId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0))
                .andReturn();
    }


    @Test
    @WithMockUser(username = "johnDoe@Garden3dControllerTest.com")
    void get3DGardenLayout_noDecoration_return200AndNoDecorations() throws Exception {
        // making call to endpoint
        mockMvcResult = mockMvc
                .perform(get("/3D-garden-layout/{gardenId}", garden1.getGardenId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn();
        //checking there is only a plant, no decorations
        JsonNode jsonNode = objectMapper.readTree(mockMvcResult.getResponse().getContentAsString());
        JsonNode item = jsonNode.get(0);
        assertEquals(item.get("type").asText(), "PLANT");
    }

    @Test
    @WithMockUser(username = "johnDoe@Garden3dControllerTest.com")
    void get3DGardenLayout_oneDecoration_return200AndDecoration() throws Exception {
        // making call to endpoint
        mockMvcResult = mockMvc
                .perform(get("/3D-garden-layout/{gardenId}", garden2.getGardenId()))
                .andExpect(status().isOk())
                .andReturn();
        //checking decoration is correct
        JsonNode jsonNode = objectMapper.readTree(mockMvcResult.getResponse().getContentAsString());
        JsonNode item = jsonNode.get(0);
        //should match decoration1 in before_all
        assertEquals(item.get("xcoordinate").asInt(), 6);
        assertEquals(item.get("ycoordinate").asInt(), 6);
        assertEquals(item.get("category").asText(), "Gnome");
        assertEquals(item.get("type").asText(), "DECORATION");
    }

    @Test
    @WithMockUser(username = "johnDoe@Garden3dControllerTest.com")
    void get3DGardenLayout_multipleDecorations_return200AndDecorations() throws Exception {
        // making call to endpoint
        mockMvcResult = mockMvc
                .perform(get("/3D-garden-layout/{gardenId}", garden2.getGardenId()))
                .andExpect(status().isOk())
                .andReturn();
        //checking decoration is correct
        JsonNode jsonNode = objectMapper.readTree(mockMvcResult.getResponse().getContentAsString());
        //should match decoration1 in before_all
        JsonNode item1 = jsonNode.get(0);
        assertEquals(item1.get("xcoordinate").asInt(), 6);
        assertEquals(item1.get("ycoordinate").asInt(), 6);
        assertEquals(item1.get("category").asText(), "Gnome");
        assertEquals(item1.get("type").asText(), "DECORATION");
        //should match decoration2 in before_all
        JsonNode item2 = jsonNode.get(1);
        assertEquals(item2.get("xcoordinate").asInt(), 4);
        assertEquals(item2.get("ycoordinate").asInt(), 4);
        assertEquals(item2.get("category").asText(), "Pond");
        assertEquals(item2.get("type").asText(), "DECORATION");

    }

    @Test
    @WithMockUser(username = "johnDoe@Garden3dControllerTest.com")
    void get3DGardenLayout_copyOfDecorationInDifferentLocations_return200AndDuplicates() throws Exception {
        // making call to endpoint
        mockMvcResult = mockMvc
                .perform(get("/3D-garden-layout/{gardenId}", garden2.getGardenId()))
                .andExpect(status().isOk())
                .andReturn();
        //checking decoration is correct
        JsonNode jsonNode = objectMapper.readTree(mockMvcResult.getResponse().getContentAsString());
        //should match decoration2 in before_all
        JsonNode item1 = jsonNode.get(1);
        assertEquals(item1.get("xcoordinate").asInt(), 4);
        assertEquals(item1.get("ycoordinate").asInt(), 4);
        assertEquals(item1.get("category").asText(), "Pond");
        assertEquals(item1.get("type").asText(), "DECORATION");
        //should match decoration2 with different coordinates
        JsonNode item2 = jsonNode.get(2);
        assertEquals(item2.get("xcoordinate").asInt(), 3);
        assertEquals(item2.get("ycoordinate").asInt(), 5);
        assertEquals(item2.get("category").asText(), "Pond");
        assertEquals(item2.get("type").asText(), "DECORATION");
    }

    @Test
    @WithMockUser(username = "johnDoe@Garden3dControllerTest.com")
    void gardenHasAPlantWithALocation_WhenCallMade_LocationForPlantIsReturned() throws Exception {
        Garden garden = gardenList.get(0);
        Long gardenId = garden.getGardenId();
        mockMvcResult = mockMvc
                .perform(get("/3D-garden-layout/{gardenId}", gardenId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn();
        JsonNode jsonNode = objectMapper.readTree(mockMvcResult.getResponse().getContentAsString());
        JsonNode item = jsonNode.get(0);
        assertEquals(item.get("xcoordinate").asInt(), itemXCoord);
        assertEquals(item.get("ycoordinate").asInt(), itemYCoord);
        assertEquals(item.get("name").asText(), itemName);
    }

    @Test
    @WithMockUser(username = "johnDoe@Garden3dControllerTest.com")
    void gardenHasALocation_WhenCallMade_CurrentHourAndWeatherIsReturned() throws Exception {
        when(weatherService.getWeather(anyString(), anyString())).thenReturn(weatherData);
        Garden garden = gardenList.get(0);
        Long gardenId = garden.getGardenId();
        mockMvcResult = mockMvc
                .perform(get("/3D-garden/{gardenId}", gardenId))
                .andExpect(status().isOk())
                .andReturn();
        ModelAndView modelAndView = mockMvcResult.getModelAndView();
        assertNotNull(modelAndView);
        Map<String, Object> model = modelAndView.getModel();
        Assertions.assertNotNull(model.get("currentHour"));
        assertEquals("Overcast", model.get("weather"));

    }

    @Test
    @WithMockUser(username = "johnDoe@Garden3dControllerTest.com")
    void gardenDoesNotHaveALocation_WhenCallMade_NoCurrentHourOrWeatherIsReturned() throws Exception {
        when(weatherService.getWeather(anyString(), anyString())).thenReturn(null);
        Garden garden = gardenList.get(0);
        Long gardenId = garden.getGardenId();
        mockMvcResult = mockMvc
                .perform(get("/3D-garden/{gardenId}", gardenId))
                .andExpect(status().isOk())
                .andReturn();
        ModelAndView modelAndView = mockMvcResult.getModelAndView();
        assertNotNull(modelAndView);
        Map<String, Object> model = modelAndView.getModel();
        Assertions.assertNull(model.get("currentHour"));
        Assertions.assertNull(model.get("weather"));
    }

}
