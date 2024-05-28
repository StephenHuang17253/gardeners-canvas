package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.model.FriendModel;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendshipService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class GardensControllerIntegrationTests {

    @Autowired
    private GardenService gardenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PlantService plantService;

    @Autowired
    private FriendshipService friendshipService;

    private final MockMvc mockMvc;

    private List<Garden> gardenList = new ArrayList<>();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);

    LocalDate date = LocalDate.parse("01/01/2001", formatter);

    User user1;

    User user2;

    User user3;

    User user4;

    List<FriendModel> friendsList;

    private final Long MAX_LONG = 10000L;

    @Autowired
    public GardensControllerIntegrationTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeAll
    void before_or_after_all() {
        gardenList = new ArrayList<>();
        userRepository.deleteAll();

        user1 =  new User("John", "Doe", "johnDoe@GardensControllerIntegrationTest.com", LocalDate.of(2003,5,2));
        user2 =  new User("Jane", "Doe", "janeDoe@GardensControllerIntegrationTest.com", LocalDate.of(2003,5,2));
        user3 =  new User("Bruce", "Wayne", "bruceWayne@GardensControllerIntegrationTest.com", LocalDate.of(2003,5,2));
        user4 =  new User("Test", "Doe", "testDoe@GardensControllerIntegrationTest.com", LocalDate.of(2003,5,2));

        userService.addUser(user1,"1es1P@ssword");
        userService.addUser(user2,"1es1P@ssword");
        userService.addUser(user3,"1es1P@ssword");
        userService.addUser(user4,"1es1P@ssword");

        Friendship friendship1 = friendshipService.addFriendship(user1,user2);
        Friendship friendship2 = friendshipService.addFriendship(user3,user1);
        friendshipService.updateFriendShipStatus(friendship1.getId(), FriendshipStatus.ACCEPTED);
        friendshipService.updateFriendShipStatus(friendship2.getId(), FriendshipStatus.ACCEPTED);

        Garden garden1 = new Garden(
                "John's Garden",
                "",
                "114 Ilam Road",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                10.0,
                false,
                userService.getUserByEmail("johnDoe@GardensControllerIntegrationTest.com"));
        Garden garden2 = new Garden(
                "Jane's Garden",
                "",
                "20 Kirkwood Avenue",
                "Upper Riccarton",
                "Christchurch",
                "8041",
                "New Zealand",
                20.0,
                false,
                userService.getUserByEmail("janeDoe@GardensControllerIntegrationTest.com"));
        gardenService.addGarden(garden1);
        gardenService.addGarden(garden2);
        plantService.addPlant("Java Tree", 1, "Grows Java Plums", date, garden2.getGardenId());
        plantService.addPlant("Java Tree", 1, "Grows Java Plums", date, garden2.getGardenId());
        gardenList.add(gardenService.getGardenById(1L).get());
        gardenList.add(gardenService.getGardenById(2L).get());

    }

    @BeforeEach
    void clear_repo() {
        friendsList = new ArrayList<>();
    }

    @Test
    public void GetMyGardens_UserNotAuthorized_Return403() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "johnDoe@GardensControllerIntegrationTest.com")
    public void GetMyGardens_UserAuthorized_Return200() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userGardens", gardenService.getAllUsersGardens(1L));
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens").session(session))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithAnonymousUser
    public void GetGardenDetailsPage_UserNotAuthenticated_Return403() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/1"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "janeDoe@GardensControllerIntegrationTest.com")
    public void GetGardenDetailsPage_UserNotAuthorizedAndGardenDoesNotExist_Return404() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/4"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(username = "bruceWayne@GardensControllerIntegrationTest.com")
    public void GetGardenDetailsPage_UserNotAuthorizedAndGardenExists_Return403() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/1"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "johnDoe@GardensControllerIntegrationTest.com")
    public void GetGardenDetailsPage_UserAuthorizedAndGardenExists_Return200() throws Exception {
        Garden garden = gardenList.get(0);
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("gardenName", is(garden.getGardenName())))
                .andExpect(MockMvcResultMatchers.model().attribute("gardenLocation", is(garden.getGardenLocation())))
                .andExpect(MockMvcResultMatchers.model().attribute("gardenSize", is(garden.getGardenSize())))
                .andExpect(MockMvcResultMatchers.model().attribute("totalPlants", is(garden.getPlants().size())))
                .andExpect(MockMvcResultMatchers.model().attribute("makeGardenPublic", is(garden.getIsPublic())));
        ;
    }

    @Test
    @WithMockUser(username = "janeDoe@GardensControllerIntegrationTest.com")
    public void GetGardenDetailsPage_UserAuthorizedAndGardenExistsWithPlants_Return200() throws Exception {
        Garden garden = gardenList.get(1);
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/2"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("gardenName", is(garden.getGardenName())))
                .andExpect(MockMvcResultMatchers.model().attribute("gardenLocation", is(garden.getGardenLocation())))
                .andExpect(MockMvcResultMatchers.model().attribute("gardenSize", is(garden.getGardenSize())))
                .andExpect(MockMvcResultMatchers.model().attribute("totalPlants", is(garden.getPlants().size())))
                .andExpect(MockMvcResultMatchers.model().attribute("makeGardenPublic", is(garden.getIsPublic())));
        ;
    }

    @Test
    @WithMockUser(username = "johnDoe@GardensControllerIntegrationTest.com")
    public void GetGardenDetailsPage_UserAuthorizedAndGardenExistsAndIsPublic_Return200() throws Exception {
        Garden garden = gardenList.get(0);
        // make an initial request to ensure garden is private as set in @before
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("makeGardenPublic", false));

        // set the garden to public
        mockMvc.perform(MockMvcRequestBuilders.post("/my-gardens/1/public").with(csrf()).param("makeGardenPublic", "true"))
                        .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

        // check the garden is now public
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("gardenName", is(garden.getGardenName())))
                .andExpect(MockMvcResultMatchers.model().attribute("gardenLocation", is(garden.getGardenLocation())))
                .andExpect(MockMvcResultMatchers.model().attribute("gardenSize", is(garden.getGardenSize())))
                .andExpect(MockMvcResultMatchers.model().attribute("totalPlants", is(garden.getPlants().size())))
                .andExpect(MockMvcResultMatchers.model().attribute("makeGardenPublic", true));
    }

    @Test
    public void GetFriendGardens_UserNotAuthorized_Return403() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get(String.format("/%d/gardens", user1.getId())))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "johnDoe@GardensControllerIntegrationTest.com")
    public void GetFriendGardens_UserAuthorizedNotFriend_Return403() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get(String.format("/%d/gardens", user4.getId())))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "johnDoe@GardensControllerIntegrationTest.com")
    public void GetFriendGardens_UserAuthorizedFriendIdInvalid_Return404() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get(String.format("/%d/gardens", MAX_LONG)))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "johnDoe@GardensControllerIntegrationTest.com")
    public void GetFriendGardens_UserAuthorizedIsFriend_Return200() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get(String.format("/%d/gardens", user2.getId())))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }



}
