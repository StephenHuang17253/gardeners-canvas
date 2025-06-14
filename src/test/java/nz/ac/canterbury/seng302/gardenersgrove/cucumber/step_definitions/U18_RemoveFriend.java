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
import nz.ac.canterbury.seng302.gardenersgrove.repository.HomePageLayoutRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class U18_RemoveFriend {

    public static MockMvc mockMVC;

    @Autowired
    public GardenRepository gardenRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public FriendshipRepository friendshipRepository;

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
    public FriendshipService friendshipService;

    @Autowired
    public UserInteractionService userInteractionService;

    private MvcResult mvcResult;

    @Before
    public void before_or_after_all() {

        userService = new UserService(passwordEncoder, userRepository, homePageLayoutRepository);
        gardenService = new GardenService(gardenRepository, userService);
        friendshipService = new FriendshipService(friendshipRepository, userService);

        ManageFriendsController manageFriendsController = new ManageFriendsController(friendshipService,
                securityService, userService, userInteractionService);
        mockMVC = MockMvcBuilders.standaloneSetup(manageFriendsController).build();

    }

    @When("I cancel my friend request to {string}")
    public void i_cancel_my_friend_request_to(String receiverEmail) throws Exception {
        User user = userService.getUserByEmail(receiverEmail);

        mvcResult = mockMVC.perform(
                MockMvcRequestBuilders
                        .post("/manage-friends/remove")
                        .param("friendId", String.valueOf(user.getId()))
                        .param("activeTab", "pending"))
                .andExpect(status().is3xxRedirection()).andReturn();
    }

    @When("There is user {string} who is logged in with {string}")
    public void there_is_user_who_is_logged_in(String userEmail, String userPassword) throws Exception {
        mockMVC.perform(
                post("/login")
                        .param("emailAddress", userEmail)
                        .param("password", userPassword))
                .andExpect(MockMvcResultMatchers.redirectedUrl("/home"));
    }

    @Then("I cannot see or accept the friend request from {string}")
    public void they_cannot_see_or_accept_my_friend_request(String senderEmail) throws Exception {
        User user = userService.getUserByEmail(senderEmail);

        String userName = user.getFirstName() + ' ' + user.getLastName();

        mvcResult = mockMVC.perform(MockMvcRequestBuilders.get("/manage-friends"))
                .andExpect(status().isOk()).andReturn();
        ModelAndView model = mvcResult.getModelAndView();
        Assertions.assertNotNull(model);
        List<RequestFriendModel> result = (List<RequestFriendModel>) model.getModelMap()
                .getAttribute("pendingFriends");
        Assertions.assertNotNull(result);
        RequestFriendModel requestFriendModel = result.get(result.size() - 1);
        Assertions.assertNotEquals(userName, requestFriendModel.getFriendName());
    }

    @When("I hit the 'Remove Friend' button for user {string}")
    public void i_hit_remove_friend_button_for_user(String email) throws Exception {

        User user = userService.getUserByEmail(email);

        mvcResult = mockMVC.perform(
                MockMvcRequestBuilders
                        .post("/manage-friends/remove")
                        .param("friendId", String.valueOf(user.getId()))
                        .param("activeTab", "pending"))
                .andExpect(status().is3xxRedirection()).andReturn();
    }

    @Then("That friend {string} is removed from my friends list")
    public void that_friend_is_removed_from_my_friends_list(String email) throws Exception {

        User user = userService.getUserByEmail(email);

        String userName = user.getFirstName() + ' ' + user.getLastName();

        mvcResult = mockMVC.perform(MockMvcRequestBuilders.get("/manage-friends"))
                .andExpect(status().isOk()).andReturn();
        ModelAndView model = mvcResult.getModelAndView();
        Assertions.assertNotNull(model);
        List<FriendModel> result = (List<FriendModel>) model.getModelMap().getAttribute("userFriends");
        Assertions.assertNotNull(result);
        FriendModel friendModel = result.get(result.size() - 1);
        Assertions.assertNotEquals(userName, friendModel.getFriendName());
    }
}
