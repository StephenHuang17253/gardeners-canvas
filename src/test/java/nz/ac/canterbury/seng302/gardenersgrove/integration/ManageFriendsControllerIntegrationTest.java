package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.model.FriendModel;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendshipService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ManageFriendsControllerIntegrationTest {

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    UserService userService;

    private final MockMvc mockMvc;
    User user1;
    User user2;
    User user3;
    User user4;
    User user5;

    List<FriendModel> friendsList;

    @Autowired
    ManageFriendsControllerIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeAll
    void before_or_after_all() {

        user1 = new User("John", "Doe", "jhonDoe@ManageFriendsControllerIntegrationTest.com", LocalDate.of(2003, 5, 2));
        user2 = new User("Jane", "Doe", "janeDoe@ManageFriendsControllerIntegrationTest.com", LocalDate.of(2003, 5, 2));
        user3 = new User("Bruce", "Wayne", "bruceWayne@ManageFriendsControllerIntegrationTest.com",
                LocalDate.of(2003, 5, 2));
        user4 = new User("Test", "Doe", "testDoe@ManageFriendsControllerIntegrationTest.com", LocalDate.of(2003, 5, 2));
        user5 = new User("Walter", "Doe", "pending@ManageFriendsControllerIntegrationTest.com",
                LocalDate.of(2003, 5, 2));

        userService.addUser(user1, "1es1P@ssword");
        userService.addUser(user2, "1es1P@ssword");
        userService.addUser(user3, "1es1P@ssword");
        userService.addUser(user4, "1es1P@ssword");
        userService.addUser(user5, "1es1P@ssword");

        Friendship friendship1 = friendshipService.addFriendship(user1, user2);
        Friendship friendship2 = friendshipService.addFriendship(user3, user1);
        Friendship friendship3 = friendshipService.addFriendship(user5, user1);
        friendshipService.updateFriendShipStatus(friendship1.getId(), FriendshipStatus.ACCEPTED);
        friendshipService.updateFriendShipStatus(friendship2.getId(), FriendshipStatus.ACCEPTED);
        friendshipService.updateFriendShipStatus(friendship3.getId(), FriendshipStatus.PENDING);

    }

    @BeforeEach
    void clear_repo() {
        friendsList = new ArrayList<>();
    }

    @Test
    void GetMyFriends_UserNotAuthorized_Return403() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get("/manage-friends"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "jhonDoe@ManageFriendsControllerIntegrationTest.com")
    void GetMyFriends_UserAuthorizedAndHasMultipleFriends_Return200() throws Exception {
        List<User> userList = List.of(user2, user3);
        for (int i = 0; i < userList.size(); i++) {
            User friend = userList.get(i);
            String friendProfilePicture = friend.getProfilePictureFilename();
            String friendsName = friend.getFirstName() + ' ' + friend.getLastName();
            String friendGardenLink = "/" + friend.getId() + "/gardens";
            friendsList.add(new FriendModel(friendProfilePicture, friendsName, friendGardenLink));
        }
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/manage-friends"))
                .andExpect(status().isOk()).andReturn();
        ModelAndView model = mvcResult.getModelAndView();
        Assertions.assertNotNull(model);
        List<FriendModel> result = (List<FriendModel>) model.getModelMap()
                .getAttribute("userFriends");
        for (int i = 0; i < friendsList.size(); i++) {
            Assertions.assertEquals(friendsList.get(i).getFriendName(), result.get(i).getFriendName());
            Assertions.assertEquals(friendsList.get(i).getFriendProfilePicture(),
                    result.get(i).getFriendProfilePicture());
            Assertions.assertEquals(friendsList.get(i).getFriendGardenLink(), result.get(i).getFriendGardenLink());
        }

    }

    @Test
    @WithMockUser(username = "bruceWayne@ManageFriendsControllerIntegrationTest.com")
    void GetMyFriends_UserAuthorizedAndHasASingleFriend_Return200() throws Exception {
        List<User> userList = List.of(user1);
        for (int i = 0; i < userList.size(); i++) {
            User friend = userList.get(i);
            String friendProfilePicture = friend.getProfilePictureFilename();
            String friendsName = friend.getFirstName() + ' ' + friend.getLastName();
            String friendGardenLink = "/" + friend.getId() + "/gardens";
            friendsList.add(new FriendModel(friendProfilePicture, friendsName, friendGardenLink));
        }
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/manage-friends"))
                .andExpect(status().isOk()).andReturn();
        ModelAndView model = mvcResult.getModelAndView();
        Assertions.assertNotNull(model);
        List<FriendModel> result = (List<FriendModel>) model.getModelMap()
                .getAttribute("userFriends");
        for (int i = 0; i < friendsList.size(); i++) {
            Assertions.assertEquals(friendsList.get(i).getFriendName(), result.get(i).getFriendName());
            Assertions.assertEquals(friendsList.get(i).getFriendProfilePicture(),
                    result.get(i).getFriendProfilePicture());
            Assertions.assertEquals(friendsList.get(i).getFriendGardenLink(), result.get(i).getFriendGardenLink());
        }

    }

    @Test
    @WithMockUser(username = "testDoe@ManageFriendsControllerIntegrationTest.com")
    void GetMyFriends_UserAuthorizedAndHasNoFriends_Return200() throws Exception {

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/manage-friends"))
                .andExpect(status().isOk()).andReturn();
        ModelAndView model = mvcResult.getModelAndView();
        Assertions.assertNotNull(model);
        List<FriendModel> result = (List<FriendModel>) model.getModelMap()
                .getAttribute("userFriends");
        Assertions.assertEquals(0, result.size());

    }

    @Test
    @WithMockUser(username = "pending@ManageFriendsControllerIntegrationTest.com")
    void cancelFriendRequest_cancelFriendRequestValidUser_DeletesFriendship() throws Exception {

        Assertions.assertTrue(friendshipService.checkFriendshipExists(user5, user1));

        mockMvc.perform(MockMvcRequestBuilders.post("/manage-friends/remove")
                .param("friendId", user1.getId().toString()).with(csrf())
                .param("activeTab", "friends"))
                .andExpect(status().is3xxRedirection());

        Assertions.assertFalse(friendshipService.checkFriendshipExists(user5, user1));
    }

    @Test
    @WithMockUser(username = "pending@ManageFriendsControllerIntegrationTest.com")
    void cancelFriendRequest_cancelFriendRequestNoFriendship_NoAction() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/manage-friends/remove")
                .param("friendId", user3.getId().toString()).with(csrf())
                .param("activeTab", "friends"))
                .andExpect(status().is3xxRedirection());

    }

}
