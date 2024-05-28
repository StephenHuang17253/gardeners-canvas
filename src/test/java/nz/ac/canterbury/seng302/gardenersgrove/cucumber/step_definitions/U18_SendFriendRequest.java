package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import nz.ac.canterbury.seng302.gardenersgrove.controller.ManageFriendsController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.model.FriendModel;
import nz.ac.canterbury.seng302.gardenersgrove.model.RequestFriendModel;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class U18_SendFriendRequest {

    Logger logger = LoggerFactory.getLogger(U18_SendFriendRequest.class);

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


    public static GardenService gardenService;

    public static UserService userService;

    @Autowired
    public FriendshipService friendshipService;

    @Autowired
    public FileService fileService;

    private MvcResult mvcResult;

    private String input;

    private List<FriendModel> friendsList = new ArrayList<>();

    // Setup
    @Before
    public void before_or_after_all() {

        userService = new UserService(passwordEncoder, userRepository);
        gardenService = new GardenService(gardenRepository, userService);
        friendshipService = new FriendshipService(friendshipRepository, userService);

        ManageFriendsController manageFriendsController = new ManageFriendsController(friendshipService,
                securityService, fileService, userService);
        // Allows us to bypass spring security
        MOCK_MVC = MockMvcBuilders.standaloneSetup(manageFriendsController).build();

    }

    // AC1
    @When("I cancel my friend request to {string}")
    public void i_cancel_my_friend_request_to(String receiverEmail) throws Exception {
        User user = userService.getUserByEmail(receiverEmail);

        mvcResult = MOCK_MVC.perform(
                        MockMvcRequestBuilders
                                .post("/manage-friends")
                                .param("pendingFriendId", String.valueOf(user.getId()))
                                .param("acceptedFriend", "false")
                                .param("activeTab", "pending"))
                .andExpect(status().is3xxRedirection()).andReturn();
    }
    // AC1
    @When("There is user {string} who is logged in with {string}")
    public void there_is_user_who_is_logged_in(String userEmail, String userPassword) throws Exception {
        MOCK_MVC.perform(
                        post("/login")
                                .param("emailAddress", userEmail)
                                .param("password", userPassword))
                .andExpect(MockMvcResultMatchers.redirectedUrl("/home"));
    }
    // AC1
    @Then("I cannot see or accept the friend request from {string}")
    public void they_cannot_see_or_accept_my_friend_request(String senderEmail) throws Exception {
        User user = userService.getUserByEmail(senderEmail);

        String userName = user.getFirstName() + ' ' + user.getLastName();

        mvcResult = MOCK_MVC.perform(MockMvcRequestBuilders.get("/manage-friends"))
                .andExpect(status().isOk()).andReturn();

        List<RequestFriendModel> result = (List<RequestFriendModel>) mvcResult.getModelAndView().getModelMap().getAttribute("pendingFriends");
        Assertions.assertNotNull(result);
        RequestFriendModel requestFriendModel = result.get(result.size() - 1);
        Assertions.assertNotEquals(userName, requestFriendModel.getFriendName());
    }




}
