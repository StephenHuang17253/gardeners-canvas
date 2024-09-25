package nz.ac.canterbury.seng302.gardenersgrove.integration;

import net.minidev.json.JSONArray;
import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.model.DisplayableItem;
import nz.ac.canterbury.seng302.gardenersgrove.model.GardenDetailModel;
import nz.ac.canterbury.seng302.gardenersgrove.repository.DecorationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GridItemLocationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.HomePageLayoutRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.DecorationCategory;
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
class Garden2dControllerTest {
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
    private GridItemLocationService gridItemLocationService;

    @Autowired
    private GridItemLocationRepository gridItemLocationRepository;

    @Autowired
    private DecorationService decorationService;

    @Autowired
    private DecorationRepository decorationRepository;

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
        decorationRepository.deleteAll();
        gridItemLocationRepository.deleteAll();
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
        Long gardenId = garden.getGardenId();
        List<Plant> expectedPlants = new ArrayList<>(plantList);
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
    void Get2DGarden_WithPlantAndDecoration_Return200() throws Exception {
        decorationRepository.deleteAll();
        gridItemLocationRepository.deleteAll();
        Garden garden = gardenList.get(0);
        Long gardenId = garden.getGardenId();

        // Prepare GridItemLocations to have plant and also a decoration
        Plant testPlant = plantService.addPlant("Test Plant", 1, "", LocalDate.now(), gardenId, PlantCategory.CLIMBER);
        gridItemLocationService.addGridItemLocation(new GridItemLocation(testPlant.getPlantId(), GridItemType.PLANT, garden, 0, 0));

        Decoration testDecoration = decorationService.getDecorationsByGarden(garden).get(0);
        gridItemLocationService.addGridItemLocation(new GridItemLocation(testDecoration.getId(), GridItemType.DECORATION, garden, 6, 6));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/2D-garden/" + gardenId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ModelMap modelMap = mvcResult.getModelAndView().getModelMap();
        List<DisplayableItem> displayableItems = (List<DisplayableItem>) modelMap.getAttribute("displayableItemsList");
        Assertions.assertNotNull(modelMap);

        GardenDetailModel gardenDetailModel = (GardenDetailModel) modelMap.getAttribute("garden");

        // Assert that we have both a plant and a decoration in the displayable items
        Assertions.assertEquals(garden.getGardenName(), gardenDetailModel.getGardenName());
        Assertions.assertEquals(2, displayableItems.size());
        Assertions.assertEquals(testPlant.getPlantName(), displayableItems.get(0).getName());
        Assertions.assertEquals(testDecoration.getDecorationCategory().getCategoryName(), displayableItems.get(1).getName());
    }

    @Test
    @WithMockUser(username = "jhonDoe@Garden2dControllerTest.com")
    void save2DGarden_emptyLists_returnRedirectAndDoNotChangePersistence() throws Exception {
        gridItemLocationRepository.deleteAll();
        Long gardenId = userService.getUserByEmail("jhonDoe@Garden2dControllerTest.com").getGardens().get(0)
                .getGardenId();
        List<String> idList = new ArrayList<>();
        List<String> typeList = new ArrayList<>();
        List<Double> xCoordList = new ArrayList<>();
        List<Double> yCoordList = new ArrayList<>();

        mockMvc.perform(MockMvcRequestBuilders.post("/2D-garden/" + gardenId + "/save").with(csrf())
                .param("idList", JSONArray.toJSONString(idList))
                .param("typeList", JSONArray.toJSONString(typeList))
                .param("xCoordList", JSONArray.toJSONString(xCoordList))
                .param("yCoordList", JSONArray.toJSONString(yCoordList)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andReturn();
        Assertions.assertTrue(gridItemLocationRepository.findAll().isEmpty());

    }

    @Test
    @WithMockUser(username = "jhonDoe@Garden2dControllerTest.com")
    void save2DGarden_oneItemInEachList_returnRedirectAndChangePersistence() throws Exception {
        // preparing parameters
        gridItemLocationRepository.deleteAll();
        Long gardenId = userService.getUserByEmail("jhonDoe@Garden2dControllerTest.com").getGardens().get(0)
                .getGardenId();
        Plant testPlant = gardenService.getGardenById(gardenId).get().getPlants().get(0);
        List<String> idList = new ArrayList<>();
        idList.add(testPlant.getPlantId().toString());
        List<String> typeList = new ArrayList<>();
        typeList.add("PLANT");
        List<Double> xCoordList = new ArrayList<>();
        xCoordList.add(2.3);
        List<Double> yCoordList = new ArrayList<>();
        yCoordList.add(3.3);

        // making call to endpoint
        mockMvc.perform(MockMvcRequestBuilders.post("/2D-garden/" + gardenId + "/save").with(csrf())
                .param("idList", JSONArray.toJSONString(idList))
                .param("typeList", JSONArray.toJSONString(typeList))
                .param("xCoordList", JSONArray.toJSONString(xCoordList))
                .param("yCoordList", JSONArray.toJSONString(yCoordList)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andReturn();

        Assertions.assertFalse(gridItemLocationRepository.findAll().isEmpty());
        Assertions.assertEquals(1, gridItemLocationRepository.findAll().size());
        Optional<GridItemLocation> gridItemAddedToRepository = gridItemLocationRepository
                .findGridItemLocationByObjectIdAndItemTypeAndGarden(testPlant.getPlantId(),
                        GridItemType.PLANT,
                        gardenService.getGardenById(gardenId).get());
        Assertions.assertTrue(gridItemAddedToRepository.isPresent());
        Assertions.assertEquals(2, gridItemAddedToRepository.get().getXCoordinate());
        Assertions.assertEquals(3, gridItemAddedToRepository.get().getYCoordinate());
    }

    @Test
    @WithMockUser(username = "jhonDoe@Garden2dControllerTest.com")
    void save2DGarden_oneItemInEachListDecoration_returnRedirectAndChangePersistence() throws Exception {
        // preparing parameters
        gridItemLocationRepository.deleteAll();
        Long gardenId = userService.getUserByEmail("jhonDoe@Garden2dControllerTest.com").getGardens().get(0)
                .getGardenId();
        Plant testPlant = gardenService.getGardenById(gardenId).get().getPlants().get(0);
        List<String> idList = new ArrayList<>();
        idList.add(testPlant.getPlantId().toString());
        List<String> typeList = new ArrayList<>();
        typeList.add("DECORATION");
        List<Double> xCoordList = new ArrayList<>();
        xCoordList.add(2.3);
        List<Double> yCoordList = new ArrayList<>();
        yCoordList.add(3.3);

        // making call to endpoint
        mockMvc.perform(MockMvcRequestBuilders.post("/2D-garden/" + gardenId + "/save").with(csrf())
                        .param("idList", JSONArray.toJSONString(idList))
                        .param("typeList", JSONArray.toJSONString(typeList))
                        .param("xCoordList", JSONArray.toJSONString(xCoordList))
                        .param("yCoordList", JSONArray.toJSONString(yCoordList)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andReturn();

        Assertions.assertFalse(gridItemLocationRepository.findAll().isEmpty());
        Assertions.assertEquals(1, gridItemLocationRepository.findAll().size());
        Optional<GridItemLocation> gridItemAddedToRepository = gridItemLocationRepository
                .findGridItemLocationByObjectIdAndItemTypeAndGarden(testPlant.getPlantId(),
                        GridItemType.DECORATION,
                        gardenService.getGardenById(gardenId).get());
        Assertions.assertTrue(gridItemAddedToRepository.isPresent());
        Assertions.assertEquals(2, gridItemAddedToRepository.get().getXCoordinate());
        Assertions.assertEquals(3, gridItemAddedToRepository.get().getYCoordinate());
    }

    @Test
    @WithMockUser(username = "jhonDoe@Garden2dControllerTest.com")
    void save2DGarden_twoItemsInEachList_returnRedirectAndChangePersistence() throws Exception {
        // preparing parameters
        gridItemLocationRepository.deleteAll();
        Long gardenId = userService.getUserByEmail("jhonDoe@Garden2dControllerTest.com").getGardens().get(0)
                .getGardenId();
        Plant testPlant = gardenService.getGardenById(gardenId).get().getPlants().get(0);
        Plant testPlant2 = gardenService.getGardenById(gardenId).get().getPlants().get(1);
        List<String> idList = new ArrayList<>();
        idList.add(testPlant.getPlantId().toString());
        idList.add(testPlant2.getPlantId().toString());
        List<String> typeList = new ArrayList<>();
        typeList.add("PLANT");
        typeList.add("PLANT");
        List<Double> xCoordList = new ArrayList<>();
        xCoordList.add(2.3);
        xCoordList.add(4.5);
        List<Double> yCoordList = new ArrayList<>();
        yCoordList.add(3.3);
        yCoordList.add(5.5);

        // making call to endpoint
        mockMvc.perform(MockMvcRequestBuilders.post("/2D-garden/" + gardenId + "/save").with(csrf())
                .param("idList", JSONArray.toJSONString(idList))
                .param("typeList", JSONArray.toJSONString(typeList))
                .param("xCoordList", JSONArray.toJSONString(xCoordList))
                .param("yCoordList", JSONArray.toJSONString(yCoordList)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andReturn();

        // check persistence is updated
        Assertions.assertFalse(gridItemLocationRepository.findAll().isEmpty());
        Assertions.assertEquals(2, gridItemLocationRepository.findAll().size());
        Optional<GridItemLocation> gridItemAddedToRepository = gridItemLocationRepository
                .findGridItemLocationByObjectIdAndItemTypeAndGarden(testPlant.getPlantId(),
                        GridItemType.PLANT,
                        gardenService.getGardenById(gardenId).get());
        Assertions.assertTrue(gridItemAddedToRepository.isPresent());
        Assertions.assertEquals(2, gridItemAddedToRepository.get().getXCoordinate());
        Assertions.assertEquals(3, gridItemAddedToRepository.get().getYCoordinate());
        Optional<GridItemLocation> gridItem2AddedToRepository = gridItemLocationRepository
                .findGridItemLocationByObjectIdAndItemTypeAndGarden(testPlant2.getPlantId(),
                        GridItemType.PLANT,
                        gardenService.getGardenById(gardenId).get());
        Assertions.assertTrue(gridItem2AddedToRepository.isPresent());
        Assertions.assertEquals(4, gridItem2AddedToRepository.get().getXCoordinate());
        Assertions.assertEquals(5, gridItem2AddedToRepository.get().getYCoordinate());

    }

    @Test
    @WithMockUser(username = "jhonDoe@Garden2dControllerTest.com")
    void save2DGarden_twoItemsInEachListOnePlantOneDecoration_returnRedirectAndChangePersistence() throws Exception {
        // preparing parameters
        gridItemLocationRepository.deleteAll();
        Long gardenId = userService.getUserByEmail("jhonDoe@Garden2dControllerTest.com").getGardens().get(0)
                .getGardenId();
        Plant testPlant = gardenService.getGardenById(gardenId).get().getPlants().get(0);
        Plant testPlant2 = gardenService.getGardenById(gardenId).get().getPlants().get(1);
        List<String> idList = new ArrayList<>();
        idList.add(testPlant.getPlantId().toString());
        idList.add(testPlant2.getPlantId().toString());
        List<String> typeList = new ArrayList<>();
        typeList.add("PLANT");
        typeList.add("DECORATION");
        List<Double> xCoordList = new ArrayList<>();
        xCoordList.add(2.3);
        xCoordList.add(4.5);
        List<Double> yCoordList = new ArrayList<>();
        yCoordList.add(3.3);
        yCoordList.add(5.5);

        // making call to endpoint
        mockMvc.perform(MockMvcRequestBuilders.post("/2D-garden/" + gardenId + "/save").with(csrf())
                        .param("idList", JSONArray.toJSONString(idList))
                        .param("typeList", JSONArray.toJSONString(typeList))
                        .param("xCoordList", JSONArray.toJSONString(xCoordList))
                        .param("yCoordList", JSONArray.toJSONString(yCoordList)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andReturn();

        // check persistence is updated
        Assertions.assertFalse(gridItemLocationRepository.findAll().isEmpty());
        Assertions.assertEquals(2, gridItemLocationRepository.findAll().size());
        Optional<GridItemLocation> gridItemAddedToRepository = gridItemLocationRepository
                .findGridItemLocationByObjectIdAndItemTypeAndGarden(testPlant.getPlantId(),
                        GridItemType.PLANT,
                        gardenService.getGardenById(gardenId).get());
        Assertions.assertTrue(gridItemAddedToRepository.isPresent());
        Assertions.assertEquals(2, gridItemAddedToRepository.get().getXCoordinate());
        Assertions.assertEquals(3, gridItemAddedToRepository.get().getYCoordinate());
        Optional<GridItemLocation> gridItem2AddedToRepository = gridItemLocationRepository
                .findGridItemLocationByObjectIdAndItemTypeAndGarden(testPlant2.getPlantId(),
                        GridItemType.DECORATION,
                        gardenService.getGardenById(gardenId).get());
        Assertions.assertTrue(gridItem2AddedToRepository.isPresent());
        Assertions.assertEquals(4, gridItem2AddedToRepository.get().getXCoordinate());
        Assertions.assertEquals(5, gridItem2AddedToRepository.get().getYCoordinate());

    }

//    @Test
//    @WithMockUser(username = "jhonDoe@Garden2dControllerTest.com")
//    void save2DGarden_twoDecorationAtSameLocation() throws Exception {
//        gridItemLocationRepository.deleteAll();
//        Long gardenId = userService.getUserByEmail("jhonDoe@Garden2dControllerTest.com").getGardens().get(0)
//                .getGardenId();
//        Plant testPlant = gardenService.getGardenById(gardenId).get().getPlants().get(0);
//        Plant testPlant2 = gardenService.getGardenById(gardenId).get().getPlants().get(1);
//        List<String> idList = new ArrayList<>();
//        idList.add(testPlant.getPlantId().toString());
//        idList.add(testPlant2.getPlantId().toString());
//
//        List<String> typeList = new ArrayList<>();
//        typeList.add("DECORATION");
//        typeList.add("DECORATION");
//        List<Double> xCoordList = new ArrayList<>();
//        xCoordList.add(2.0);
//        xCoordList.add(2.0);
//        List<Double> yCoordList = new ArrayList<>();
//        yCoordList.add(2.0);
//        yCoordList.add(2.0);
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/2D-garden/" + gardenId + "/save").with(csrf())
//                        .param("idList", JSONArray.toJSONString(idList))
//                        .param("typeList", JSONArray.toJSONString(typeList))
//                        .param("xCoordList", JSONArray.toJSONString(xCoordList))
//                        .param("yCoordList", JSONArray.toJSONString(yCoordList)))
//                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
//
//        Assertions.assertTrue(gridItemLocationRepository.findAll().isEmpty());
//        Assertions.assertEquals(2, gridItemLocationRepository.findAll().size());
//
//        Optional<GridItemLocation> gridItemAddedToRepository = gridItemLocationRepository
//                .findGridItemLocationByObjectIdAndItemTypeAndGarden(testPlant.getPlantId(),
//                        GridItemType.DECORATION,
//                        gardenService.getGardenById(gardenId).get());
//
//        Assertions.assertTrue(gridItemAddedToRepository.isPresent());
//        Assertions.assertEquals(2, gridItemAddedToRepository.get().getXCoordinate());
//        Assertions.assertEquals(2, gridItemAddedToRepository.get().getYCoordinate());
//
//        Optional<GridItemLocation> gridItem2AddedToRepository = gridItemLocationRepository
//                .findGridItemLocationByObjectIdAndItemTypeAndGarden(testPlant2.getPlantId(),
//                        GridItemType.DECORATION,
//                        gardenService.getGardenById(gardenId).get());
//
//        Assertions.assertTrue(gridItem2AddedToRepository.isPresent());
//        Assertions.assertEquals(2, gridItem2AddedToRepository.get().getXCoordinate());
//        Assertions.assertEquals(2, gridItem2AddedToRepository.get().getYCoordinate());
//    }


    @Test
    @WithMockUser(username = "jhonDoe@Garden2dControllerTest.com")
    void save2DGarden_differentNumberOfItemsInEachList_return400() throws Exception {
        gridItemLocationRepository.deleteAll();
        Long gardenId = userService.getUserByEmail("jhonDoe@Garden2dControllerTest.com").getGardens().get(0)
                .getGardenId();
        Plant testPlant = gardenService.getGardenById(gardenId).get().getPlants().get(0);
        Plant testPlant2 = gardenService.getGardenById(gardenId).get().getPlants().get(1);
        List<String> idList = new ArrayList<>();
        idList.add(testPlant.getPlantId().toString());
        idList.add(testPlant2.getPlantId().toString());
        List<String> typeList = new ArrayList<>();
        typeList.add("PLANT");
        typeList.add("DECORATION");
        List<Double> xCoordList = new ArrayList<>();
        xCoordList.add(2.3);
        List<Double> yCoordList = new ArrayList<>();

        // making call to endpoint
        mockMvc.perform(MockMvcRequestBuilders.post("/2D-garden/" + gardenId + "/save").with(csrf())
                .param("idList", JSONArray.toJSONString(idList))
                .param("typeList", JSONArray.toJSONString(typeList))
                .param("xCoordList", JSONArray.toJSONString(xCoordList))
                .param("yCoordList", JSONArray.toJSONString(yCoordList)))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();
        Assertions.assertTrue(gridItemLocationRepository.findAll().isEmpty());

    }

    @Test
    @WithMockUser(username = "jhonDoe@Garden2dControllerTest.com")
    void save2DGarden_invalidGardenID_return404() throws Exception {
        gridItemLocationRepository.deleteAll();
        Long gardenId = userService.getUserByEmail("jhonDoe@Garden2dControllerTest.com").getGardens().get(0)
                .getGardenId();
        Plant testPlant = gardenService.getGardenById(gardenId).get().getPlants().get(0);
        List<String> idList = new ArrayList<>();
        idList.add(testPlant.getPlantId().toString());
        List<String> typeList = new ArrayList<>();
        typeList.add("PLANT");
        typeList.add("DECORATION");
        List<Double> xCoordList = new ArrayList<>();
        xCoordList.add(2.3);
        List<Double> yCoordList = new ArrayList<>();
        yCoordList.add(3.3);

        // making call to endpoint
        mockMvc.perform(MockMvcRequestBuilders.post("/2D-garden/" + 9L + "/save").with(csrf())
                .param("idList", JSONArray.toJSONString(idList))
                .param("typeList", JSONArray.toJSONString(typeList))
                .param("xCoordList", JSONArray.toJSONString(xCoordList))
                .param("yCoordList", JSONArray.toJSONString(yCoordList)))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();
        Assertions.assertTrue(gridItemLocationRepository.findAll().isEmpty());

    }

    @Test
    @WithMockUser(username = "jhonDoe@Garden2dControllerTest.com")
    void save2DGarden_UserNotAuthorizedAndGardenDoesNotExist_Return404() throws Exception {
        gridItemLocationRepository.deleteAll();
        List<String> idList = new ArrayList<>();
        List<String> typeList = new ArrayList<>();
        List<Double> xCoordList = new ArrayList<>();
        List<Double> yCoordList = new ArrayList<>();

        mockMvc.perform(MockMvcRequestBuilders.post("/2D-garden/" + 99L + "/save").with(csrf())
                .param("idList", JSONArray.toJSONString(idList))
                .param("typeList", JSONArray.toJSONString(typeList))
                .param("xCoordList", JSONArray.toJSONString(xCoordList))
                .param("yCoordList", JSONArray.toJSONString(yCoordList)))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "jhonDoe@Garden2dControllerTest.com")
    void save2DGarden_updatePreexisting_returnRedirectAndChangePersistence() throws Exception {
        // preparing parameters
        gridItemLocationRepository.deleteAll();
        Long gardenId = userService.getUserByEmail("jhonDoe@Garden2dControllerTest.com").getGardens().get(0)
                .getGardenId();
        Plant testPlant = gardenService.getGardenById(gardenId).get().getPlants().get(0);
        List<String> idList = new ArrayList<>();
        idList.add(testPlant.getPlantId().toString());
        List<String> typeList = new ArrayList<>();
        typeList.add("PLANT");
        List<Double> xCoordList = new ArrayList<>();
        xCoordList.add(2.3);
        List<Double> yCoordList = new ArrayList<>();
        yCoordList.add(3.3);

        // adding initial grid item locations to repository
        GridItemLocation newGridItemLocation = new GridItemLocation(testPlant.getPlantId(), GridItemType.PLANT,
                gardenService.getGardenById(gardenId).get(), 9, 8);
        gridItemLocationService.addGridItemLocation(newGridItemLocation);
        Assertions.assertFalse(gridItemLocationRepository.findAll().isEmpty());
        Assertions.assertEquals(1, gridItemLocationRepository.findAll().size());
        Optional<GridItemLocation> gridItemAddedToRepository = gridItemLocationRepository
                .findGridItemLocationByObjectIdAndItemTypeAndGarden(testPlant.getPlantId(),
                        GridItemType.PLANT,
                        gardenService.getGardenById(gardenId).get());
        Assertions.assertTrue(gridItemAddedToRepository.isPresent());
        Assertions.assertEquals(9, gridItemAddedToRepository.get().getXCoordinate());
        Assertions.assertEquals(8, gridItemAddedToRepository.get().getYCoordinate());

        // making call to endpoint
        mockMvc.perform(MockMvcRequestBuilders.post("/2D-garden/" + gardenId + "/save").with(csrf())
                .param("idList", JSONArray.toJSONString(idList))
                .param("typeList", JSONArray.toJSONString(typeList))
                .param("xCoordList", JSONArray.toJSONString(xCoordList))
                .param("yCoordList", JSONArray.toJSONString(yCoordList)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andReturn();

        // checking grid item locations are updated
        Assertions.assertFalse(gridItemLocationRepository.findAll().isEmpty());
        Assertions.assertEquals(1, gridItemLocationRepository.findAll().size());
        gridItemAddedToRepository = gridItemLocationRepository
                .findGridItemLocationByObjectIdAndItemTypeAndGarden(
                        testPlant.getPlantId(), GridItemType.PLANT,
                        gardenService.getGardenById(gardenId).get());
        Assertions.assertTrue(gridItemAddedToRepository.isPresent());
        Assertions.assertEquals(2, gridItemAddedToRepository.get().getXCoordinate());
        Assertions.assertEquals(3, gridItemAddedToRepository.get().getYCoordinate());
    }

    @Test
    @WithMockUser(username = "jhonDoe@Garden2dControllerTest.com")
    void save2DGarden_updatePreexistingDecoration_returnRedirectAndChangePersistence() throws Exception {
        // preparing parameters
        gridItemLocationRepository.deleteAll();
        Long gardenId = userService.getUserByEmail("jhonDoe@Garden2dControllerTest.com").getGardens().get(0)
                .getGardenId();
        Plant testPlant = gardenService.getGardenById(gardenId).get().getPlants().get(0);
        List<String> idList = new ArrayList<>();
        idList.add(testPlant.getPlantId().toString());
        List<String> typeList = new ArrayList<>();
        typeList.add("DECORATION");
        List<Double> xCoordList = new ArrayList<>();
        xCoordList.add(2.3);
        List<Double> yCoordList = new ArrayList<>();
        yCoordList.add(3.3);

        // adding initial grid item locations to repository
        GridItemLocation newGridItemLocation = new GridItemLocation(testPlant.getPlantId(), GridItemType.DECORATION,
                gardenService.getGardenById(gardenId).get(), 9, 8);
        gridItemLocationService.addGridItemLocation(newGridItemLocation);
        Assertions.assertFalse(gridItemLocationRepository.findAll().isEmpty());
        Assertions.assertEquals(1, gridItemLocationRepository.findAll().size());
        Optional<GridItemLocation> gridItemAddedToRepository = gridItemLocationRepository
                .findGridItemLocationByObjectIdAndItemTypeAndGarden(testPlant.getPlantId(),
                        GridItemType.DECORATION,
                        gardenService.getGardenById(gardenId).get());
        Assertions.assertTrue(gridItemAddedToRepository.isPresent());
        Assertions.assertEquals(9, gridItemAddedToRepository.get().getXCoordinate());
        Assertions.assertEquals(8, gridItemAddedToRepository.get().getYCoordinate());

        // making call to endpoint
        mockMvc.perform(MockMvcRequestBuilders.post("/2D-garden/" + gardenId + "/save").with(csrf())
                        .param("idList", JSONArray.toJSONString(idList))
                        .param("typeList", JSONArray.toJSONString(typeList))
                        .param("xCoordList", JSONArray.toJSONString(xCoordList))
                        .param("yCoordList", JSONArray.toJSONString(yCoordList)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andReturn();

        // checking grid item locations are updated
        Assertions.assertFalse(gridItemLocationRepository.findAll().isEmpty());
        Assertions.assertEquals(1, gridItemLocationRepository.findAll().size());
        gridItemAddedToRepository = gridItemLocationRepository
                .findGridItemLocationByObjectIdAndItemTypeAndGarden(
                        testPlant.getPlantId(), GridItemType.DECORATION,
                        gardenService.getGardenById(gardenId).get());
        Assertions.assertTrue(gridItemAddedToRepository.isPresent());
        Assertions.assertEquals(2, gridItemAddedToRepository.get().getXCoordinate());
        Assertions.assertEquals(3, gridItemAddedToRepository.get().getYCoordinate());
    }

    @Test
    @WithMockUser(username = "jhonDoe@Garden2dControllerTest.com")
    void save2DGarden_twoCopiesOfSamePlantAtDifferentLocations_returnRedirectAndChangePersistence()
            throws Exception {
        // preparing parameters
        gridItemLocationRepository.deleteAll();
        Long gardenId = userService.getUserByEmail("jhonDoe@Garden2dControllerTest.com").getGardens().get(0)
                .getGardenId();
        Plant testPlant = gardenService.getGardenById(gardenId).get().getPlants().get(0);
        List<String> idList = new ArrayList<>();
        idList.add(testPlant.getPlantId().toString());
        idList.add(testPlant.getPlantId().toString());
        List<String> typeList = new ArrayList<>();
        typeList.add("PLANT");
        typeList.add("PLANT");
        List<Double> xCoordList = new ArrayList<>();
        xCoordList.add(2.3);
        xCoordList.add(4.5);
        List<Double> yCoordList = new ArrayList<>();
        yCoordList.add(3.3);
        yCoordList.add(5.5);

        // making call to endpoint
        mockMvc.perform(MockMvcRequestBuilders.post("/2D-garden/" + gardenId + "/save").with(csrf())
                .param("idList", JSONArray.toJSONString(idList))
                .param("typeList", JSONArray.toJSONString(typeList))
                .param("xCoordList", JSONArray.toJSONString(xCoordList))
                .param("yCoordList", JSONArray.toJSONString(yCoordList)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andReturn();

        // check persistence is updated
        Assertions.assertFalse(gridItemLocationRepository.findAll().isEmpty());
        Assertions.assertEquals(2, gridItemLocationRepository.findAll().size());
        List<GridItemLocation> gridItemsAddedToRepository = gridItemLocationRepository.findAll().stream()
                .toList();
        Assertions.assertEquals(2, gridItemsAddedToRepository.get(0).getXCoordinate());
        Assertions.assertEquals(3, gridItemsAddedToRepository.get(0).getYCoordinate());
        Assertions.assertEquals(4, gridItemsAddedToRepository.get(1).getXCoordinate());
        Assertions.assertEquals(5, gridItemsAddedToRepository.get(1).getYCoordinate());
    }

    @Test
    @WithMockUser(username = "jhonDoe@Garden2dControllerTest.com")
    void save2DGarden_twoSameDecorationAtDifferentLocations_returnRedirectAndChangePersistence()
            throws Exception {
        // preparing parameters
        gridItemLocationRepository.deleteAll();
        Long gardenId = userService.getUserByEmail("jhonDoe@Garden2dControllerTest.com").getGardens().get(0)
                .getGardenId();
        Plant testPlant = gardenService.getGardenById(gardenId).get().getPlants().get(0);
        List<String> idList = new ArrayList<>();
        idList.add(testPlant.getPlantId().toString());
        idList.add(testPlant.getPlantId().toString());
        List<String> typeList = new ArrayList<>();
        typeList.add("DECORATION");
        typeList.add("DECORATION");
        List<Double> xCoordList = new ArrayList<>();
        xCoordList.add(2.3);
        xCoordList.add(4.5);
        List<Double> yCoordList = new ArrayList<>();
        yCoordList.add(3.3);
        yCoordList.add(5.5);

        // making call to endpoint
        mockMvc.perform(MockMvcRequestBuilders.post("/2D-garden/" + gardenId + "/save").with(csrf())
                        .param("idList", JSONArray.toJSONString(idList))
                        .param("typeList", JSONArray.toJSONString(typeList))
                        .param("xCoordList", JSONArray.toJSONString(xCoordList))
                        .param("yCoordList", JSONArray.toJSONString(yCoordList)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andReturn();

        // check persistence is updated
        Assertions.assertFalse(gridItemLocationRepository.findAll().isEmpty());
        Assertions.assertEquals(2, gridItemLocationRepository.findAll().size());
        List<GridItemLocation> gridItemsAddedToRepository = gridItemLocationRepository.findAll().stream()
                .toList();
        Assertions.assertEquals(2, gridItemsAddedToRepository.get(0).getXCoordinate());
        Assertions.assertEquals(3, gridItemsAddedToRepository.get(0).getYCoordinate());
        Assertions.assertEquals(4, gridItemsAddedToRepository.get(1).getXCoordinate());
        Assertions.assertEquals(5, gridItemsAddedToRepository.get(1).getYCoordinate());
    }


    @Test
    @WithMockUser(username = "jhonDoe@Garden2dControllerTest.com")
    void save2DGarden_updatePreexistingWithDuplicates_returnRedirectAndChangePersistence() throws Exception {
        // preparing parameters
        gridItemLocationRepository.deleteAll();
        Long gardenId = userService.getUserByEmail("jhonDoe@Garden2dControllerTest.com").getGardens().get(0)
                .getGardenId();
        Plant testPlant = gardenService.getGardenById(gardenId).get().getPlants().get(0);
        List<String> idList = new ArrayList<>();
        idList.add(testPlant.getPlantId().toString());
        idList.add(testPlant.getPlantId().toString());
        List<String> typeList = new ArrayList<>();
        typeList.add("PLANT");
        typeList.add("PLANT");
        List<Double> xCoordList = new ArrayList<>();
        xCoordList.add(2.3);
        xCoordList.add(4.3);
        List<Double> yCoordList = new ArrayList<>();
        yCoordList.add(3.3);
        yCoordList.add(5.3);

        // adding initial grid item locations to repository
        GridItemLocation newGridItemLocation = new GridItemLocation(testPlant.getPlantId(), GridItemType.PLANT,
                gardenService.getGardenById(gardenId).get(), 9, 8);
        gridItemLocationService.addGridItemLocation(newGridItemLocation);
        Assertions.assertFalse(gridItemLocationRepository.findAll().isEmpty());
        Assertions.assertEquals(1, gridItemLocationRepository.findAll().size());
        Optional<GridItemLocation> gridItemAddedToRepository = gridItemLocationRepository
                .findGridItemLocationByObjectIdAndItemTypeAndGarden(testPlant.getPlantId(),
                        GridItemType.PLANT,
                        gardenService.getGardenById(gardenId).get());
        Assertions.assertTrue(gridItemAddedToRepository.isPresent());
        Assertions.assertEquals(9, gridItemAddedToRepository.get().getXCoordinate());
        Assertions.assertEquals(8, gridItemAddedToRepository.get().getYCoordinate());

        // making call to endpoint
        mockMvc.perform(MockMvcRequestBuilders.post("/2D-garden/" + gardenId + "/save").with(csrf())
                .param("idList", JSONArray.toJSONString(idList))
                .param("typeList", JSONArray.toJSONString(typeList))
                .param("xCoordList", JSONArray.toJSONString(xCoordList))
                .param("yCoordList", JSONArray.toJSONString(yCoordList)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andReturn();

        // checking grid item locations are updated
        Assertions.assertFalse(gridItemLocationRepository.findAll().isEmpty());
        Assertions.assertEquals(2, gridItemLocationRepository.findAll().size());
        List<GridItemLocation> gridItemsAddedToRepository = gridItemLocationRepository.findAll().stream()
                .toList();
        Assertions.assertEquals(2, gridItemsAddedToRepository.get(0).getXCoordinate());
        Assertions.assertEquals(3, gridItemsAddedToRepository.get(0).getYCoordinate());
        Assertions.assertEquals(4, gridItemsAddedToRepository.get(1).getXCoordinate());
        Assertions.assertEquals(5, gridItemsAddedToRepository.get(1).getYCoordinate());
    }

}
