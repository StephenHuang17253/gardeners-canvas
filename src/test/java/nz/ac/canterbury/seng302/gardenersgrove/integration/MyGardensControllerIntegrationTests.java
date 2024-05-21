package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MyGardensControllerIntegrationTests {
    @Autowired
    private GardenService gardenService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private PlantService plantService;
    private final MockMvc mockMvc;
    private List<Garden> gardenList = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
    LocalDate date = LocalDate.parse("01/01/2001", formatter);

    @Autowired
    public MyGardensControllerIntegrationTests(MockMvc mockMvc){
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
