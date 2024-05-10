package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendshipService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class FriendshipServiceIntegrationTest {

    private static UserService userService;
    private static FriendshipRepository friendshipRepository;
    private static FriendshipService friendshipService;
    @Captor
    private ArgumentCaptor<Friendship> friendshipCaptor;


    private User user1;
    private User user2;
    private User user3;
    private Long MAX_LONG = 1000L;
    @BeforeEach
    public void before_or_after_all() {
        userService = Mockito.mock(UserService.class);
        friendshipRepository = Mockito.mock(FriendshipRepository.class);
        friendshipService = new FriendshipService(friendshipRepository, userService);
        user1 =  new User("John", "Doe", "jhonDoe@FriendshipServiceIntegrationTest.com", LocalDate.of(2003,5,2));
        user2 =  new User("Jane", "Doe", "janeDoe@FriendshipServiceIntegrationTest.com", LocalDate.of(2003,5,2));
        user3 =  new User("Test", "Doe", "testDoe@FriendshipServiceIntegrationTest.com", LocalDate.of(2003,5,2));
    }
    @Test
    public void getFriendshipById_FriendshipNotInPersistence_ThrowsIllegalArgumentException(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.getFriendShipById(MAX_LONG);
        });
    }
    @Test
    public void getFriendshipById_FriendshipInPersistence(){
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
    public void getAllUsersFriends_UserNotInPersistence_ThrowsIllegalArgumentException(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.getAllUsersFriends(MAX_LONG);
        });
    }
    @Test
    public void getAllUsersFriends_UserInPersistenceAndNoFriendshipRelation(){
        List<Friendship> expectedFriendships = friendshipService.getAllUsersFriends(user3.getId());
        Assertions.assertEquals(0,expectedFriendships.size());
    }
    @Test
    public void getAllUsersFriends_UserInPersistenceAndHasSingleFriendshipRelation(){
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
    public void getAllUsersFriends_UserInPersistenceAndHasMultipleFriendshipRelations(){
        List<Friendship> expectedFriendships = List.of(new Friendship(user1,user2, FriendshipStatus.PENDING),
                new Friendship(user1,user3, FriendshipStatus.PENDING));
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
    public void addFriendship_BothUser1AndUser2NotInPersistence_ThrowsIllegalArgumentException(){
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
    public void addFriendship_User1InPersistenceAndAddsThemselves_ThrowsIllegalArgumentException(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.addFriendship(user1, user1);
        });
    }
    @Test
    public void addFriendship_User1NotInPersistenceAndUser2InPersistence_ThrowsIllegalArgumentException(){
        User nonExistentUser1 = new User("nonExistentUser",
                "", "nonExistentUser1@FriendshipServiceIntegrationTest.com",
                LocalDate.of(2003,5,2));
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.addFriendship(nonExistentUser1, user2);
        });
    }
    @Test
    public void addFriendship_User1InPersistenceAndUser2NotInPersistence_ThrowsIllegalArgumentException(){
        User nonExistentUser2 = new User("nonExistentUser",
                "", "nonExistentUser2@FriendshipServiceIntegrationTest.com",
                LocalDate.of(2003,5,2));
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.addFriendship(user1, nonExistentUser2);
        });
    }
    @Test
    public void addFriendship_User1AndUser2BothInPersistence(){
        friendshipService.addFriendship(user1, user2);
        Mockito.verify(friendshipRepository).save(friendshipCaptor.capture());

        Friendship savedFriendship = friendshipCaptor.getValue();
        Assertions.assertEquals(user1.getId(), savedFriendship.getUser1().getId());
        Assertions.assertEquals(user2.getId(), savedFriendship.getUser2().getId());
        Assertions.assertEquals(FriendshipStatus.PENDING, savedFriendship.getStatus());
    }
    @Test
    public void addFriendship_U1U2DeclinedStatusFriendShipExistsAndUser1AddsUser2_ThrowsIllegalArgumentException(){
        friendshipRepository.save(new Friendship(user1,user2,FriendshipStatus.DECLINED));
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.addFriendship(user1, user2);
        });
    }
    @Test
    public void addFriendship_U1U2DeclinedStatusFriendShipExistsAndUser2AddsUser1(){
        Friendship oldFriendship = new Friendship(user1,user2,FriendshipStatus.DECLINED);
        friendshipRepository.save(oldFriendship);
        friendshipService.addFriendship(user2, user1);
        Mockito.verify(friendshipRepository).save(friendshipCaptor.capture());

        Friendship savedFriendship = friendshipCaptor.getValue();
        Assertions.assertEquals(oldFriendship.getId(), savedFriendship.getId());
        Assertions.assertEquals(user2.getId(), savedFriendship.getUser1().getId());
        Assertions.assertEquals(user1.getId(), savedFriendship.getUser2().getId());
        Assertions.assertEquals(FriendshipStatus.PENDING, savedFriendship.getStatus());
    }
    @Test
    public void addFriendship_U1U2PendingStatusFriendShipExistsAndUser1AddsUser2_ThrowsIllegalArgumentException(){
        friendshipRepository.save(new Friendship(user1,user2,FriendshipStatus.PENDING));
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.addFriendship(user1, user2);
        });
    }
    @Test
    public void addFriendship_U1U2PendingStatusFriendShipExistsAndUser2AddsUser1_ThrowsIllegalArgumentException(){
        friendshipRepository.save(new Friendship(user1,user2,FriendshipStatus.PENDING));
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.addFriendship(user2, user1);
        });
    }
    @Test
    public void addFriendship_U1U2AcceptedStatusFriendShipExistsAndUser1AddsUser2_ThrowsIllegalArgumentException(){
        friendshipRepository.save(new Friendship(user1,user2,FriendshipStatus.ACCEPTED));
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.addFriendship(user1, user2);
        });
    }
    @Test
    public void addFriendship_U1U2AcceptedStatusFriendShipExistsAndUser2AddsUser1_ThrowsIllegalArgumentException(){
        friendshipRepository.save(new Friendship(user1,user2,FriendshipStatus.ACCEPTED));
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.addFriendship(user2, user1);
        });
    }
    @Test
    public void addFriendship_U1U2AcceptedStatusFriendShipExistsAndUser1AddsANewUser(){
        friendshipRepository.save(new Friendship(user1,user2,FriendshipStatus.ACCEPTED));
        friendshipService.addFriendship(user1, user3);
        Mockito.verify(friendshipRepository).save(friendshipCaptor.capture());

        Friendship savedFriendship = friendshipCaptor.getValue();
        Assertions.assertEquals(user1.getId(), savedFriendship.getUser1().getId());
        Assertions.assertEquals(user3.getId(), savedFriendship.getUser2().getId());
        Assertions.assertEquals(FriendshipStatus.PENDING, savedFriendship.getStatus());
    }
    @Test
    public void addFriendship_U1U2AcceptedStatusFriendShipExistsAndUser2AddsANewUser(){
        friendshipRepository.save(new Friendship(user1,user2,FriendshipStatus.ACCEPTED));
        friendshipService.addFriendship(user2, user3);
        Mockito.verify(friendshipRepository).save(friendshipCaptor.capture());

        Friendship savedFriendship = friendshipCaptor.getValue();
        Assertions.assertEquals(user2.getId(), savedFriendship.getUser1().getId());
        Assertions.assertEquals(user3.getId(), savedFriendship.getUser2().getId());
        Assertions.assertEquals(FriendshipStatus.PENDING, savedFriendship.getStatus());
    }
    @Test
    public void updateFriendShipStatus_FriendshipNotInPersistence_ThrowsIllegalArgumentException(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.updateFriendShipStatus(MAX_LONG,FriendshipStatus.ACCEPTED);
        });
    }
    @Test
    public void updateFriendShipStatus_FriendshipInPersistenceAndCurrentStatusIsDeclined_ThrowsIllegalArgumentException(){
        Mockito.verify(friendshipRepository).save(friendshipCaptor.capture());
        friendshipRepository.save(new Friendship(user1,user2,FriendshipStatus.DECLINED));
        Friendship savedFriendship = friendshipCaptor.getValue();
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.updateFriendShipStatus(savedFriendship.getId(),FriendshipStatus.ACCEPTED);
        });
    }
    @Test
    public void updateFriendShipStatus_FriendshipInPersistenceAndCurrentStatusIsNotDeclined(){
        Mockito.verify(friendshipRepository).save(friendshipCaptor.capture());
        friendshipRepository.save(new Friendship(user1,user2,FriendshipStatus.PENDING));
        Friendship originalFriendship = friendshipCaptor.getValue();
        friendshipService.updateFriendShipStatus(originalFriendship.getId(),FriendshipStatus.ACCEPTED);
        Friendship updatedFriendship = friendshipCaptor.getValue();

        Assertions.assertEquals(originalFriendship.getId(), updatedFriendship.getId());
        Assertions.assertEquals(user1.getId(), updatedFriendship.getUser1().getId());
        Assertions.assertEquals(user2.getId(), updatedFriendship.getUser2().getId());
        Assertions.assertEquals(FriendshipStatus.ACCEPTED, updatedFriendship.getStatus());
    }

}
