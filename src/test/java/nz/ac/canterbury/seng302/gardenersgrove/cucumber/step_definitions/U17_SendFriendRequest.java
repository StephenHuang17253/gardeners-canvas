package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.ManageFriendsController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.model.FriendModel;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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


    public static GardenService gardenService;

    public static UserService userService;

    @Autowired
    public FriendshipService friendshipService;

    @Autowired
    public FileService fileService;

    private MvcResult mvcResult;

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

    // Setup
    @Given("I {string} am friends with {string} {string}, {int}, a user with email {string} and password {string}")
    public void friends_with(String userEmail, String firstName, String lastName, Integer age, String friendEmail,
                                             String friendPassword) {
        int birthYear = 2024 - age;
        String dob = "01/01/" + birthYear;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
        LocalDate dateOfBirth = LocalDate.parse(dob, formatter);

        User friend = new User(firstName, lastName, friendEmail, dateOfBirth);
        friend.setVerified(true);
        userService.addUser(friend, friendPassword);

        User currentUser = userService.getUserByEmail(userEmail);

        Friendship friendshipStatus = friendshipService.addFriendship(currentUser, friend);

        friendshipService.updateFriendShipStatus(friendshipStatus.getId(), FriendshipStatus.ACCEPTED);

        boolean friends = friendshipService.checkFriendshipExists(currentUser, friend);

        String friendProfilePicture = friend.getProfilePictureFilename();
        String friendsName = firstName + ' ' + lastName;
        String friendGardenLink = "/" + friend.getId() + "/gardens";

        FriendModel friendModel = new FriendModel(friendProfilePicture, friendsName, friendGardenLink);
        friendsList.add(friendModel);

        Assertions.assertTrue(friends);
    }

    // AC1
    @When("I click on the 'Manage Friends' button")
    public void i_click_on_button() throws Exception {
        mvcResult = MOCK_MVC.perform(MockMvcRequestBuilders.get("/manage-friends"))
                .andExpect(status().isOk()).andReturn();
    }

    // AC1
    @Then("I am shown a 'Manage Friends' page")
    public void i_am_shown_a_manage_friends_page() {
        String pageName = mvcResult.getModelAndView().getViewName();
        logger.info(pageName);
        Assertions.assertEquals("manageFriendsPage", pageName);
    }

    // AC2
    @When("I am on the 'Manage Friends' page")
    public void i_am_on_the_manage_friends_page() throws Exception {
        mvcResult = MOCK_MVC.perform(MockMvcRequestBuilders.get("/manage-friends"))
                .andExpect(status().isOk()).andReturn();
    }

    // AC2
    @Then("I see a list of my friends with their names, profile pictures, and link to their gardens list")
    public void i_see_a_list_of_my_friends_with_their_names_profile_pictures_and_link_to_their_gardens_list() {
        List<FriendModel> result = (List<FriendModel>) mvcResult.getModelAndView().getModelMap().getAttribute("userFriends");
        for (int i = 0; i < friendsList.size(); i++) {
            Assertions.assertEquals(friendsList.get(i).getFriendName(), result.get(i).getFriendName());
            Assertions.assertEquals(friendsList.get(i).getFriendProfilePicture(), result.get(i).getFriendProfilePicture());
            Assertions.assertEquals(friendsList.get(i).getFriendGardenLink(), result.get(i).getFriendGardenLink());
        }


    }

    @Given("A user with first name {string}, last name {string}, and email {string} exists")
    public void a_user_with_first_name_last_name_and_email_exists(String fname, String lname, String email) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @When("I enter in {string}, {string}, {string}")
    public void i_enter_in(String fname, String lname, String email) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
    @When("I hit the search button")
    public void i_hit_the_search_button() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
    @Then("I can see a list of users of the app exactly matching {string} {string} {string}")
    public void i_can_see_a_list_of_users_of_the_app_exactly_matching(String fname, String lname, String email) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @When("I have opened the search bar")
    public void i_have_opened_the_search_bar() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Then("I can see the error {string}")
    public void i_can_see_the_error(String error) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }






}
