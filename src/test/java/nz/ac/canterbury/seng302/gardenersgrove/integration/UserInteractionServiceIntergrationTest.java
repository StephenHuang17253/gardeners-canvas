package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.UserInteraction;
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
public class UserInteractionServiceIntergrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserInteractionRepository userInteractionRepository;

    private UserInteractionService userInteractionService;

    private UserService userService;

    @Autowired
    private PlantService plantService;

    @Autowired
    private GardenService gardenService;

    private static User user1;
    private static User user2;
    private static User user3;

    private final Long MAX_LONG = 1000L;

    private List<Garden> gardenList = new ArrayList<>();
    private List<Plant> plantList = new ArrayList<>();

    @BeforeAll
    void before_or_after_all() {
        userService = new UserService(passwordEncoder, userRepository);
        userInteractionService = new UserInteractionService(userInteractionRepository, userService);
        user1 =  new User("John", "Doe", "jhonDoe@UserInteractionServiceIntergrationTest.com", LocalDate.of(2003,5,2));
        user2 =  new User("Jane", "Doe", "jhonDoe@UserInteractionServiceIntergrationTest.com", LocalDate.of(2003,5,2));
        user3 =  new User("Sheldon", "Cooper", "sheldonCooper@UserInteractionServiceIntergrationTest.com", LocalDate.of(2003,5,2));
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
                user1
        );
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
                user1
        );
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
                user2
        );

        Plant plant1 = plantService.addPlant("John's Plant", 3, "Plant owned by John", LocalDate.of(2003,5,2), 1L);
        userService.addUser(user1,"1es1P@ssword");
        userService.addUser(user2,"1es1P@ssword");
        userService.addUser(user3,"1es1P@ssword");
        gardenList.add(gardenService.addGarden(garden1));
        gardenList.add(gardenService.addGarden(garden2));
        gardenList.add(gardenService.addGarden(garden3));
        plantList.add(plant1);
    }


    @Test
    void getUserInteractionById_UserInteractionNotInPersistence_returnsOptionalUserInteraction(){
        Optional<UserInteraction> userInteraction = userInteractionService.getUserInteractionById(MAX_LONG);

        Assertions.assertTrue(userInteraction.isEmpty());
    }

    @Test
    void getUserInteractionById_UserInteractionInPersistence_returnsOptionalUserInteraction(){
        Long gardenId = gardenList.get(0).getGardenId();
        UserInteraction expectedUserInteraction = new UserInteraction(user1, gardenId, ItemType.GARDEN, LocalDateTime.MIN);
        userInteractionRepository.save(expectedUserInteraction);

        Optional<UserInteraction> optionalUserInteraction = userInteractionService.getUserInteractionById(expectedUserInteraction.getUserInteractionId());

        Assertions.assertTrue(optionalUserInteraction.isPresent());
        UserInteraction actualUserInteraction = optionalUserInteraction.get();

        Assertions.assertEquals(expectedUserInteraction.getUserInteractionId(), actualUserInteraction.getUserInteractionId());
        Assertions.assertEquals(expectedUserInteraction.getUser().getId(), actualUserInteraction.getUser().getId());
        Assertions.assertEquals(expectedUserInteraction.getItemId(), actualUserInteraction.getItemId());
        Assertions.assertEquals(expectedUserInteraction.getItemType(), actualUserInteraction.getItemType());
        Assertions.assertEquals(expectedUserInteraction.getInteractionTime(), actualUserInteraction.getInteractionTime());
    }

    @Test
    void getAllUsersUserInteractions_UserNotInPersistence_ThrowsIllegalArgumentException(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userInteractionService.getAllUsersUserInteractions(MAX_LONG);
        });
    }

    @Test
    void getAllUsersUserInteractions_UserInPersistenceAndNoUserInteractions_returnsListUserInteraction(){
        List<UserInteraction> expectedUserInteractions = userInteractionService.getAllUsersUserInteractions(user3.getId());

        Assertions.assertEquals(0,expectedUserInteractions.size());
    }

    @Test
    void getAllUsersUserInteractions_UserInPersistenceAndHasSingleUserInteraction_returnsListUserInteraction(){
        Long gardenId = gardenList.get(0).getGardenId();
        List<UserInteraction> expectedUserInteractions = List.of(new UserInteraction(user1, gardenId, ItemType.GARDEN, LocalDateTime.MIN));
        userInteractionRepository.saveAll(expectedUserInteractions);

        List<UserInteraction> actualUserInteractions = userInteractionService.getAllUsersUserInteractions(user1.getId());
        Assertions.assertEquals(1,actualUserInteractions.size());

        for(int i = 0; i < expectedUserInteractions.size(); i++){
            UserInteraction expectedUserInteraction = actualUserInteractions.get(i);
            UserInteraction actualUserInteraction = actualUserInteractions.get(i);
            Assertions.assertEquals(expectedUserInteraction.getUserInteractionId(), actualUserInteraction.getUserInteractionId());
            Assertions.assertEquals(expectedUserInteraction.getUser().getId(), actualUserInteraction.getUser().getId());
            Assertions.assertEquals(expectedUserInteraction.getItemId(), actualUserInteraction.getItemId());
            Assertions.assertEquals(expectedUserInteraction.getItemType(), actualUserInteraction.getItemType());
            Assertions.assertEquals(expectedUserInteraction.getInteractionTime(), actualUserInteraction.getInteractionTime());
        }
    }

    @Test
    void getAllUsersUserInteractions_UserInPersistenceAndHasMultipleUserInteractionsSameType_returnsListUserInteraction(){
        Long gardenId1 = gardenList.get(0).getGardenId();
        Long gardenId2 = gardenList.get(1).getGardenId();
        List<UserInteraction> expectedUserInteractions = List.of(new UserInteraction(user1, gardenId1, ItemType.GARDEN, LocalDateTime.MIN),
                new UserInteraction(user1, gardenId2, ItemType.GARDEN, LocalDateTime.MIN));
        userInteractionRepository.saveAll(expectedUserInteractions);

        List<UserInteraction> actualUserInteractions = userInteractionService.getAllUsersUserInteractions(user1.getId());
        Assertions.assertEquals(2,actualUserInteractions.size());

        for(int i = 0; i < expectedUserInteractions.size(); i++){
            UserInteraction expectedUserInteraction = actualUserInteractions.get(i);
            UserInteraction actualUserInteraction = actualUserInteractions.get(i);
            Assertions.assertEquals(expectedUserInteraction.getUserInteractionId(), actualUserInteraction.getUserInteractionId());
            Assertions.assertEquals(expectedUserInteraction.getUser().getId(), actualUserInteraction.getUser().getId());
            Assertions.assertEquals(expectedUserInteraction.getItemId(), actualUserInteraction.getItemId());
            Assertions.assertEquals(expectedUserInteraction.getItemType(), actualUserInteraction.getItemType());
            Assertions.assertEquals(expectedUserInteraction.getInteractionTime(), actualUserInteraction.getInteractionTime());
        }
    }

    @Test
    void getAllUsersUserInteractions_UserInPersistenceAndHasMultipleUserInteractionsDifferentTypes_returnsListUserInteraction(){
        Long gardenId = gardenList.get(2).getGardenId();
        Long plantId = plantList.get(0).getPlantId();
        List<UserInteraction> expectedUserInteractions = List.of(new UserInteraction(user1, gardenId, ItemType.GARDEN, LocalDateTime.MIN),
                new UserInteraction(user1, plantId, ItemType.PLANT, LocalDateTime.MIN));
        userInteractionRepository.saveAll(expectedUserInteractions);

        List<UserInteraction> actualUserInteractions = userInteractionService.getAllUsersUserInteractions(user2.getId());
        Assertions.assertEquals(2,actualUserInteractions.size());

        for(int i = 0; i < expectedUserInteractions.size(); i++){
            UserInteraction expectedUserInteraction = actualUserInteractions.get(i);
            UserInteraction actualUserInteraction = actualUserInteractions.get(i);
            Assertions.assertEquals(expectedUserInteraction.getUserInteractionId(), actualUserInteraction.getUserInteractionId());
            Assertions.assertEquals(expectedUserInteraction.getUser().getId(), actualUserInteraction.getUser().getId());
            Assertions.assertEquals(expectedUserInteraction.getItemId(), actualUserInteraction.getItemId());
            Assertions.assertEquals(expectedUserInteraction.getItemType(), actualUserInteraction.getItemType());
            Assertions.assertEquals(expectedUserInteraction.getInteractionTime(), actualUserInteraction.getInteractionTime());
        }
    }

    @Test
    void getAllUsersUserInteractions_UserInPersistenceAndHasMultipleUserInteractionsDifferentTypesAndNotOwner_returnsListUserInteraction(){
        Long gardenId = gardenList.get(2).getGardenId();
        Long plantId = plantList.get(0).getPlantId();
        List<UserInteraction> expectedUserInteractions = List.of(new UserInteraction(user3, gardenId, ItemType.GARDEN, LocalDateTime.MIN),
                new UserInteraction(user3, plantId, ItemType.PLANT, LocalDateTime.MIN));
        userInteractionRepository.saveAll(expectedUserInteractions);

        List<UserInteraction> actualUserInteractions = userInteractionService.getAllUsersUserInteractions(user3.getId());
        Assertions.assertEquals(2,actualUserInteractions.size());

        for(int i = 0; i < expectedUserInteractions.size(); i++){
            UserInteraction expectedUserInteraction = actualUserInteractions.get(i);
            UserInteraction actualUserInteraction = actualUserInteractions.get(i);
            Assertions.assertEquals(expectedUserInteraction.getUserInteractionId(), actualUserInteraction.getUserInteractionId());
            Assertions.assertEquals(expectedUserInteraction.getUser().getId(), actualUserInteraction.getUser().getId());
            Assertions.assertEquals(expectedUserInteraction.getItemId(), actualUserInteraction.getItemId());
            Assertions.assertEquals(expectedUserInteraction.getItemType(), actualUserInteraction.getItemType());
            Assertions.assertEquals(expectedUserInteraction.getInteractionTime(), actualUserInteraction.getInteractionTime());
        }
    }

    @Test
    void addUserInteraction_UserNotInPersistence_ThrowsIllegalArgumentException(){
        User userNotInRepo = new User("","","",null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userInteractionService.addUserInteraction(userNotInRepo, MAX_LONG, ItemType.GARDEN);
        });
    }

    @Test
    void addUserInteraction_UserInPersistenceAndItemNotInPersistence_ThrowsIllegalArgumentException(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userInteractionService.addUserInteraction(user1, MAX_LONG, ItemType.GARDEN);
        });
    }

    @Test
    void addUserInteraction_UserInPersistenceAndItemInPersistenceAndOwner_returnsUserInteraction(){
        Long gardenId = gardenList.get(1).getGardenId();
        UserInteraction expectedUserInteraction = new UserInteraction(user1,gardenId,ItemType.GARDEN,LocalDateTime.MIN);

        UserInteraction actualUserInteraction = userInteractionService.addUserInteraction(user1,gardenId,ItemType.GARDEN);

        Assertions.assertEquals(expectedUserInteraction.getUser().getId(), actualUserInteraction.getUser().getId());
        Assertions.assertEquals(expectedUserInteraction.getItemId(), actualUserInteraction.getItemId());
        Assertions.assertEquals(expectedUserInteraction.getItemType(), actualUserInteraction.getItemType());
        Assertions.assertEquals(expectedUserInteraction.getInteractionTime(), actualUserInteraction.getInteractionTime());
    }

    @Test
    void addUserInteraction_UserInPersistenceAndItemInPersistenceAndNotOwner_returnsUserInteraction(){
        Long gardenId = gardenList.get(1).getGardenId();
        UserInteraction expectedUserInteraction = new UserInteraction(user3,gardenId,ItemType.GARDEN,LocalDateTime.MIN);

        UserInteraction actualUserInteraction = userInteractionService.addUserInteraction(user1,gardenId,ItemType.GARDEN);

        Assertions.assertEquals(expectedUserInteraction.getUser().getId(), actualUserInteraction.getUser().getId());
        Assertions.assertEquals(expectedUserInteraction.getItemId(), actualUserInteraction.getItemId());
        Assertions.assertEquals(expectedUserInteraction.getItemType(), actualUserInteraction.getItemType());
        Assertions.assertEquals(expectedUserInteraction.getInteractionTime(), actualUserInteraction.getInteractionTime());
    }
}
