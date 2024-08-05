package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.AccountController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardensController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.PublicGardensController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.*;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class U23_DeactivateAccount {
    public static MockMvc mockMVCPublicGardens;

    public static MockMvc mockMVCAccount;

    public static MockMvc mockMVCGardens;

    @Autowired
    public GardenRepository gardenRepository;

    @Autowired
    public GardenTagRepository gardenTagRepository;

    @Autowired
    public GardenTagRelationRepository gardenTagRelationRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public FriendshipRepository friendshipRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationManager authenticationManager;

    @Autowired
    public SecurityService securityService;

    @Autowired
    public ObjectMapper objectMapper;

    public static GardenService gardenService;

    public static UserService userService;

    public static PlantService plantService;

    public static WeatherService weatherService;

    @Autowired
    public FriendshipService friendshipService;

    @Autowired
    public ProfanityService profanityService;

    @Autowired
    public EmailService emailService;

    @Autowired
    private TokenService tokenService;

    public static GardenTagService gardenTagService;


    private MvcResult mvcResultGardens;
    private MvcResult mvcResultAccount;

    private Garden garden;

    private User user;

    @Before
    public void before_or_after_all() {

        userService = new UserService(passwordEncoder, userRepository);
        gardenService = new GardenService(gardenRepository, userService);
        friendshipService = new FriendshipService(friendshipRepository, userService);
        gardenTagService = new GardenTagService(gardenTagRepository, gardenTagRelationRepository);

        PublicGardensController publicGardensController = new PublicGardensController(gardenService,
                securityService,
                friendshipService, gardenTagService);

        mockMVCPublicGardens = MockMvcBuilders.standaloneSetup(publicGardensController).build();

        GardensController gardensController = new GardensController(gardenService, securityService,
                plantService, weatherService, objectMapper, gardenTagService, profanityService, userService);

        AccountController loginPageController = new AccountController(userService, authenticationManager,
                emailService,
                tokenService, gardenService, securityService);

        mockMVCAccount = MockMvcBuilders.standaloneSetup(loginPageController).build();
        mockMVCGardens = MockMvcBuilders.standaloneSetup(gardensController).build();


    }

    @When("I attempt to enter tag {string}")
    public void iAttemptToEnterTag(String tagName) throws Exception {
        garden = gardenService.getGardens().get(0);
        mvcResultGardens = mockMVCGardens.perform(
                MockMvcRequestBuilders
                        .post("/my-gardens/{gardenId}/tag", garden.getGardenId())
                        .param("tag", tagName)
                ).andExpect(status().is3xxRedirection())
                .andReturn();
    }

    @Then("I am logged out")
    public void iAmLoggedOut() throws Exception {
        Assertions.assertEquals("/login", mvcResultGardens.getFlashMap().getTargetRequestPath());
    }

    @And("A login error message is displayed {string}")
    public void aLoginMessageIsDisplayed(String errorMessage){
        Assertions.assertEquals(errorMessage, mvcResultGardens.getFlashMap().get("message"));
    }

    @And("I {string} get banned")
    public void iGetBanned(String email){
        Assertions.assertTrue(userService.getUserByEmail(email).isBanned());
    }
    @And("I {string} am banned for {int} days")
    public void iAmBanned(String email, int days) {
        userService.banUser(userService.getUserByEmail(email), days);
    }
    @When("I {string} try to login with password {string}")
    public void iTryToLoginWithPassword(String email, String password) throws Exception {
        mvcResultAccount = mockMVCAccount.perform(
                        post("/login")
                                .param("emailAddress", email)
                                .param("password", password)).andReturn();
    }
    @Then("I receive an error message {string}")
    public void iReceiveErrorMessage(String errorMessage) {
        Assertions.assertEquals(errorMessage, mvcResultAccount.getModelAndView().getModel().get("message"));
        System.out.println("Current time:"+LocalDateTime.now());
        System.out.println("Yesterday"+LocalDateTime.now().minusDays(1));

    }

    @When("It is the eighth day of my account {string} being blocked")
    public void itIsTheEighthDayOfMyAccountBeingBlocked(String email){
        user = userService.getUserByEmail(email);
        user.setLastBanDate(null);
        userRepository.save(user);
    }

    @Then("On day 8 I {string} am able to log in with {string}")
    public void iAmAbleToLogIn(String email, String password) throws Exception {
        mockMVCAccount.perform(
                post("/login")
                        .param("emailAddress", email)
                        .param("password", password)).andExpect(redirectedUrl("/home"));


        Assertions.assertFalse(user.isBanned());
    }


}
