package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.UserInteraction;
import nz.ac.canterbury.seng302.gardenersgrove.repository.HomePageLayoutRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserInteractionRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserInteractionService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.util.ItemType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserInteractionServiceIntergrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserInteractionRepository userInteractionRepository;

    @Autowired
    public HomePageLayoutRepository homePageLayoutRepository;

    private UserInteractionService userInteractionService;

    private UserService userService;

    @Autowired
    private PlantService plantService;

    @Autowired
    private GardenService gardenService;

    private static User user1;
    private static User user2;
    private static User user3;

    private static final Long MAX_LONG = 1000L;

    private List<Garden> gardenList = new ArrayList<>();
    private List<Plant> plantList = new ArrayList<>();

    @BeforeAll
    void before_or_after_all() {
        userService = new UserService(passwordEncoder, userRepository, homePageLayoutRepository);
        userInteractionService = new UserInteractionService(userInteractionRepository, userService, gardenService,
                plantService);
        user1 = new User("John", "Doe", "jhonDoe@UserInteractionServiceIntergrationTest.com", LocalDate.of(2003, 5, 2));
        user2 = new User("Jane", "Doe", "janeDoe@UserInteractionServiceIntergrationTest.com", LocalDate.of(2003, 5, 2));
        user3 = new User("Sheldon", "Cooper", "sheldonCooper@UserInteractionServiceIntergrationTest.com",
                LocalDate.of(2003, 5, 2));
        userService.addUser(user1, "1es1P@ssword");
        userService.addUser(user2, "1es1P@ssword");
        userService.addUser(user3, "1es1P@ssword");
        Garden garden1 = new Garden(
                "garden1",
                "garden1",
                "garden1",
                "garden1",
                "garden1",
                "80",
                "garden1",
                10.0,
                false,
                "",
                "",
                user1);
        Garden garden2 = new Garden(
                "garden2",
                "garden2",
                "garden2",
                "garden2",
                "garden2",
                "80",
                "garden2",
                10.0,
                false,
                "",
                "",
                user1);
        Garden garden3 = new Garden(
                "garden2",
                "garden2",
                "garden2",
                "garden2",
                "garden2",
                "80",
                "garden2",
                10.0,
                false,
                "",
                "",
                user2);

        gardenList.add(gardenService.addGarden(garden1));
        gardenList.add(gardenService.addGarden(garden2));
        gardenList.add(gardenService.addGarden(garden3));
        Plant plant1 = plantService.addPlant(
                "John's Plant",
                3,
                "Plant owned by John",
                LocalDate.of(2003, 5, 2),
                1L);
        plantList.add(plant1);
    }

    @BeforeEach
    void clear_repo() {
        userInteractionRepository.deleteAll();
    }

    @Test
    void getUserInteractionById_UserInteractionNotInPersistence_returnsOptionalUserInteraction() {
        Optional<UserInteraction> userInteraction = userInteractionService.getUserInteractionById(MAX_LONG);

        Assertions.assertTrue(userInteraction.isEmpty());
    }

    @Test
    void getUserInteractionById_UserInteractionInPersistence_returnsOptionalUserInteraction() {
        Long gardenId = gardenList.get(0).getGardenId();
        LocalDateTime interactionTime = LocalDateTime.of(2023, 7, 15, 10, 0);
        UserInteraction expectedUserInteraction = new UserInteraction(user1, gardenId, ItemType.GARDEN,
                interactionTime);
        userInteractionRepository.save(expectedUserInteraction);

        Optional<UserInteraction> optionalUserInteraction = userInteractionService
                .getUserInteractionById(expectedUserInteraction.getUserInteractionId());

        Assertions.assertTrue(optionalUserInteraction.isPresent());
        UserInteraction actualUserInteraction = optionalUserInteraction.get();

        Assertions.assertEquals(expectedUserInteraction.getUserInteractionId(),
                actualUserInteraction.getUserInteractionId());
        Assertions.assertEquals(expectedUserInteraction.getUser().getId(), actualUserInteraction.getUser().getId());
        Assertions.assertEquals(expectedUserInteraction.getItemId(), actualUserInteraction.getItemId());
        Assertions.assertEquals(expectedUserInteraction.getItemType(), actualUserInteraction.getItemType());
        Assertions.assertEquals(expectedUserInteraction.getInteractionTime(),
                actualUserInteraction.getInteractionTime());
    }

    @Test
    void getAllUsersUserInteractions_UserNotInPersistence_ThrowsIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userInteractionService.getAllUsersUserInteractions(MAX_LONG);
        });
    }

    @Test
    void getAllUsersUserInteractions_UserInPersistenceAndNoUserInteractions_returnsListUserInteraction() {
        List<UserInteraction> expectedUserInteractions = userInteractionService
                .getAllUsersUserInteractions(user3.getId());

        Assertions.assertEquals(0, expectedUserInteractions.size());
    }

    @Test
    void getAllUsersUserInteractions_UserInPersistenceAndHasSingleUserInteraction_returnsListUserInteraction() {
        Long gardenId = gardenList.get(0).getGardenId();
        LocalDateTime interactionTime = LocalDateTime.of(2023, 7, 15, 10, 0);
        List<UserInteraction> expectedUserInteractions = List
                .of(new UserInteraction(user1, gardenId, ItemType.GARDEN, interactionTime));
        userInteractionRepository.saveAll(expectedUserInteractions);

        List<UserInteraction> actualUserInteractions = userInteractionService
                .getAllUsersUserInteractions(user1.getId());
        Assertions.assertEquals(1, actualUserInteractions.size());

        for (int i = 0; i < expectedUserInteractions.size(); i++) {
            UserInteraction expectedUserInteraction = actualUserInteractions.get(i);
            UserInteraction actualUserInteraction = actualUserInteractions.get(i);
            Assertions.assertEquals(expectedUserInteraction.getUserInteractionId(),
                    actualUserInteraction.getUserInteractionId());
            Assertions.assertEquals(expectedUserInteraction.getUser().getId(), actualUserInteraction.getUser().getId());
            Assertions.assertEquals(expectedUserInteraction.getItemId(), actualUserInteraction.getItemId());
            Assertions.assertEquals(expectedUserInteraction.getItemType(), actualUserInteraction.getItemType());
            Assertions.assertEquals(expectedUserInteraction.getInteractionTime(),
                    actualUserInteraction.getInteractionTime());
        }
    }

    @Test
    void getAllUsersUserInteractions_UserInPersistenceAndHasMultipleUserInteractionsSameType_returnsListUserInteraction() {
        Long gardenId1 = gardenList.get(0).getGardenId();
        Long gardenId2 = gardenList.get(1).getGardenId();
        LocalDateTime interactionTime = LocalDateTime.of(2023, 7, 15, 10, 0);
        List<UserInteraction> expectedUserInteractions = List.of(
                new UserInteraction(user1, gardenId1, ItemType.GARDEN, interactionTime),
                new UserInteraction(user1, gardenId2, ItemType.GARDEN, interactionTime));
        userInteractionRepository.saveAll(expectedUserInteractions);

        List<UserInteraction> actualUserInteractions = userInteractionService
                .getAllUsersUserInteractions(user1.getId());
        Assertions.assertEquals(2, actualUserInteractions.size());

        for (int i = 0; i < expectedUserInteractions.size(); i++) {
            UserInteraction expectedUserInteraction = expectedUserInteractions.get(i);
            UserInteraction actualUserInteraction = actualUserInteractions.get(i);
            Assertions.assertEquals(expectedUserInteraction.getUserInteractionId(),
                    actualUserInteraction.getUserInteractionId());
            Assertions.assertEquals(expectedUserInteraction.getUser().getId(), actualUserInteraction.getUser().getId());
            Assertions.assertEquals(expectedUserInteraction.getItemId(), actualUserInteraction.getItemId());
            Assertions.assertEquals(expectedUserInteraction.getItemType(), actualUserInteraction.getItemType());
            Assertions.assertEquals(expectedUserInteraction.getInteractionTime(),
                    actualUserInteraction.getInteractionTime());
        }
    }

    @Test
    void getAllUsersUserInteractions_UserInPersistenceAndHasMultipleUserInteractionsDifferentTypes_returnsListUserInteraction() {
        Long gardenId = gardenList.get(2).getGardenId();
        Long plantId = plantList.get(0).getPlantId();
        LocalDateTime interactionTime = LocalDateTime.of(2023, 7, 15, 10, 0);
        List<UserInteraction> expectedUserInteractions = List.of(
                new UserInteraction(user2, gardenId, ItemType.GARDEN, interactionTime),
                new UserInteraction(user2, plantId, ItemType.PLANT, interactionTime));
        userInteractionRepository.saveAll(expectedUserInteractions);

        List<UserInteraction> actualUserInteractions = userInteractionService
                .getAllUsersUserInteractions(user2.getId());
        Assertions.assertEquals(2, actualUserInteractions.size());

        for (int i = 0; i < expectedUserInteractions.size(); i++) {
            UserInteraction expectedUserInteraction = expectedUserInteractions.get(i);
            UserInteraction actualUserInteraction = actualUserInteractions.get(i);
            Assertions.assertEquals(expectedUserInteraction.getUserInteractionId(),
                    actualUserInteraction.getUserInteractionId());
            Assertions.assertEquals(expectedUserInteraction.getUser().getId(), actualUserInteraction.getUser().getId());
            Assertions.assertEquals(expectedUserInteraction.getItemId(), actualUserInteraction.getItemId());
            Assertions.assertEquals(expectedUserInteraction.getItemType(), actualUserInteraction.getItemType());
            Assertions.assertEquals(expectedUserInteraction.getInteractionTime(),
                    actualUserInteraction.getInteractionTime());
        }
    }

    @Test
    void getAllUsersUserInteractions_UserInPersistenceAndHasMultipleUserInteractionsDifferentTypesAndNotOwner_returnsListUserInteraction() {
        Long gardenId = gardenList.get(2).getGardenId();
        Long plantId = plantList.get(0).getPlantId();
        LocalDateTime interactionTime = LocalDateTime.of(2023, 7, 15, 10, 0);
        List<UserInteraction> expectedUserInteractions = List.of(
                new UserInteraction(user3, gardenId, ItemType.GARDEN, interactionTime),
                new UserInteraction(user3, plantId, ItemType.PLANT, interactionTime));
        userInteractionRepository.saveAll(expectedUserInteractions);

        List<UserInteraction> actualUserInteractions = userInteractionService
                .getAllUsersUserInteractions(user3.getId());
        Assertions.assertEquals(2, actualUserInteractions.size());

        for (int i = 0; i < expectedUserInteractions.size(); i++) {
            UserInteraction expectedUserInteraction = expectedUserInteractions.get(i);
            UserInteraction actualUserInteraction = actualUserInteractions.get(i);
            Assertions.assertEquals(expectedUserInteraction.getUserInteractionId(),
                    actualUserInteraction.getUserInteractionId());
            Assertions.assertEquals(expectedUserInteraction.getUser().getId(), actualUserInteraction.getUser().getId());
            Assertions.assertEquals(expectedUserInteraction.getItemId(), actualUserInteraction.getItemId());
            Assertions.assertEquals(expectedUserInteraction.getItemType(), actualUserInteraction.getItemType());
            Assertions.assertEquals(expectedUserInteraction.getInteractionTime(),
                    actualUserInteraction.getInteractionTime());
        }
    }

    @Test
    void addUserInteraction_UserNotInPersistence_ThrowsIllegalArgumentException() {
        LocalDateTime interactionTime = LocalDateTime.of(2023, 7, 15, 10, 0);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userInteractionService.addUserInteraction(MAX_LONG, MAX_LONG, ItemType.GARDEN, interactionTime);
        });
    }

    @Test
    void addUserInteraction_UserInPersistenceAndItemNotInPersistence_ThrowsIllegalArgumentException() {
        LocalDateTime interactionTime = LocalDateTime.of(2023, 7, 15, 10, 0);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userInteractionService.addUserInteraction(user1.getId(), MAX_LONG, ItemType.GARDEN, interactionTime);
        });
    }

    @Test
    void addUserInteraction_UserInPersistenceAndItemInPersistenceAndOwner_returnsUserInteraction() {
        LocalDateTime interactionTime = LocalDateTime.of(2023, 7, 15, 10, 0);
        Long gardenId = gardenList.get(1).getGardenId();
        UserInteraction expectedUserInteraction = new UserInteraction(user1, gardenId, ItemType.GARDEN,
                interactionTime);

        UserInteraction actualUserInteraction = userInteractionService.addUserInteraction(user1.getId(), gardenId,
                ItemType.GARDEN, interactionTime);

        Assertions.assertEquals(expectedUserInteraction.getUser().getId(), actualUserInteraction.getUser().getId());
        Assertions.assertEquals(expectedUserInteraction.getItemId(), actualUserInteraction.getItemId());
        Assertions.assertEquals(expectedUserInteraction.getItemType(), actualUserInteraction.getItemType());
        Assertions.assertEquals(expectedUserInteraction.getInteractionTime(),
                actualUserInteraction.getInteractionTime());
    }

    @Test
    void addUserInteraction_UserInPersistenceAndItemInPersistenceAndNotOwner_returnsUserInteraction() {
        Long gardenId = gardenList.get(1).getGardenId();
        LocalDateTime interactionTime = LocalDateTime.of(2023, 7, 15, 10, 0);
        UserInteraction expectedUserInteraction = new UserInteraction(user3, gardenId, ItemType.GARDEN,
                interactionTime);

        UserInteraction actualUserInteraction = userInteractionService.addUserInteraction(user3.getId(), gardenId,
                ItemType.GARDEN, interactionTime);

        Assertions.assertEquals(expectedUserInteraction.getUser().getId(), actualUserInteraction.getUser().getId());
        Assertions.assertEquals(expectedUserInteraction.getItemId(), actualUserInteraction.getItemId());
        Assertions.assertEquals(expectedUserInteraction.getItemType(), actualUserInteraction.getItemType());
        Assertions.assertEquals(expectedUserInteraction.getInteractionTime(),
                actualUserInteraction.getInteractionTime());
    }
}
