package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GardenServiceIntegrationTest {
    @Autowired
    private GardenRepository gardenRepository;
    @Autowired
    private GardenService gardenService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    private List<Garden> gardenList = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
    LocalDate date = LocalDate.parse("01/01/2001", formatter);

    @BeforeEach
    void ClearRepository_AddUsersAndGardens() {
        gardenList = new ArrayList<>();
        userRepository.deleteAll();
        User user1 = new User("John", "Doe", "johnDoe@email.com", date);
        User user2 = new User("Jane", "Doe", "janeDoe@email.com", date);
        User user3 = new User("Bruce", "Wayne", "bruceWyane@email.com", date);
        userService.addUser(user1, "1es1P@ssword");
        userService.addUser(user2, "1es1P@ssword");
        userService.addUser(user3, "1es1P@ssword");
        Garden garden1 = new Garden(
                "John's Garden",
                "",
                "114 Ilam Road",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                10.0,
                false,
                "-43.5214643",
                "172.5796159",
                user1);
        Garden garden2 = new Garden(
                "John's Garden",
                "",
                "114 Ilam Road",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                10.0,
                false,
                "-43.5214643",
                "172.5796159",
                user1);
        Garden garden3 = new Garden(
                "Jane's Garden",
                "",
                "20 Kirkwood Avenue",
                "Upper Riccarton",
                "Christchurch",
                "8041",
                "New Zealand",
                20.0,
                false,
                "-43.5214643",
                "172.5796159",
                user2);
        gardenList.add(garden1);
        gardenList.add(garden2);
        gardenList.add(garden3);
        gardenRepository.saveAll(gardenList);
    }

    @Test
    void GetAllUsersGardens_UserInPersistenceAndOwnsSingleGardens() {
        List<Garden> expectedGardens = new ArrayList<>();
        expectedGardens.add(gardenList.get(2));
        List<Garden> actualGardens = gardenService.getAllUsersGardens(2L);
        Assertions.assertEquals(1, actualGardens.size());

        for (int i = 0; i < expectedGardens.size(); i++) {
            Garden expectedGarden = expectedGardens.get(i);
            Garden actualGarden = actualGardens.get(i);

            Assertions.assertEquals(expectedGarden.getGardenName(), actualGarden.getGardenName());
            Assertions.assertEquals(expectedGarden.getGardenLocation(), actualGarden.getGardenLocation());
            Assertions.assertEquals(expectedGarden.getGardenSize(), actualGarden.getGardenSize());
            Assertions.assertEquals(expectedGarden.getOwner().getId(), actualGarden.getOwner().getId());
        }
    }

    @Test
    void GetAllUsersGardens_UserInPersistenceAndOwnsMultipleGardens() {
        List<Garden> expectedGardens = new ArrayList<>();
        expectedGardens.add(gardenList.get(0));
        List<Garden> actualGardens = gardenService.getAllUsersGardens(1L);
        Assertions.assertEquals(2, actualGardens.size());

        for (int i = 0; i < expectedGardens.size(); i++) {
            Garden expectedGarden = expectedGardens.get(i);
            Garden actualGarden = actualGardens.get(i);

            Assertions.assertEquals(expectedGarden.getGardenName(), actualGarden.getGardenName());
            Assertions.assertEquals(expectedGarden.getGardenLocation(), actualGarden.getGardenLocation());
            Assertions.assertEquals(expectedGarden.getGardenSize(), actualGarden.getGardenSize());
            Assertions.assertEquals(expectedGarden.getOwner().getId(), actualGarden.getOwner().getId());
        }
    }

    @Test
    void GetAllUsersGardens_UserInPersistenceAndOwnsNoGardens() {
        List<Garden> actualGardens = gardenService.getAllUsersGardens(3L);
        Assertions.assertEquals(0, actualGardens.size());
    }

    @Test
    void GetAllUsersGardens_UserNotInPersistence_ThrowsIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            gardenService.getAllUsersGardens(4L);
        });
    }

    @Test
    void FindById_GardenIdExists() {
        List<Garden> expectedGardens = new ArrayList<>();
        expectedGardens.add(gardenList.get(0));
        Optional<Garden> optionalGarden = gardenService.getGardenById(1L);
        Assertions.assertTrue(optionalGarden.isPresent());

        Garden expectedGarden = gardenList.get(0);
        Garden actualGarden = optionalGarden.get();

        Assertions.assertEquals(expectedGarden.getGardenName(), actualGarden.getGardenName());
        Assertions.assertEquals(expectedGarden.getGardenLocation(), actualGarden.getGardenLocation());
        Assertions.assertEquals(expectedGarden.getGardenSize(), actualGarden.getGardenSize());
        Assertions.assertEquals(expectedGarden.getOwner().getId(), actualGarden.getOwner().getId());
    }

    @Test
    void FindById_GardenIdDoseNotExist() {
        Optional<Garden> optionalGarden = gardenService.getGardenById(4L);
        Assertions.assertFalse(optionalGarden.isPresent());
    }

    @Test
    void AddGarden_UserInPersistence() {
        Garden garden = new Garden(
                "Bat Cave",
                "",
                "1 Wayne Manor",
                "Wayne Island",
                "Gotham City",
                "8041",
                "United States of America",
                20.0,
                false,
                "-43.5214643",
                "172.5796159",
                userService.getUserById(3L));
        gardenService.addGarden(garden);
        Optional<Garden> optionalGarden = gardenService.getGardenById(4L);
        Assertions.assertTrue(optionalGarden.isPresent());

        Garden expectedGarden = garden;
        Garden actualGarden = optionalGarden.get();

        Assertions.assertEquals(expectedGarden.getGardenName(), actualGarden.getGardenName());
        Assertions.assertEquals(expectedGarden.getGardenLocation(), actualGarden.getGardenLocation());
        Assertions.assertEquals(expectedGarden.getGardenSize(), actualGarden.getGardenSize());
        Assertions.assertEquals(expectedGarden.getOwner().getId(), actualGarden.getOwner().getId());
    }

    @Test
    void AddGarden_UserNotInPersistence_ThrowsIllegalArgumentException() {
        User user = new User("Boogie", "Man", "boogieMan@email.com", date);
        Garden garden = new Garden(
                "Bat Cave",
                "",
                "1 Wayne Manor",
                "Wayne Island",
                "Gotham City",
                "8041",
                "United States of America",
                20.0,
                false,
                "-43.5214643",
                "172.5796159",
                user);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            gardenService.addGarden(garden);
        });
    }

    @Test
    void UpdateGarden_GardenInPersistence() {
        Garden gardenWithUpdatedValues = new Garden(
                "Jane's Garden",
                "",
                "20 Kirkwood Avenue",
                "Upper Riccarton",
                "Christchurch",
                "8041",
                "New Zealand",
                20.0,
                false,
                "-43.5214643",
                "172.5796159");

        gardenService.updateGarden(1L, gardenWithUpdatedValues);
        User user = userService.getUserById(1L);
        Garden garden = user.getGardens().get(0);
        Assertions.assertEquals(2, user.getGardens().size());
        Assertions.assertEquals(gardenWithUpdatedValues.getGardenName(), garden.getGardenName());
        Assertions.assertEquals(gardenWithUpdatedValues.getGardenLocation(), garden.getGardenLocation());
        Assertions.assertEquals(gardenWithUpdatedValues.getGardenSize(), garden.getGardenSize());
    }

    @Test
    void UpdateGarden_GardenNotInPersistence_ThrowsIllegalArgumentException() {
        User user = userService.getUserById(4L);
        Garden gardenWithUpdatedValues = new Garden(
                "Jane's Garden",
                "",
                "20 Kirkwood Avenue",
                "Upper Riccarton",
                "Christchurch",
                "8041",
                "New Zealand",
                20.0,
                false,
                "-43.5214643",
                "172.5796159",
                user);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            gardenService.updateGarden(4L, gardenWithUpdatedValues);
        });
    }

    @Test
    void AddPlantToGarden_GardenInPersistence() {
        // Given
        Garden garden = userService.getUserById(1L).getGardens().get(0);
        LocalDate dateOfPlanting = LocalDate.of(2024, 3, 14);
        Plant plant = new Plant("John's Plant", 3, "Plant owned by John", dateOfPlanting, garden);

        // When
        gardenService.addPlantToGarden(1L, plant);

        // That
        Garden resultGarden = userService.getUserById(1L).getGardens().get(0);
        Assertions.assertEquals(1, resultGarden.getPlants().size());
        Plant resultPlant = resultGarden.getPlants().get(0);
        Assertions.assertEquals(resultPlant.getPlantName(), "John's Plant");
        Assertions.assertEquals(resultPlant.getPlantCount(), 3);
        Assertions.assertEquals(resultPlant.getPlantDescription(), "Plant owned by John");
        Assertions.assertEquals(resultPlant.getPlantDate(), dateOfPlanting);
        Assertions.assertEquals(resultPlant.getGarden().getGardenId(), garden.getGardenId());
    }

    @Test
    void AddPlantToGarden_GardenNotInPersistence_ThrowsIllegalArgumentException() {
        User user = userService.getUserById(4L);
        Garden garden = new Garden(
                "Jane's Garden",
                "",
                "20 Kirkwood Avenue",
                "Upper Riccarton",
                "Christchurch",
                "8041",
                "New Zealand",
                20.0,
                false,
                "-43.5214643",
                "172.5796159",
                user);
        LocalDate dateOfPlanting = LocalDate.of(2024, 3, 14);
        Plant plant = new Plant("John's Plant", 3, "Plant owned by John", dateOfPlanting, garden);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            gardenService.addPlantToGarden(4L, plant);
        });
    }

}
