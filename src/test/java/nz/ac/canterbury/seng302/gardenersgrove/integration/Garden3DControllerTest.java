package nz.ac.canterbury.seng302.gardenersgrove.integration;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GridItemLocation;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GridItemLocationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.util.GridItemType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private GridItemLocationService gridItemLocationService;

    @Autowired
    private MockMvc mockMvc;

    private MvcResult mockMvcResult;

    private List<Garden> gardenList = new ArrayList<>();

    private static final Long MAX_LONG = 10000L;

    private static User user1;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
    LocalDate date = LocalDate.parse("01/01/2001", formatter);

    private final int itemXCoord = 5;
    private final int itemYCoord = 5;
    private final String itemName = "testName";
    private final String itemModelName = "testModel";

    private final String johnEmail = "johnDoe@Garden3dControllerTest.com";
    private final String janeEmail = "janeDoe@Garden3dControllerTest.com";

    @BeforeAll
    void before_each() {
        User user;
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
                "-43.5214643",
                "172.5796159",
                user1));
        gardenList.add(garden1);

        Plant plant = plantService.addPlant("test plant", 1, "hello", null, garden1.getGardenId(), null);
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
        assertEquals(item.get("xCoordinate").asInt(), itemXCoord);
        assertEquals(item.get("yCoordinate").asInt(), itemYCoord);
        assertEquals(item.get("name").asText(), itemName);
        assertEquals(item.get("modelName").asText(), itemModelName);
    }

}
