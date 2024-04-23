package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
<<<<<<<< HEAD:src/test/java/nz/ac/canterbury/seng302/gardenersgrove/GardenServiceIntegrationTest.java
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
========
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
>>>>>>>> origin/dev:src/test/java/nz/ac/canterbury/seng302/gardenersgrove/integration/GardenServiceTest.java
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

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
    LocalDate date = LocalDate.parse("01/01/2001", formatter);

    @Test
    public void testGetGardens() {

        User user1 = new User("John","Doe","johnDoe@email.com", date);
        User user2 = new User("Jane","Doe","janeDoe@email.com", date);
        userService.addUser(user1,"1es1P@ssword");
        userService.addUser(user2,"1es1P@ssword");
        Garden garden1 = new Garden("John's Garden", "John's Backyard", 15, user1);
        Garden garden2 = new Garden("Jane's Garden", "Jane's Backyard", 20, user2);
        List<Garden> expectedGardens = new ArrayList<>();
        expectedGardens.add(garden1);
        expectedGardens.add(garden2);
        gardenRepository.save(garden1);
        gardenRepository.save(garden2);
        List<Garden> actualGardens = gardenService.getGardens();
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
    public void GetAllUsersGardens_UserInPersistenceAndOwnsGardens() {

        GardenService gardenService = new GardenService(gardenRepository);
        User user1 = new User("John","Doe","johnDoe@email.com", date);
        User user2 = new User("Jane","Doe","janeDoe@email.com", date);
        Garden garden1 = new Garden("John's Garden", "John's Backyard", 15, user1);
        Garden garden2 = new Garden("Jane's Garden", "Jane's Backyard", 20, user2);

        Mockito.when(user1.getId()).thenReturn(1L);
        Mockito.when(user2.getId()).thenReturn(2L);

        gardenRepository.save(garden1);
        gardenRepository.save(garden2);
        List<Garden> jhonsGardens = gardenService.getAllUsersGardens(user1.getId());
        List<Garden> janesGardens = gardenService.getAllUsersGardens(user2.getId());
        Assertions.assertTrue(jhonsGardens.contains(garden1));
        Assertions.assertTrue(janesGardens.contains(garden2));

    }
    @Mock
    GardenRepository gardenRepo;
    @Test
    public void testFindById() {
        User user = new User("John","Doe","johnDoe@email.com", date);
        Garden garden = new Garden("John's Garden", "John's Backyard", 15, user);
        Mockito.when(gardenRepo.findById(1L)).thenReturn(Optional.of(garden));

        GardenService gardenService = new GardenService(gardenRepo);
        Optional<Garden> optionalGarden = gardenService.findById(1L);

        Assertions.assertTrue(optionalGarden.isPresent());
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
