package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenFormController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.LoginPageController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.LocationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.http.WebSocketHandshakeException;
import java.util.Objects;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
public class LoginUser {
    public static MockMvc MOCK_MVC;

    @Autowired
    public GardenRepository gardenRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationManager authenticationManager;

    @Autowired
    public SecurityService securityService;

    public static GardenService gardenService;

    public static UserService userService;
    @Before
    public void before_or_after_all() {
        userService = new UserService(passwordEncoder, userRepository);
        gardenService = new GardenService(gardenRepository, userService);

        LoginPageController loginPageController = new LoginPageController(userService, authenticationManager, gardenService);
        // Allows us to bypass spring security
        MOCK_MVC = MockMvcBuilders.standaloneSetup(loginPageController).build();


    }

    @When("I as user {string} am logged in with {string}")
    public void iAsUserAmLoggedInWith(String userEmail, String userPassword) throws Exception {
        MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .post("/login")
                        .param("email", userEmail)
                        .param("password", userPassword)
        ).andExpect(MockMvcResultMatchers.redirectedUrl("/landing"));
    }

}
