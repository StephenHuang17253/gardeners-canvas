package nz.ac.canterbury.seng302.gardenersgrove.integration;

import net.minidev.json.JSONArray;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GridItemLocation;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.model.GardenDetailModel;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GridItemLocationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.HomePageLayoutRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.util.GridItemType;
import nz.ac.canterbury.seng302.gardenersgrove.util.PlantCategory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class Garden2dControllerTest {
    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    public HomePageLayoutRepository homePageLayoutRepository;
    private MockMvc mockMvc;
    @Autowired
    private GardenService gardenService;
    @Autowired
    private PlantService plantService;

    @Autowired
    UserService userService;

    @Autowired
    private GridItemLocationRepository gridItemLocationRepository;

    private List<Garden> gardenList = new ArrayList<>();
    private List<Plant> plantList = new ArrayList<>();

    User mockUser = new User("John", "Test", "profile.user.test@ProfileController.com", LocalDate.now());


    private static final Long MAX_LONG = 10000L;
    private static final int COUNT_PER_PAGE = 6;

    @Autowired
    Garden2dControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private static User user1;
    private static User user2;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
    LocalDate date = LocalDate.parse("01/01/2001", formatter);

    @BeforeAll
    void before_each() {
        userService = new UserService(passwordEncoder, userRepository, homePageLayoutRepository);
        user1 = new User("John", "Doe", "jhonDoe@Garden2dControllerTest.com", date);
        user2 = new User("Jane", "Doe", "janeDoe@Garden2dControllerTest.com", date);
        userService.addUser(user1, "1es1P@ssword");
        userService.addUser(user2, "1es1P@ssword");
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
        gardenList.add(garden1);
        for (int i = 0; i < COUNT_PER_PAGE + 1; i++) {
            plantList.add(plantService.addPlant(String.valueOf(i),
                    1,
                    "testDescription1",
                    date,
                    garden1.getGardenId(),
                    PlantCategory.TREE));
        }

    }

    @Test
    void Get2DGarden_UserNotAuthorizedAndGardenDoseNotExist_Return403() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/2D-garden/" + MAX_LONG))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "janeDoe@Garden2dControllerTest.com")
    void Get2DGarden_UserNotAuthorizedAndGardenExists_Return403() throws Exception {
        Long gardenId = gardenList.get(0).getGardenId();
        mockMvc
                .perform(MockMvcRequestBuilders.get("/2D-garden/" + gardenId))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "janeDoe@email.com")
    void GetGardenDetailsPage_UserNotAuthorizedAndGardenDoesNotExist_Return404() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/2D-garden/" + MAX_LONG))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(username = "jhonDoe@Garden2dControllerTest.com")
    void Get2DGarden_UserAuthorized_Return200() throws Exception {
        Garden garden = gardenList.get(0);
        Long gardenId = gardenList.get(0).getGardenId();
        List<Plant> expectedPlants = plantList.subList(0, COUNT_PER_PAGE);
        expectedPlants.sort(Comparator.comparing(Plant::getPlantName));
        MvcResult mvcResult = mockMvc
                .perform(MockMvcRequestBuilders.get("/2D-garden/" + gardenId))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        ModelMap modelMap = mvcResult.getModelAndView().getModelMap();
        Assertions.assertNotNull(modelMap);

        GardenDetailModel gardenDetailModel = (GardenDetailModel) modelMap.getAttribute("garden");
        List<Plant> plants = (List<Plant>) modelMap.getAttribute("plants");

        Assertions.assertEquals(garden.getGardenName(), gardenDetailModel.getGardenName());
        Assertions.assertEquals(garden.getGardenLocation(), gardenDetailModel.getGardenLocation());
        Assertions.assertEquals(garden.getGardenSize(), gardenDetailModel.getGardenSize());
        Assertions.assertEquals(expectedPlants.size(), plants.size());
        Assertions.assertEquals(garden.getIsPublic(), gardenDetailModel.isGardenIsPublic());
    }

    @Test
    @WithMockUser(username = "jhonDoe@Garden2dControllerTest.com")
    void save2DGarden_emptyLists_returnRedirectAndDoNotChangePersistence() throws Exception {
        gridItemLocationRepository.deleteAll();
        Long gardenId = userService.getUserByEmail("jhonDoe@Garden2dControllerTest.com").getGardens().get(0).getGardenId();
        List<String> idList = new ArrayList<>();
        List<Double> xCoordList = new ArrayList<>();
        List<Double> yCoordList = new ArrayList<>();

        MvcResult mvcResult = mockMvc
                .perform(MockMvcRequestBuilders.post("/2D-garden/" + gardenId + "/save").with(csrf())
                        .param("idList", JSONArray.toJSONString(idList))
                        .param("xCoordList", JSONArray.toJSONString(xCoordList))
                        .param("yCoordList", JSONArray.toJSONString(yCoordList)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andReturn();
        Assertions.assertTrue(gridItemLocationRepository.findAll().isEmpty());

    }

    @Test
    @WithMockUser(username = "jhonDoe@Garden2dControllerTest.com")
    void save2DGarden_oneItemInEachList_returnRedirectAndChangePersistence() throws Exception {
        //preparing parameters
        gridItemLocationRepository.deleteAll();
        Long gardenId = userService.getUserByEmail("jhonDoe@Garden2dControllerTest.com").getGardens().get(0).getGardenId();
        Plant testPlant = gardenService.getGardenById(gardenId).get().getPlants().get(0);
        List<String> idList = new ArrayList<>();
        idList.add(testPlant.getPlantId().toString());
        List<Double> xCoordList = new ArrayList<>();
        xCoordList.add(2.3);
        List<Double> yCoordList = new ArrayList<>();
        yCoordList.add(3.3);

        //making call to endpoint
        MvcResult mvcResult = mockMvc
                .perform(MockMvcRequestBuilders.post("/2D-garden/" + gardenId + "/save").with(csrf())
                        .param("idList", JSONArray.toJSONString(idList))
                        .param("xCoordList", JSONArray.toJSONString(xCoordList))
                        .param("yCoordList", JSONArray.toJSONString(yCoordList)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andReturn();

        Assertions.assertFalse(gridItemLocationRepository.findAll().isEmpty());
        Assertions.assertEquals(1, gridItemLocationRepository.findAll().size());
        Optional<GridItemLocation> gridItemAddedToRepository = gridItemLocationRepository.findGridItemLocationByObjectIdAndItemTypeAndGarden(testPlant.getPlantId(), GridItemType.PLANT, gardenService.getGardenById(gardenId).get());
        Assertions.assertTrue(gridItemAddedToRepository.isPresent());
        Assertions.assertEquals(2, gridItemAddedToRepository.get().getXCoordinates());
        Assertions.assertEquals(3, gridItemAddedToRepository.get().getYCoordinates());

    }

    @Test
    @WithMockUser(username = "jhonDoe@Garden2dControllerTest.com")
    void save2DGarden_twoItemsInEachList_returnRedirectAndChangePersistence() throws Exception {
        //preparing parameters
        gridItemLocationRepository.deleteAll();
        Long gardenId = userService.getUserByEmail("jhonDoe@Garden2dControllerTest.com").getGardens().get(0).getGardenId();
        Plant testPlant = gardenService.getGardenById(gardenId).get().getPlants().get(0);
        Plant testPlant2 = gardenService.getGardenById(gardenId).get().getPlants().get(1);
        List<String> idList = new ArrayList<>();
        idList.add(testPlant.getPlantId().toString());
        idList.add(testPlant2.getPlantId().toString());
        List<Double> xCoordList = new ArrayList<>();
        xCoordList.add(2.3);
        xCoordList.add(4.5);
        List<Double> yCoordList = new ArrayList<>();
        yCoordList.add(3.3);
        yCoordList.add(5.5);


        //making call to endpoint
        MvcResult mvcResult = mockMvc
                .perform(MockMvcRequestBuilders.post("/2D-garden/" + gardenId + "/save").with(csrf())
                        .param("idList", JSONArray.toJSONString(idList))
                        .param("xCoordList", JSONArray.toJSONString(xCoordList))
                        .param("yCoordList", JSONArray.toJSONString(yCoordList)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andReturn();

        //check persistence is updated
        Assertions.assertFalse(gridItemLocationRepository.findAll().isEmpty());
        Assertions.assertEquals(2, gridItemLocationRepository.findAll().size());
        Optional<GridItemLocation> gridItemAddedToRepository = gridItemLocationRepository.findGridItemLocationByObjectIdAndItemTypeAndGarden(testPlant.getPlantId(), GridItemType.PLANT, gardenService.getGardenById(gardenId).get());
        Assertions.assertTrue(gridItemAddedToRepository.isPresent());
        Assertions.assertEquals(2, gridItemAddedToRepository.get().getXCoordinates());
        Assertions.assertEquals(3, gridItemAddedToRepository.get().getYCoordinates());
        Optional<GridItemLocation> gridItem2AddedToRepository = gridItemLocationRepository.findGridItemLocationByObjectIdAndItemTypeAndGarden(testPlant2.getPlantId(), GridItemType.PLANT, gardenService.getGardenById(gardenId).get());
        Assertions.assertTrue(gridItem2AddedToRepository.isPresent());
        Assertions.assertEquals(4, gridItem2AddedToRepository.get().getXCoordinates());
        Assertions.assertEquals(5, gridItem2AddedToRepository.get().getYCoordinates());

    }

    @Test
    @WithMockUser(username = "jhonDoe@Garden2dControllerTest.com")
    void save2DGarden_differentNumberOfItemsInEachList_return400() throws Exception {
        gridItemLocationRepository.deleteAll();
        Long gardenId = userService.getUserByEmail("jhonDoe@Garden2dControllerTest.com").getGardens().get(0).getGardenId();
        Plant testPlant = gardenService.getGardenById(gardenId).get().getPlants().get(0);
        Plant testPlant2 = gardenService.getGardenById(gardenId).get().getPlants().get(1);
        List<String> idList = new ArrayList<>();
        idList.add(testPlant.getPlantId().toString());
        idList.add(testPlant2.getPlantId().toString());
        List<Double> xCoordList = new ArrayList<>();
        xCoordList.add(2.3);
        List<Double> yCoordList = new ArrayList<>();

        //making call to endpoint
        MvcResult mvcResult = mockMvc
                .perform(MockMvcRequestBuilders.post("/2D-garden/" + gardenId + "/save").with(csrf())
                        .param("idList", JSONArray.toJSONString(idList))
                        .param("xCoordList", JSONArray.toJSONString(xCoordList))
                        .param("yCoordList", JSONArray.toJSONString(yCoordList)))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();
        Assertions.assertTrue(gridItemLocationRepository.findAll().isEmpty());

    }

    @Test
    @WithMockUser(username = "jhonDoe@Garden2dControllerTest.com")
    void save2DGarden_invalidGardenID_return404() throws Exception {
        gridItemLocationRepository.deleteAll();
        Long gardenId = userService.getUserByEmail("jhonDoe@Garden2dControllerTest.com").getGardens().get(0).getGardenId();
        Plant testPlant = gardenService.getGardenById(gardenId).get().getPlants().get(0);
        List<String> idList = new ArrayList<>();
        idList.add(testPlant.getPlantId().toString());
        List<Double> xCoordList = new ArrayList<>();
        xCoordList.add(2.3);
        List<Double> yCoordList = new ArrayList<>();
        yCoordList.add(3.3);

        //making call to endpoint
        MvcResult mvcResult = mockMvc
                .perform(MockMvcRequestBuilders.post("/2D-garden/" + 9L + "/save").with(csrf())
                        .param("idList", JSONArray.toJSONString(idList))
                        .param("xCoordList", JSONArray.toJSONString(xCoordList))
                        .param("yCoordList", JSONArray.toJSONString(yCoordList)))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();
        Assertions.assertTrue(gridItemLocationRepository.findAll().isEmpty());

    }


}
