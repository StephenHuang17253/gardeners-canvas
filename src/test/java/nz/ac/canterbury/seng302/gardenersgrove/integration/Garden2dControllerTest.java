package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.Garden2DController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.model.GardenDetailModel;
import nz.ac.canterbury.seng302.gardenersgrove.repository.HomePageLayoutRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.util.PlantCategory;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    private List<Garden> gardenList = new ArrayList<>();
    private List<Plant> plantList = new ArrayList<>();

    User mockUser = new User("John", "Test", "profile.user.test@ProfileController.com", LocalDate.now());

//    @InjectMocks
//    private static Garden2DController Garden2DController;

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
        for(int i = 0; i < COUNT_PER_PAGE+1; i++) {
            plantList.add(plantService.addPlant("testName1",
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
                .perform(MockMvcRequestBuilders.get("/2D-garden/"+MAX_LONG))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "janeDoe@Garden2dControllerTest.com")
    void Get2DGarden_UserNotAuthorizedAndGardenExists_Return403() throws Exception {
        Long gardenId = gardenList.get(0).getGardenId();
        mockMvc
                .perform(MockMvcRequestBuilders.get("/2D-garden/"+gardenId))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "janeDoe@email.com")
    void GetGardenDetailsPage_UserNotAuthorizedAndGardenDoesNotExist_Return404() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/2D-garden/"+MAX_LONG))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(username = "jhonDoe@Garden2dControllerTest.com")
    void Get2DGarden_UserAuthorized_Return200() throws Exception {
        Garden garden = gardenList.get(0);
        Long gardenId = gardenList.get(0).getGardenId();
        List<Plant> expectedPlants = plantList.subList(0, COUNT_PER_PAGE);
        MvcResult mvcResult = mockMvc
                .perform(MockMvcRequestBuilders.get("/2D-garden/"+gardenId))
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



}
