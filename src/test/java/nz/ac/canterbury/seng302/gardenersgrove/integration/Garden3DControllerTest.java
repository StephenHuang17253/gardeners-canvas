package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.HomePageLayoutRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class Garden3DControllerTest {
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

    private static final Long MAX_LONG = 10000L;
    private static final int COUNT_PER_PAGE = 6;

    @Autowired
    Garden3DControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private static User user1;
    private static User user2;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
    LocalDate date = LocalDate.parse("01/01/2001", formatter);

    @BeforeAll
    void before_each() {
        userService = new UserService(passwordEncoder, userRepository, homePageLayoutRepository);
        user1 = new User("John", "Doe", "johnDoe@Garden3dControllerTest.com", date);
        user2 = new User("Jane", "Doe", "janeDoe@Garden3dControllerTest.com", date);
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
                userService.getUserByEmail(user1.getEmailAddress())));
        gardenList.add(garden2);
    }

    @Test
    @WithMockUser(username = "janeDoe@email.com")
    void Get3DGarden_UserNotAuthorisedAndGardenDoesNotExist_Return404() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/3D-garden/{gardenId}",MAX_LONG))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(username = "janeDoe@Garden3dControllerTest.com")
    void Get3DGarden_UserNotOwnerOrFriendGardenExists_Return403() throws Exception {
        Long gardenId = gardenList.get(0).getGardenId();
        mockMvc
                .perform(MockMvcRequestBuilders.get("/3D-garden/{gardenId}", gardenId))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "johnDoe@Garden3dControllerTest.com")
    void Get3DGarden_UserAuthorised_Return200() throws Exception {
        Garden garden = gardenList.get(0);
        Long gardenId = garden.getGardenId();
        mockMvc.perform(MockMvcRequestBuilders.get("/3D-garden/{gardenId}", gardenId))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

    }

    @Test
    @WithMockUser(username = "janeDoe@Garden3dControllerTest.com")
    void Get3DGarden_UserNotOwnerButGardenIsPublic_Return200() throws Exception {
        Garden garden = gardenList.get(1);
        Long gardenId = garden.getGardenId();
        List<Plant> expectedPlants = plantList.subList(0, COUNT_PER_PAGE);
        expectedPlants.sort(Comparator.comparing(Plant::getPlantName));
        mockMvc.perform(MockMvcRequestBuilders.get("/3D-garden/{gardenId}", gardenId))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

    }

}

