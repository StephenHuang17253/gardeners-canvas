package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.HomePageController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.HomePageLayoutRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class HomePageControllerIntegrationTest {

    @Autowired
    private HomePageController homePageController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    public HomePageLayoutRepository homePageLayoutRepository;

    UserService userService;

    @BeforeEach
    void before_or_after_all() {
        if (userService == null) {
            userService = new UserService(passwordEncoder, userRepository, homePageLayoutRepository);
        }

        String userEmail = "johndoe.test@email.com";
        if (!userService.emailInUse(userEmail)) {
            User user = new User("John", "Doe", userEmail, null);
            userService.addUser(user, "AlphabetSoup10!");
        }
    }

    @Test
    void controllerLoads() {
        assertNotNull(homePageController);
    }

    @Test
    void mvcMockIsAlive() throws Exception {
        this.mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void getMapping_root_redirectsToHomePage() throws Exception {
        this.mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("./home"));
    }

    @Test
    void getMappingNotLoggedIn_Invalid_requestForbidden() throws Exception {
        this.mockMvc.perform(get("/asdassdas"))
                .andExpect(status().isForbidden()); // Should be 403 as the user is not logged in
    }

    @Test
    @WithMockUser(username = "johndoe.test@email.com")
    void getMappingLoggedIn_home_containsNames() throws Exception {

        this.mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("username", is("John Doe")));
    }

}
