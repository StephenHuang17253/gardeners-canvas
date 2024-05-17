package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardensController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.ManageFriendsController;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class U17_SendFriendRequest {

    Logger logger = LoggerFactory.getLogger(U17_SendFriendRequest.class);

    public static MockMvc MOCK_MVC;

    @Autowired
    public GardenRepository gardenRepository;

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

    @MockBean
    private LocationService locationService;

    public static GardenService gardenService;

    public static UserService userService;

    @Autowired
    public FriendshipService friendshipService;

    @Autowired
    public FileService fileService;

    private MvcResult mvcResult;


    @Before
    public void before_or_after_all() {

        userService = new UserService(passwordEncoder, userRepository);
        gardenService = new GardenService(gardenRepository, userService);
        friendshipService = new FriendshipService(friendshipRepository, userService);

        ManageFriendsController manageFriendsController = new ManageFriendsController(friendshipService,
                securityService, fileService);
        // Allows us to bypass spring security
        MOCK_MVC = MockMvcBuilders.standaloneSetup(manageFriendsController).build();

    }

    @When("I click on the 'Manage Friends' button")
    public void i_click_on_button() throws Exception {
        mvcResult = MOCK_MVC.perform(MockMvcRequestBuilders.get("/manage-friends"))
                .andExpect(status().isOk()).andReturn();
    }
    @Then("I am shown a 'Manage Friends' page")
    public void i_am_shown_a_manage_friends_page() {
        String pageName = mvcResult.getModelAndView().getViewName();
        logger.info(pageName);
        Assertions.assertEquals("manageFriendsPage", pageName);
    }
}
