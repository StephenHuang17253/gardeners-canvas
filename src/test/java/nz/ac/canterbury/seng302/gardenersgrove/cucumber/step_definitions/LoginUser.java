package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.AccountController;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.HomePageLayoutRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
public class LoginUser {
    public static MockMvc mockMVC;

    @Autowired
    public GardenRepository gardenRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public HomePageLayoutRepository homePageLayoutRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationManager authenticationManager;

    @Autowired
    public SecurityService securityService;

    public static GardenService gardenService;

    public static UserService userService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TokenService tokenService;

    @Before
    public void before_or_after_all() {
        userService = new UserService(passwordEncoder, userRepository, homePageLayoutRepository);
        gardenService = new GardenService(gardenRepository, userService);

        AccountController loginPageController = new AccountController(userService, authenticationManager,
                emailService,
                tokenService, gardenService, securityService);
        // Allows us to bypass spring security
        mockMVC = MockMvcBuilders.standaloneSetup(loginPageController).build();

    }

    @When("I as user {string} am logged in with {string}")
    public void iAsUserAmLoggedInWith(String userEmail, String userPassword) throws Exception {
        mockMVC.perform(
                post("/login")
                        .param("emailAddress", userEmail)
                        .param("password", userPassword))
                .andExpect(MockMvcResultMatchers.redirectedUrl("/home"));
    }

    @Given("I am not logged in")
    public void iAmNotLoggedIn() throws Exception {
        mockMVC.perform(
                post("/logout"));
    }

}
