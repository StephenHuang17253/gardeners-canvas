package nz.ac.canterbury.seng302.gardenersgrove.unit;

import io.cucumber.core.backend.Pending;
import jakarta.persistence.EntityNotFoundException;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendshipService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

public class FriendshipServiceUnitTest {

    @Mock
    FriendshipRepository friendshipRepository;

    @Mock
    UserService userService;

    User testUser1;

    User testUser2;

    Optional<Friendship> emptyOptional = Optional.empty();

    Optional<Friendship> mockOptionalFriendship;

    static FriendshipService friendshipService;

    @BeforeEach
    void before()
    {
        friendshipRepository = Mockito.mock(FriendshipRepository.class);
        userService = Mockito.mock(UserService.class);
        friendshipService = new FriendshipService(friendshipRepository,userService);

        testUser1 = Mockito.spy(new User("John", "Doe", "jhonDoe@ManageFriendsControllerIntegrationTest.com", LocalDate.of(2003,5,2)));
        testUser2 = Mockito.spy(new User("Walter", "Doe", "pending@ManageFriendsControllerIntegrationTest.com", LocalDate.of(2003,5,2)));
        Mockito.when(testUser1.getId()).thenReturn(1L);
        Mockito.when(testUser2.getId()).thenReturn(2L);

        Mockito.when(userService.getUserById(1L)).thenReturn(testUser1);
        Mockito.when(userService.getUserById(2L)).thenReturn(testUser2);

        mockOptionalFriendship = Mockito.mock(Optional.class);




    }

    @Test
    void checkFriendshipExists_friendship1to2Exists_returnTrue()
    {
        Mockito.when(mockOptionalFriendship.isEmpty()).thenReturn(false);
        Mockito.when(friendshipRepository.findByUser1IdAndUser2Id(testUser1.getId(), testUser2.getId())).thenReturn(mockOptionalFriendship);
        Mockito.when(friendshipRepository.findByUser1IdAndUser2Id(testUser2.getId(), testUser1.getId())).thenReturn(Optional.empty());
        Assertions.assertTrue(friendshipService.checkFriendshipExists(testUser1, testUser2));
    }

    @Test
    void checkFriendshipExists_friendship2to1Exists_returnTrue()
    {
        Mockito.when(mockOptionalFriendship.isEmpty()).thenReturn(false);
        Mockito.when(friendshipRepository.findByUser1IdAndUser2Id(testUser1.getId(), testUser2.getId())).thenReturn(Optional.empty());
        Mockito.when(friendshipRepository.findByUser1IdAndUser2Id(testUser2.getId(), testUser1.getId())).thenReturn(mockOptionalFriendship);
        Assertions.assertTrue(friendshipService.checkFriendshipExists(testUser1, testUser2));
    }

    @Test
    void checkFriendshipExists_friendshipDoesNotExist_returnFalse()
    {
        Mockito.when(mockOptionalFriendship.isEmpty()).thenReturn(false);
        Mockito.when(friendshipRepository.findByUser1IdAndUser2Id(testUser1.getId(), testUser2.getId())).thenReturn(Optional.empty());
        Mockito.when(friendshipRepository.findByUser1IdAndUser2Id(testUser2.getId(), testUser1.getId())).thenReturn(Optional.empty());
        Assertions.assertFalse(friendshipService.checkFriendshipExists(testUser1, testUser2));
    }

    @Test
    void addFriendship_noCurrentFriendship_addFriendshipBetween1and2_newFriendshipCreated()
    {
        friendshipService.addFriendship(testUser1, testUser2);

        Mockito.when(friendshipRepository.findByUser1IdAndUser2Id(testUser1.getId(), testUser2.getId())).thenReturn(Optional.empty());
        Mockito.when(friendshipRepository.findByUser1IdAndUser2Id(testUser2.getId(), testUser1.getId())).thenReturn(Optional.empty());
        Mockito.verify(friendshipRepository, Mockito.atLeastOnce()).save(Mockito.any());
    }

    @Test
    void addFriendship_currentFriendshipExistsfrom1to2_addFriendshipBetween1and2_noNewFriendship()
    {

        Mockito.when(friendshipRepository.findByUser1IdAndUser2Id(testUser1.getId(), testUser2.getId())).thenReturn(mockOptionalFriendship);
        Mockito.when(friendshipRepository.findByUser1IdAndUser2Id(testUser2.getId(), testUser1.getId())).thenReturn(Optional.empty());

        Friendship testFriendship = new Friendship(testUser1, testUser2, FriendshipStatus.ACCEPTED);
        Mockito.when(mockOptionalFriendship.isPresent()).thenReturn(true);
        Mockito.when(mockOptionalFriendship.get()).thenReturn(testFriendship);

        Assertions.assertThrows(IllegalArgumentException.class,() -> {friendshipService.addFriendship(testUser1, testUser2);});

        Mockito.verify(friendshipRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void addFriendship_currentFriendshipExistsfrom2to1_addFriendshipBetween1and2_noNewFriendship()
    {

        Mockito.when(friendshipRepository.findByUser1IdAndUser2Id(testUser1.getId(), testUser2.getId())).thenReturn(Optional.empty());
        Mockito.when(friendshipRepository.findByUser1IdAndUser2Id(testUser2.getId(), testUser1.getId())).thenReturn(mockOptionalFriendship);

        Friendship testFriendship = new Friendship(testUser2, testUser1, FriendshipStatus.ACCEPTED);
        Mockito.when(mockOptionalFriendship.isPresent()).thenReturn(true);
        Mockito.when(mockOptionalFriendship.get()).thenReturn(testFriendship);

        Assertions.assertThrows(IllegalArgumentException.class,() -> {friendshipService.addFriendship(testUser1, testUser2);});

        Mockito.verify(friendshipRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void addFriendship_currentFriendshipExistsfrom2to1ButIsDeclined_addFriendshipBetween1and2_NewFriendshipAdded()
    {

        Mockito.when(friendshipRepository.findByUser1IdAndUser2Id(testUser1.getId(), testUser2.getId())).thenReturn(Optional.empty());
        Mockito.when(friendshipRepository.findByUser1IdAndUser2Id(testUser2.getId(), testUser1.getId())).thenReturn(mockOptionalFriendship);

        Friendship testFriendship = new Friendship(testUser2, testUser1, FriendshipStatus.DECLINED);
        Mockito.when(mockOptionalFriendship.isPresent()).thenReturn(true);
        Mockito.when(mockOptionalFriendship.get()).thenReturn(testFriendship);

        friendshipService.addFriendship(testUser1, testUser2);
        Mockito.verify(friendshipRepository, Mockito.atLeastOnce()).save(Mockito.any());
    }

    @ParameterizedTest
    @EnumSource (
            value = FriendshipStatus.class,
            names = {"PENDING","ACCEPTED"}
    )
    void updateFriendshipStatus_toValidValue_StatusChanged(FriendshipStatus newStatus)
    {
        Mockito.when(friendshipRepository.findById(Mockito.any())).thenReturn(mockOptionalFriendship);
        Friendship testFriendship = new Friendship(testUser2, testUser1, newStatus);
        Mockito.when(mockOptionalFriendship.isEmpty()).thenReturn(false);
        Mockito.when(mockOptionalFriendship.get()).thenReturn(testFriendship);

        friendshipService.updateFriendShipStatus(1L, newStatus);
    }

    @Test
    void updateFriendshipStatus_toValidValue_StatusChanged()
    {
        Mockito.when(friendshipRepository.findById(Mockito.any())).thenReturn(mockOptionalFriendship);
        Friendship testFriendship = new Friendship(testUser2, testUser1, FriendshipStatus.DECLINED);
        Mockito.when(mockOptionalFriendship.isEmpty()).thenReturn(false);
        Mockito.when(mockOptionalFriendship.get()).thenReturn(testFriendship);

        Assertions.assertThrows(IllegalArgumentException.class,() -> { friendshipService.updateFriendShipStatus(1L, FriendshipStatus.ACCEPTED);});
    }

    @Test
    void deleteFriendship_friendshipExists_friendshipDeleted()
    {
        Mockito.when(friendshipRepository.findById(Mockito.any())).thenReturn(mockOptionalFriendship);
        Friendship testFriendship = new Friendship(testUser2, testUser1, FriendshipStatus.DECLINED);
        Mockito.when(mockOptionalFriendship.isEmpty()).thenReturn(false);
        Mockito.when(mockOptionalFriendship.get()).thenReturn(testFriendship);


        friendshipService.deleteFriendship(1L);
        Mockito.verify(friendshipRepository, Mockito.atLeastOnce()).deleteById(Mockito.anyLong());
    }

    @Test
    void deleteFriendship_friendshipDoesNotExists_errorThrown()
    {
        Mockito.when(friendshipRepository.findById(Mockito.any())).thenReturn(Optional.empty());



        Assertions.assertThrows(EntityNotFoundException.class, () -> { friendshipService.deleteFriendship(1L);});
        Mockito.verify(friendshipRepository, Mockito.never()).deleteById(Mockito.anyLong());
    }



}
