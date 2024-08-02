package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.model.FriendModel;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.ItemType;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import io.cucumber.java.Before;
import nz.ac.canterbury.seng302.gardenersgrove.controller.HomePageController;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.HomePageLayoutRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class U25_MainPage {

    @Autowired
    public GardenRepository gardenRepository;

    @Autowired
    public PlantRepository plantRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public FriendshipRepository friendshipRepository;

    @Autowired
    public HomePageLayoutRepository homePageLayoutRepository;

    @Autowired
    public SecurityService securityService;

    @Autowired
    public FileService fileService;

    public static GardenService gardenService;

    public static UserService userService;

    public static PlantService plantService;

    private static FriendshipService friendshipService;

    @Autowired
    public UserInteractionService userInteractionService;

    public MockMvc mockMVC;

    private MvcResult mvcResult;

    // Setup
    @Before
    public void before_or_after_all() {

        userService = new UserService(passwordEncoder, userRepository, homePageLayoutRepository);
        gardenService = new GardenService(gardenRepository, userService);
        plantService = new PlantService(plantRepository, gardenService, fileService);
        friendshipService = new FriendshipService(friendshipRepository, userService);

        HomePageController homePageController = new HomePageController(userService, gardenService, plantService,
                friendshipService, securityService, userInteractionService);

        // Allows us to bypass spring security
        mockMVC = MockMvcBuilders
                .standaloneSetup(homePageController)
                .build();
    }

    @Given("I am a user with email {string} and no recently added friends")
    public void i_am_a_user_with_email_and_no_recently_added_friends(String email) {
        User newUser = userService.getUserByEmail(email);
        Long userId = newUser.getId();
        assertNotNull(newUser);
        List<Friendship> allFriendships = friendshipService.getAllUsersFriends(newUser.getId());
        for (Friendship friendship : allFriendships) {
            Long friendId = friendship.getUser1().getId();
            if (userId.equals(friendId)) {
                friendId = friendship.getUser2().getId();
            }
            userInteractionService.removeUserInteraction(userId, friendId, ItemType.USER);
            userInteractionService.removeUserInteraction(friendId, userId, ItemType.USER);
            friendshipService.deleteFriendship(friendship.getId());
        }

        List<Friendship> emptyFriendships = friendshipService.getAllUsersFriends(newUser.getId());
        assertTrue(emptyFriendships.isEmpty());
    }

    @When("I look at the recent friends list on the home page")
    public void i_look_at_the_recent_friends_list_on_the_home_page() throws Exception {
        mvcResult = mockMVC.perform(get("/home")).andExpect(status().isOk()).andReturn();
    }

    @Then("I see that my friends with emails {string} and {string} are listed in order")
    public void i_see_that_my_friends_with_emails_and_are_listed_in_order(String email1, String email2) {
        ModelAndView model = mvcResult.getModelAndView();
        Assertions.assertNotNull(model);
        List<FriendModel> friendList = (List<FriendModel>) model.getModelMap().getAttribute("recentFriends");
        User user1 = userService.getUserByEmail(email1);
        User user2 = userService.getUserByEmail(email2);
        assertNotNull(friendList);
        assertEquals(2, friendList.size());
        // Most to least recent
        assertEquals(friendList.get(0).getFriendId(), user2.getId());
        assertEquals(friendList.get(1).getFriendId(), user1.getId());
    }

    @Then("There are no recently accessed friends")
    public void there_are_no_recently_accessed_friends() {
        ModelAndView model = mvcResult.getModelAndView();
        Assertions.assertNotNull(model);
        List<FriendModel> friendList = (List<FriendModel>) model.getModelMap().getAttribute("recentFriends");
        assertNotNull(friendList);
        assertTrue(friendList.isEmpty());
    }

}
