package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.ManageFriendsController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.model.FriendModel;
import nz.ac.canterbury.seng302.gardenersgrove.model.RequestFriendModel;
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
import java.util.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class U17_SendFriendRequest {

    Logger logger = LoggerFactory.getLogger(U17_SendFriendRequest.class);

    public static MockMvc mockMVC;

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
    public UserInteractionService userInteractionService;

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
                securityService, userService, userInteractionService);
        // Allows us to bypass spring security
        mockMVC = MockMvcBuilders.standaloneSetup(manageFriendsController).build();

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

    @Given("I {string} {string}, {int} am a user with email {string} and password {string}")
    public void iAmAUserWithEmailAndPassword(String firstName, String LastName, Integer age, String userEmail,
                                             String userPassword) {
        int birthYear = 2024 - age;
        String dob = "01/01/" + birthYear;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
        LocalDate dateOfBirth = LocalDate.parse(dob, formatter);

        User user = new User(firstName, LastName, userEmail, dateOfBirth);
        user.setVerified(true);
        userService.addUser(user, userPassword);
        Assertions.assertNotNull(userService.getUserByEmail(userEmail));
    }


    // AC1
    @When("I click on the 'Manage Friends' button")
    public void i_click_on_button() throws Exception {
        mvcResult = mockMVC.perform(MockMvcRequestBuilders.get("/manage-friends"))
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
        mvcResult = mockMVC.perform(MockMvcRequestBuilders.get("/manage-friends"))
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
        User user = new User(fname, lname, email, null);
        user.setVerified(true);
        userService.addUser(user, "Password!0");
        Assertions.assertNotNull(userService.getUserByEmail(email));
    }


    @Given("I, user {string}, have declined a friend request from {string}")
    public void i_user_have_declined_a_friend_request_from(String myEmail, String declinedFriendEmail) {
        User liam = userService.getUserByEmail(myEmail);
        User declinedFriend = userService.getUserByEmail(declinedFriendEmail);

        Friendship friendship = friendshipService.addFriendship(liam, declinedFriend);
        friendship.setStatus(FriendshipStatus.DECLINED);
        Assertions.assertEquals(FriendshipStatus.DECLINED, friendship.getStatus());
    }


    @When("I enter in {string}")
    public void i_enter_in(String input) {
        this.input = input;
    }

    @When("I hit the search button")
    public void i_hit_the_search_button() throws Exception {
        mvcResult = mockMVC.perform(
                        MockMvcRequestBuilders
                                .get("/manage-friends/search")
                                .param("searchInput", input))
                .andExpect(status().isOk()).andReturn();

    }

    @Then("I can see a list of users of the app exactly matching {string} {string} {string}")
    public void i_can_see_a_list_of_users_of_the_app_exactly_matching(String fname, String lname, String email) {
        List<FriendModel> result = (List<FriendModel>) mvcResult.getModelAndView().getModelMap().getAttribute("searchResults");
        assert result != null;
        for (FriendModel friendModel : result) {
            String matchFullName = friendModel.getFriendName();
            Long matchId = Long.parseLong(friendModel.getFriendGardenLink().split("/")[1]);
            User matchUser = userRepository.findById(matchId).orElseThrow(() -> new NoSuchElementException("User not found"));

            Assertions.assertNotNull(matchUser);
            Assertions.assertTrue((Objects.equals(matchFullName, fname + " " + lname) || Objects.equals(matchUser.getEmailAddress(), email)));
        }
    }

    @Then("I can see the error {string}")
    public void i_can_see_the_error(String error) {
        String searchError = (String) mvcResult.getModelAndView().getModelMap().getAttribute("searchErrorText");
        Assertions.assertEquals(error, searchError);
    }

    @Given("I search for a user {string}")
    public void i_search_for_a_user(String email) throws Exception {

        User user = userService.getUserByEmail(email);

        String friendProfilePicture = user.getProfilePictureFilename();
        String userName = user.getFirstName() + ' ' + user.getLastName();

        mvcResult = mockMVC.perform(
                        MockMvcRequestBuilders
                                .get("/manage-friends/search")
                                .param("searchInput", email))
                .andExpect(status().isOk())
                .andReturn();
        List<FriendModel> result = (List<FriendModel>) mvcResult.getModelAndView().getModelMap().getAttribute("searchResults");
        FriendModel friendModel = result.get(0);
        Assertions.assertEquals(friendProfilePicture, friendModel.getFriendProfilePicture());
        Assertions.assertEquals(userName, friendModel.getFriendName());
    }

    @When("I hit the 'invite as friend' button for user with {string}")
    public void i_hit_the_invite_as_friend_button_for_user_with(String email) throws Exception {
        User user = userService.getUserByEmail(email);
        mvcResult = mockMVC.perform(
                        MockMvcRequestBuilders
                                .post("/manage-friends/send-invite")
                                .param("friendId", String.valueOf(user.getId()))
                                .param("activeTab", "search"))
                .andExpect(status().is3xxRedirection()).andReturn();
    }

    @Then("user {string} sees the invite from {string}")
    public void user_sees_the_invite_from(String receiverEmail, String senderEmail) throws Exception {
        User user = userService.getUserByEmail(senderEmail);

        String friendProfilePicture = user.getProfilePictureFilename();
        String userName = user.getFirstName() + ' ' + user.getLastName();

        mvcResult = mockMVC.perform(MockMvcRequestBuilders.get("/manage-friends"))
                .andExpect(status().isOk()).andReturn();
        List<RequestFriendModel> result = (List<RequestFriendModel>) mvcResult.getModelAndView().getModelMap().getAttribute("pendingFriends");
        RequestFriendModel requestFriendModel = result.get(0);
        Assertions.assertEquals(friendProfilePicture, requestFriendModel.getFriendProfilePicture());
        Assertions.assertEquals(userName, requestFriendModel.getFriendName());
        Assertions.assertFalse(requestFriendModel.isSender());
    }


    @Given("I {string} have a pending invite from {string}")
    public void i_have_a_pending_invite_from(String receiverEmail, String senderEmail) {
        User liam = userService.getUserByEmail(senderEmail);
        User sarah = userService.getUserByEmail(receiverEmail);

        Friendship friendship = friendshipService.addFriendship(liam, sarah);
        Assertions.assertEquals(FriendshipStatus.PENDING, friendship.getStatus());
    }

    @When("I accept the pending invite from {string}")
    public void i_accept_the_pending_invite_from(String email) throws Exception {
        User user = userService.getUserByEmail(email);

        mvcResult = mockMVC.perform(
                        MockMvcRequestBuilders
                                .post("/manage-friends")
                                .param("pendingFriendId", String.valueOf(user.getId()))
                                .param("friendAccepted", "true")
                                .param("activeTab", "pending"))
                .andExpect(status().is3xxRedirection()).andReturn();
    }

    @Then("{string} is added to my friends list")
    public void is_added_to_my_friends_list(String email) throws Exception {
        User user = userService.getUserByEmail(email);

        String friendProfilePicture = user.getProfilePictureFilename();
        String userName = user.getFirstName() + ' ' + user.getLastName();


        mvcResult = mockMVC.perform(MockMvcRequestBuilders.get("/manage-friends"))
                .andExpect(status().isOk()).andReturn();

        List<FriendModel> result = (List<FriendModel>) mvcResult.getModelAndView().getModelMap().getAttribute("userFriends");
        Assertions.assertNotNull(result);
        FriendModel friendModel = result.get(result.size() - 1);
        Assertions.assertEquals(friendProfilePicture, friendModel.getFriendProfilePicture());
        Assertions.assertEquals(userName, friendModel.getFriendName());

    }

    @When("I decline the pending invite from {string}")
    public void i_decline_the_pending_invite_from(String email) throws Exception {
        User user = userService.getUserByEmail(email);

        mvcResult = mockMVC.perform(
                        MockMvcRequestBuilders
                                .post("/manage-friends")
                                .param("pendingFriendId", String.valueOf(user.getId()))
                                .param("friendAccepted", "false")
                                .param("activeTab", "pending"))
                .andExpect(status().is3xxRedirection()).andReturn();
    }

    @Then("{string} are not added to friends list")
    public void are_not_added_to_friends_list(String email) throws Exception {
        User user = userService.getUserByEmail(email);


        mvcResult = mockMVC.perform(MockMvcRequestBuilders.get("/manage-friends"))
                .andExpect(status().isOk()).andReturn();

        List<FriendModel> result = (List<FriendModel>) mvcResult.getModelAndView().getModelMap().getAttribute("userFriends");
        System.out.println(result);
        Assertions.assertEquals(0, result.size());
    }

    @Then("{string} can not add me {string}")
    public void can_not_add_me(String senderEmail, String receiverEmail) {
        User sender = userService.getUserByEmail(senderEmail);
        User receiver = userService.getUserByEmail(receiverEmail);

        Assertions.assertThrows(IllegalArgumentException.class, () -> friendshipService.addFriendship(sender, receiver));
    }

    @Given("I {string} have sent an invite to {string}")
    public void i_have_sent_an_invite_to(String senderEmail, String receiverEmail) {
        User sender = userService.getUserByEmail(senderEmail);
        User receiver = userService.getUserByEmail(receiverEmail);

        Friendship friendship = friendshipService.addFriendship(sender, receiver);
        Assertions.assertEquals(FriendshipStatus.PENDING, friendship.getStatus());
    }

    @When("{string} declines my {string} request")
    public void declines_my_request(String receiverEmail, String senderEmail) {
        User sender = userService.getUserByEmail(senderEmail);
        User receiver = userService.getUserByEmail(receiverEmail);

        Friendship friendship = friendshipService.findFriendship(sender, receiver);
        Assertions.assertNotNull(friendship);
        Friendship updatedFriendship = friendshipService.updateFriendShipStatus(friendship.getId(), FriendshipStatus.DECLINED);
        Assertions.assertEquals(FriendshipStatus.DECLINED, updatedFriendship.getStatus());
    }

    @Then("I see the request as declined by {string}")
    public void i_see_the_request_as_declined_by(String receiver) throws Exception {
        User user = userService.getUserByEmail(receiver);

        String friendProfilePicture = user.getProfilePictureFilename();
        String userName = user.getFirstName() + ' ' + user.getLastName();

        mvcResult = mockMVC.perform(MockMvcRequestBuilders.get("/manage-friends"))
                .andExpect(status().isOk()).andReturn();

        List<RequestFriendModel> result = (List<RequestFriendModel>) mvcResult.getModelAndView().getModelMap().getAttribute("declinedFriends");
        Assertions.assertNotNull(result);
        RequestFriendModel requestFriendModel = result.get(result.size() - 1);
        Assertions.assertEquals(friendProfilePicture, requestFriendModel.getFriendProfilePicture());
        Assertions.assertEquals(userName, requestFriendModel.getFriendName());
    }

    @When("{string} has not accepted or declined my {string} request")
    public void leaves_my_request_pending(String receiverEmail, String senderEmail) {
        User sender = userService.getUserByEmail(senderEmail);
        User receiver = userService.getUserByEmail(receiverEmail);

        Friendship friendship = friendshipService.findFriendship(sender, receiver);
        Assertions.assertNotNull(friendship);
        Assertions.assertEquals(FriendshipStatus.PENDING, friendship.getStatus());
    }

    @Then("I see the request to {string} as pending")
    public void i_see_the_request_as_pending_by(String receiver) throws Exception {
        User user = userService.getUserByEmail(receiver);

        String friendProfilePicture = user.getProfilePictureFilename();
        String userName = user.getFirstName() + ' ' + user.getLastName();

        mvcResult = mockMVC.perform(MockMvcRequestBuilders.get("/manage-friends"))
                .andExpect(status().isOk()).andReturn();

        List<RequestFriendModel> result = (List<RequestFriendModel>) mvcResult.getModelAndView().getModelMap().getAttribute("pendingFriends");
        Assertions.assertNotNull(result);
        RequestFriendModel requestFriendModel = result.get(result.size() - 1);
        Assertions.assertEquals(friendProfilePicture, requestFriendModel.getFriendProfilePicture());
        Assertions.assertEquals(userName, requestFriendModel.getFriendName());
    }


}
