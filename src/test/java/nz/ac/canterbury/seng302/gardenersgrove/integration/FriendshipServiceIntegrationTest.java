package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendshipService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Optional;

@SpringBootTest
public class FriendshipServiceIntegrationTest {

    private static UserService userService;
    private static FriendshipRepository friendshipRepository;
    private static FriendshipService friendshipService;

    private User user1;
    private User user2;
    @BeforeEach
    public void before_or_after_all() {
        userService = Mockito.mock(UserService.class);
        friendshipRepository = Mockito.mock(FriendshipRepository.class);
        friendshipService = new FriendshipService(friendshipRepository, userService);
        user1 =  new User("John", "Doe", "jhonDoe@FriendshipServiceIntegrationTest.com", LocalDate.of(2003,5,2));
        user2 =  new User("Jane", "Doe", "janeDoe@FriendshipServiceIntegrationTest.com", LocalDate.of(2003,5,2));
    }
    @Test
    public void getFriendshipById_FriendshipNotInPersistence_ThrowsIllegalArgumentException(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.getFriendShipById(2L);
        });
    }
    @Test
    public void getFriendshipById_FriendshipInPersistence(){
        Friendship expectedFriendShip = new Friendship(user1,user2, FriendshipStatus.PENDING);
        friendshipRepository.save(expectedFriendShip);

        Optional<Friendship> optionalFriendship = friendshipService.getFriendShipById(1L);

        Assertions.assertTrue(optionalFriendship.isPresent());
        Friendship actualFriendship = optionalFriendship.get();
        Assertions.assertEquals(actualFriendship.getUser1().getId(), expectedFriendShip.getUser1().getId());
        Assertions.assertEquals(actualFriendship.getUser2().getId(), expectedFriendShip.getUser2().getId());
        Assertions.assertEquals(actualFriendship.getStatus(), expectedFriendShip.getStatus());
    }
    @Test
    public void getAllUsersFriends_UserNotInPersistence_ThrowsIllegalArgumentException(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.getFriendShipById(2L);
        });
    }
    @Test
    public void getAllUsersFriends_UserInPersistenceAndNoFriendshipRelation(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.getFriendShipById(2L);
        });
    }
    @Test
    public void getAllUsersFriends_UserInPersistenceAndHasFriendshipRelation(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.getFriendShipById(2L);
        });
    }

    // Todo have ones for single and multiple friends
    @Test
    public void addFriendship_User1NotInPersistence_ThrowsIllegalArgumentException(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.getFriendShipById(2L);
        });
    }
    @Test
    public void addFriendship_User1InPersistenceAndUser2NotInPersistence_ThrowsIllegalArgumentException(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.getFriendShipById(2L);
        });
    }
    @Test
    public void addFriendship_User1AndUser2BothInPersistence(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.getFriendShipById(2L);
        });
    }
    // Todo add user 2 not in persistence but user 1 is in persitence
    @Test
    public void updateFriendShipStatus_FriendshipNotInPersistence_ThrowsIllegalArgumentException(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.getFriendShipById(2L);
        });
    }
    @Test
    public void updateFriendShipStatus_FriendshipInPersistenceAndCurrentStatusIsDeclined_ThrowsIllegalArgumentException(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.getFriendShipById(2L);
        });
    }
    @Test
    public void updateFriendShipStatus_FriendshipInPersistenceAndCurrentStatusIsNotDeclined(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.getFriendShipById(2L);
        });
    }

    // Todo, add test for case where someone has declined an invite but then invites the person they declined
    // Todo user 1 sent user 2 a friendship invite then tries to re add user 2
    // Todo user 1 sent user 2 a friendship invite then user 2 tries to send user 1 a friendship invite
    // Todo user 1 has friendship with user 2, then sends a request to user 3
    // Todo user 1 has friendship with user 2, then user 3 sends user 1 a request
    // Todo user 2 has friendship with user 3, then user 1 sends a request to user 3



}
