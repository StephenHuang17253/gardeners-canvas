package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@SpringBootTest
public class GardenServiceIntegrationTest {

    @Autowired
    private GardenRepository gardenRepository;

    @Autowired
    private GardenService gardenService;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    private List<Garden> gardenList = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
    LocalDate date = LocalDate.parse("01/01/2001", formatter);

    @BeforeEach
    void ClearRepository_AddUsersAndGardens() {
        userRepository.deleteAll();
        User user1 = new User("John","Doe","johnDoe@email.com", date);
        User user2 = new User("Jane","Doe","janeDoe@email.com", date);
        User user3 = new User("Bruce","Wayne","bruceWyane@email.com", date);
        userService.addUser(user1,"1es1P@ssword");
        userService.addUser(user2,"1es1P@ssword");
        userService.addUser(user3,"1es1P@ssword");
        Garden garden1 = new Garden("John's Garden", "John's Backyard", 15, user1);
        Garden garden2 = new Garden("John's Garden", "John's Backyard", 15, user1);
        Garden garden3 = new Garden("Jane's Garden", "Jane's Backyard", 20, user2);
        gardenList.add(garden1);
        gardenList.add(garden2);
        gardenList.add(garden3);
        gardenRepository.saveAll(gardenList);
    }
    @Test
    public void GetAllUsersGardens_UserInPersistenceAndOwnsSingleGardens() {
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
    public void GetAllUsersGardens_UserInPersistenceAndOwnsMultipleGardens() {
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
    public void GetAllUsersGardens_UserInPersistenceAndOwnsNoGardens() {
        List<Garden> actualGardens = gardenService.getAllUsersGardens(3L);
        Assertions.assertEquals(0, actualGardens.size());
    }
    @Test
    void GetAllUsersGardens_UserNotInPersistence_ThrowsIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            gardenService.getAllUsersGardens(3L);
        });
    }
    @Mock
    GardenRepository gardenRepo;
    @Test
    public void FindById_GardenIdExists() {
        List<Garden> expectedGardens = new ArrayList<>();
        expectedGardens.add(gardenList.get(0));
        Optional<Garden> optionalGarden = gardenService.findById(1L);
        Assertions.assertTrue(optionalGarden.isPresent());

        Garden expectedGarden = gardenList.get(0);
        Garden actualGarden =  optionalGarden.get();

        Assertions.assertEquals(expectedGarden.getGardenName(), actualGarden.getGardenName());
        Assertions.assertEquals(expectedGarden.getGardenLocation(), actualGarden.getGardenLocation());
        Assertions.assertEquals(expectedGarden.getGardenSize(), actualGarden.getGardenSize());
        Assertions.assertEquals(expectedGarden.getOwner().getId(), actualGarden.getOwner().getId());
    }
    @Test
    public void FindById_GardenIdDoseNotExist() {
        Optional<Garden> optionalGarden = gardenService.findById(1L);
        Assertions.assertFalse(optionalGarden.isPresent());
    }

    @Test
    public void testAddGarden() {
        GardenService gardenService = new GardenService(gardenRepository);
        User user = new User("John","Doe","johnDoe@email.com", date);
        Garden garden = gardenService.addGarden(new Garden("John's Garden", "John's Backyard", 15, user));
        Assertions.assertEquals(garden.getGardenName(), "John's Garden");
        Assertions.assertEquals(garden.getGardenLocation(), "John's Backyard");
        Assertions.assertEquals(garden.getGardenSize(), 15);
    }

    @Test
    public void testUpdateGarden() {
        GardenService gardenService = new GardenService(gardenRepository);
        User user = new User("John","Doe","johnDoe@email.com", date);
        Garden oldGarden = new Garden("John's Garden", "John's Backyard", 15, user);
        Garden newGarden = new Garden("Jane's Garden", "Jane's Backyard", 20);
        gardenService.addGarden(oldGarden);
        gardenService.updateGarden(oldGarden.getGardenId(), newGarden);
        Assertions.assertEquals(oldGarden.getGardenName(), newGarden.getGardenName());
        Assertions.assertEquals(oldGarden.getGardenLocation(), newGarden.getGardenLocation());
        Assertions.assertEquals(oldGarden.getGardenSize(), newGarden.getGardenSize());
    }

    @Test
    public void testAddPlantToGarden() {
        // Given
        LocalDate dateOfPlanting = LocalDate.of(2024, 3, 14);
        GardenService gardenService = new GardenService(gardenRepository);
        User user = new User("John","Doe","johnDoe@email.com", date);
        Garden garden = new Garden("John's Garden", "John's Backyard", 15, user);
        Plant plant = new Plant("John's Plant", 3, "Plant owned by John", dateOfPlanting, garden);

        // When
        gardenService.addGarden(garden);
        gardenService.addPlantToGarden(garden.getGardenId(), plant);

        // That
        Assertions.assertEquals(1, garden.getPlants().size());
        Plant resultPlant = garden.getPlants().get(0);
        Assertions.assertEquals(resultPlant.getPlantName(),"John's Plant");
        Assertions.assertEquals(resultPlant.getPlantCount(),3);
        Assertions.assertEquals(resultPlant.getPlantDescription(),"Plant owned by John");
        Assertions.assertEquals(resultPlant.getPlantDate(),dateOfPlanting);
        Assertions.assertEquals(resultPlant.getGarden(),garden);
    }


}
