package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
public class MyGardensControllerIntegrationTests {
    @Autowired
    private GardenRepository gardenRepository;
    @Autowired
    private GardenService gardenService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
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
        Garden garden1 = new Garden("John's Garden", "John's Backyard", 15, user1);
        Garden garden2 = new Garden("John's Garden", "John's Backyard", 15, user1);
        Garden garden3 = new Garden("Jane's Garden", "Jane's Backyard", 20, user2);
        gardenList.add(garden1);
        gardenList.add(garden2);
        gardenList.add(garden3);
        gardenRepository.saveAll(gardenList);
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
    public void GetGardenDetailsPage_UserNotAuthticated_Return403() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/1=John's Garden"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
    @Test
    @WithMockUser(username = "johnDoe@email.com")
    public void GetGardenDetailsPage_UserAuthorized_Return200() throws Exception {
        Garden garden = gardenList.get(0);
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/1=John's Garden"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("gardenName", is(garden.getGardenName())));
    }
    @Test
    @WithMockUser(username = "janeDoe@email.com")
    public void GetGardenDetailsPage_UserNotAuthorized_Return403() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/my-gardens/1=John's Garden"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

}
