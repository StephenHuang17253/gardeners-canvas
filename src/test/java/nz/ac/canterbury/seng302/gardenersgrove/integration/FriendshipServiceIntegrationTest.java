package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendshipService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FriendshipServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FriendshipRepository friendshipRepository;

    private FriendshipService friendshipService;

    private UserService userService;

    private static User user1;

    private static User user2;

    private static User user3;

    private final Long MAX_LONG = 1000L;

    @BeforeAll
    void before_or_after_all() {
        userService = new UserService(passwordEncoder, userRepository);
        friendshipService = new FriendshipService(friendshipRepository, userService);

        user1 =  new User("John", "Doe", "jhonDoe@FriendshipServiceIntegrationTest.com", LocalDate.of(2003,5,2));
        user2 =  new User("Jane", "Doe", "janeDoe@FriendshipServiceIntegrationTest.com", LocalDate.of(2003,5,2));
        user3 =  new User("Test", "Doe", "testDoe@FriendshipServiceIntegrationTest.com", LocalDate.of(2003,5,2));

        userService.addUser(user1,"1es1P@ssword");
        userService.addUser(user2,"1es1P@ssword");
        userService.addUser(user3,"1es1P@ssword");
    }

    @BeforeEach
    void clear_repo() {
        friendshipRepository.deleteAll();
    }

    @Test
    void getFriendshipById_FriendshipNotInPersistence_returnsOptionalFriendship(){
        Optional<Friendship> optionalFriendship = friendshipService.getFriendShipById(MAX_LONG);

        Assertions.assertTrue(optionalFriendship.isEmpty());
    }

    @Test
    void getFriendshipById_FriendshipInPersistence_returnsOptionalFriendship(){
        Friendship expectedFriendShip = new Friendship(user1,user2, FriendshipStatus.PENDING);
        friendshipRepository.save(expectedFriendShip);

        Optional<Friendship> optionalFriendship = friendshipService.getFriendShipById(expectedFriendShip.getId());

        Assertions.assertTrue(optionalFriendship.isPresent());
        Friendship actualFriendship = optionalFriendship.get();

        Assertions.assertEquals(expectedFriendShip.getUser1().getId(), actualFriendship.getUser1().getId());
        Assertions.assertEquals(expectedFriendShip.getUser2().getId(), actualFriendship.getUser2().getId());
        Assertions.assertEquals(expectedFriendShip.getStatus(), actualFriendship.getStatus());
    }

    @Test
    void checkFriendshipExists_U1AndU2BothNotInPersistence_ThrowsIllegalArgumentException(){
        User nonExistentUser1 = new User("nonExistentUser",
                "", "nonExistentUser1@FriendshipServiceIntegrationTest.com",
                LocalDate.of(2003,5,2));
        User nonExistentUser2 = new User("nonExistentUser",
                "", "nonExistentUser2@FriendshipServiceIntegrationTest.com",
                LocalDate.of(2003,5,2));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.checkFriendshipExists(nonExistentUser1, nonExistentUser2);
        });
    }

    @Test
    void checkFriendshipExists_U1NotInPersistenceAndU2InPersistence_ThrowsIllegalArgumentException(){
        User nonExistentUser1 = new User("nonExistentUser",
                "", "nonExistentUser1@FriendshipServiceIntegrationTest.com",
                LocalDate.of(2003,5,2));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.checkFriendshipExists(nonExistentUser1, user2);
        });
    }

    @Test
    void checkFriendshipExists_U1InPersistenceAndU2NotInPersistence_ThrowsIllegalArgumentException(){
        User nonExistentUser2 = new User("nonExistentUser",
                "", "nonExistentUser2@FriendshipServiceIntegrationTest.com",
                LocalDate.of(2003,5,2));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.checkFriendshipExists(user1, nonExistentUser2);
        });
    }

    @Test
    void checkFriendshipExists_U1AndU2BothInPersistenceButNoRelation_returnFalse(){
        boolean friendshipExists = friendshipService.checkFriendshipExists(user1, user2);

        Assertions.assertFalse(friendshipExists);

    }

    @Test
    void checkFriendshipExists_U1AndU2AndU3AllInPersistenceAndHaveRelations_returnTrue(){
        friendshipRepository.save(new Friendship(user1,user2, FriendshipStatus.PENDING));
        friendshipRepository.save(new Friendship(user1,user3, FriendshipStatus.PENDING));

        boolean friendshipExists_withSameOrdering = friendshipService.checkFriendshipExists(user1, user2);
        boolean friendshipExists_withReverseOrdering = friendshipService.checkFriendshipExists(user3, user1);

        Assertions.assertTrue(friendshipExists_withSameOrdering);
        Assertions.assertTrue(friendshipExists_withReverseOrdering);
    }

    @Test
    void getAllUsersFriends_UserNotInPersistence_ThrowsIllegalArgumentException(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.getAllUsersFriends(MAX_LONG);
        });
    }

    @Test
    void getAllUsersFriends_UserInPersistenceAndNoFriendshipRelation_returnsListFriendship(){
        List<Friendship> expectedFriendships = friendshipService.getAllUsersFriends(user3.getId());

        Assertions.assertEquals(0,expectedFriendships.size());
    }

    @Test
    void getAllUsersFriends_UserInPersistenceAndHasSingleFriendshipRelation_returnsListFriendship(){
        List<Friendship> expectedFriendships = List.of(new Friendship(user1,user2, FriendshipStatus.PENDING));
        friendshipRepository.saveAll(expectedFriendships);

        List<Friendship> actualFriendShips = friendshipService.getAllUsersFriends(user1.getId());
        Assertions.assertEquals(1,actualFriendShips.size());

        for(int i = 0; i < expectedFriendships.size(); i++){
            Friendship expectedFriendship = expectedFriendships.get(i);
            Friendship actualFriendShip = actualFriendShips.get(i);
            Assertions.assertEquals(expectedFriendship.getUser1().getId(), actualFriendShip.getUser1().getId());
            Assertions.assertEquals(expectedFriendship.getUser2().getId(), actualFriendShip.getUser2().getId());
            Assertions.assertEquals(expectedFriendship.getStatus(), actualFriendShip.getStatus());
        }
    }

    @Test
    void getAllUsersFriends_UserInPersistenceAndHasMultipleFriendshipRelations_returnsListFriendship(){
        List<Friendship> expectedFriendships = List.of(new Friendship(user1,user2, FriendshipStatus.PENDING),
                new Friendship(user1,user3, FriendshipStatus.PENDING));
        friendshipRepository.saveAll(expectedFriendships);

        List<Friendship> actualFriendShips = friendshipService.getAllUsersFriends(user1.getId());
        Assertions.assertEquals(2,actualFriendShips.size());

        for(int i = 0; i < expectedFriendships.size(); i++){
            Friendship expectedFriendship = expectedFriendships.get(i);
            Friendship actualFriendShip = actualFriendShips.get(i);
            Assertions.assertEquals(expectedFriendship.getUser1().getId(), actualFriendShip.getUser1().getId());
            Assertions.assertEquals(expectedFriendship.getUser2().getId(), actualFriendShip.getUser2().getId());
            Assertions.assertEquals(expectedFriendship.getStatus(), actualFriendShip.getStatus());
        }
    }

    @Test
    void addFriendship_BothUser1AndUser2NotInPersistence_ThrowsIllegalArgumentException(){
        User nonExistentUser1 = new User("nonExistentUser",
                "", "nonExistentUser1@FriendshipServiceIntegrationTest.com",
                LocalDate.of(2003,5,2));
        User nonExistentUser2 = new User("nonExistentUser",
                "", "nonExistentUser2@FriendshipServiceIntegrationTest.com",
                LocalDate.of(2003,5,2));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.addFriendship(nonExistentUser1, nonExistentUser2);
        });
    }

    @Test
    void addFriendship_User1InPersistenceAndAddsThemselves_ThrowsIllegalArgumentException(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.addFriendship(user1, user1);
        });
    }

    @Test
    void addFriendship_User1NotInPersistenceAndUser2InPersistence_ThrowsIllegalArgumentException(){
        User nonExistentUser1 = new User("nonExistentUser",
                "", "nonExistentUser1@FriendshipServiceIntegrationTest.com",
                LocalDate.of(2003,5,2));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.addFriendship(nonExistentUser1, user2);
        });
    }

    @Test
    void addFriendship_User1InPersistenceAndUser2NotInPersistence_ThrowsIllegalArgumentException(){
        User nonExistentUser2 = new User("nonExistentUser",
                "", "nonExistentUser2@FriendshipServiceIntegrationTest.com",
                LocalDate.of(2003,5,2));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.addFriendship(user1, nonExistentUser2);
        });
    }

    @Test
    void addFriendship_User1AndUser2BothInPersistence_returnsFriendship(){
        Friendship savedFriendship = friendshipService.addFriendship(user1, user2);

        Assertions.assertEquals(user1.getId(), savedFriendship.getUser1().getId());
        Assertions.assertEquals(user2.getId(), savedFriendship.getUser2().getId());
        Assertions.assertEquals(FriendshipStatus.PENDING, savedFriendship.getStatus());
    }

    @Test
    void addFriendship_U1U2DeclinedStatusFriendShipExistsAndUser1AddsUser2_ThrowsIllegalArgumentException(){
        friendshipRepository.save(new Friendship(user1,user2,FriendshipStatus.DECLINED));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.addFriendship(user1, user2);
        });
    }

    @Test
    void addFriendship_U1U2DeclinedStatusFriendShipExistsAndUser2AddsUser1_returnsFriendship(){
        Friendship oldFriendship = new Friendship(user1,user2,FriendshipStatus.DECLINED);
        friendshipRepository.save(oldFriendship);

        Friendship savedFriendship = friendshipService.addFriendship(user2, user1);

        Assertions.assertEquals(oldFriendship.getId(), savedFriendship.getId());
        Assertions.assertEquals(user2.getId(), savedFriendship.getUser1().getId());
        Assertions.assertEquals(user1.getId(), savedFriendship.getUser2().getId());
        Assertions.assertEquals(FriendshipStatus.PENDING, savedFriendship.getStatus());
    }

    @Test
    void addFriendship_U1U2PendingStatusFriendShipExistsAndUser1AddsUser2_ThrowsIllegalArgumentException(){
        friendshipRepository.save(new Friendship(user1,user2,FriendshipStatus.PENDING));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.addFriendship(user1, user2);
        });
    }

    @Test
    void addFriendship_U1U2PendingStatusFriendShipExistsAndUser2AddsUser1_ThrowsIllegalArgumentException(){
        friendshipRepository.save(new Friendship(user1,user2,FriendshipStatus.PENDING));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.addFriendship(user2, user1);
        });
    }

    @Test
    void addFriendship_U1U2AcceptedStatusFriendShipExistsAndUser1AddsUser2_ThrowsIllegalArgumentException(){
        friendshipRepository.save(new Friendship(user1,user2,FriendshipStatus.ACCEPTED));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.addFriendship(user1, user2);
        });
    }

    @Test
    void addFriendship_U1U2AcceptedStatusFriendShipExistsAndUser2AddsUser1_ThrowsIllegalArgumentException(){
        friendshipRepository.save(new Friendship(user1,user2,FriendshipStatus.ACCEPTED));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.addFriendship(user2, user1);
        });
    }

    @Test
    void addFriendship_U1U2AcceptedStatusFriendShipExistsAndUser1AddsANewUser_returnsFriendship(){
        friendshipRepository.save(new Friendship(user1,user2,FriendshipStatus.ACCEPTED));

        Friendship savedFriendship = friendshipService.addFriendship(user1, user3);

        Assertions.assertEquals(user1.getId(), savedFriendship.getUser1().getId());
        Assertions.assertEquals(user3.getId(), savedFriendship.getUser2().getId());
        Assertions.assertEquals(FriendshipStatus.PENDING, savedFriendship.getStatus());
    }

    @Test
    void addFriendship_U1U2AcceptedStatusFriendShipExistsAndUser2AddsANewUser_returnsFriendship(){
        friendshipRepository.save(new Friendship(user1,user2,FriendshipStatus.ACCEPTED));

        Friendship savedFriendship = friendshipService.addFriendship(user2, user3);

        Assertions.assertEquals(user2.getId(), savedFriendship.getUser1().getId());
        Assertions.assertEquals(user3.getId(), savedFriendship.getUser2().getId());
        Assertions.assertEquals(FriendshipStatus.PENDING, savedFriendship.getStatus());
    }

    @Test
    void updateFriendShipStatus_FriendshipNotInPersistence_ThrowsIllegalArgumentException(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.updateFriendShipStatus(MAX_LONG,FriendshipStatus.ACCEPTED);
        });
    }

    @Test
    void updateFriendShipStatus_FriendshipInPersistenceAndCurrentStatusIsDeclined_ThrowsIllegalArgumentException(){
        Friendship savedFriendship = new Friendship(user1,user2,FriendshipStatus.DECLINED);
        friendshipRepository.save(savedFriendship);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.updateFriendShipStatus(savedFriendship.getId(),FriendshipStatus.ACCEPTED);
        });
    }

    @Test
    void updateFriendShipStatus_FriendshipInPersistenceAndCurrentStatusIsNotDeclined_returnsFriendship(){
        Friendship originalFriendship = new Friendship(user1,user2,FriendshipStatus.PENDING);
        friendshipRepository.save(originalFriendship);

        Friendship updatedFriendship = friendshipService.updateFriendShipStatus(originalFriendship.getId(),FriendshipStatus.ACCEPTED);

        Assertions.assertEquals(originalFriendship.getId(), updatedFriendship.getId());
        Assertions.assertEquals(user1.getId(), updatedFriendship.getUser1().getId());
        Assertions.assertEquals(user2.getId(), updatedFriendship.getUser2().getId());
        Assertions.assertEquals(FriendshipStatus.ACCEPTED, updatedFriendship.getStatus());
    }

}
