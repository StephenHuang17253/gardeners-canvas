package nz.ac.canterbury.seng302.gardenersgrove.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GridItemLocation;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.model.DisplayableItem;
import nz.ac.canterbury.seng302.gardenersgrove.repository.*;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.util.GridItemType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import nz.ac.canterbury.seng302.gardenersgrove.controller.Garden3DController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GridItemLocationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class Garden3DControllerTest {

    @Autowired
    private Garden3DController garden3DController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HomePageLayoutRepository homePageLayoutRepository;

    @Autowired
    private GardenRepository gardenRepository;

    @Autowired
    private GridItemLocationRepository gridItemLocationRepository;

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    public ObjectMapper objectMapper;

    @Mock
    private FileService fileService;

    private UserService userService;

    private GardenService gardenService;

    private GridItemLocationService gridItemLocationService;

    private PlantService plantService;

    private MvcResult mockMvcResult;

    private Long gardenId;

    private int itemXCoord = 5;
    private int itemYCoord = 5;
    private String itemName = "testName";
    private String itemModelName = "testModel";

    @BeforeEach
    void before_or_after_all() {
        if (userService == null) {
            userService = new UserService(passwordEncoder, userRepository, homePageLayoutRepository);
            gardenService = new GardenService(gardenRepository, userService);
            gridItemLocationService = new GridItemLocationService(gridItemLocationRepository);
            plantService = new PlantService(plantRepository, gardenService, fileService);
        }

        String userEmail = "johndoe.test@email.com";
        if (!userService.emailInUse(userEmail)) {
            User user = new User("John", "Doe", userEmail, null);
            userService.addUser(user, "AlphabetSoup10!");
            Garden garden = new Garden("test garden", "hello", "the white house", "", "", "", "murica", 12.0, false,
                    null, null, user);
            gardenService.addGarden(garden);
            gardenId = garden.getGardenId();
            Plant plant = plantService.addPlant("test plant", 1, "hello", null, garden.getGardenId(), null);
            GridItemLocation newLocation = new GridItemLocation(
                    plant.getPlantId(),
                    GridItemType.PLANT,
                    garden,
                    5,
                    5);
            gridItemLocationService.addGridItemLocation(newLocation);
        }
    }

    @Test
    void controllerLoads() {
        assertNotNull(garden3DController);
    }

    @Test
    @WithMockUser(username = "johndoe.test@email.com")
    void gardenHasAPlantWithALocation_WhenCallMade_LocationForPlantIsReturned() throws Exception {
        mockMvcResult= this.mockMvc.perform(MockMvcRequestBuilders.get("/3D-garden-layout/" + gardenId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn();
        JsonNode jsonNode = objectMapper.readTree(mockMvcResult.getResponse().getContentAsString());
        JsonNode item = jsonNode.get(0);
        Assertions.assertEquals(item.get("xCoordinate").asInt(), itemXCoord);
        Assertions.assertEquals(item.get("yCoordinate").asInt(), itemYCoord);
        Assertions.assertEquals(item.get("name").asText(), itemName);
        Assertions.assertEquals(item.get("modelName").asText(), itemModelName);
    }

}
