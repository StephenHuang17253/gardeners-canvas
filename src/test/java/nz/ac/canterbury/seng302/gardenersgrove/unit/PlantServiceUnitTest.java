package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.UserInteraction;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.ItemType;
import nz.ac.canterbury.seng302.gardenersgrove.util.PlantCategory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class for unit testing PlantService methods
 */

@SpringBootTest
class PlantServiceUnitTest {

    private static PlantService plantService;

    private static PlantRepository plantRepository;

    private static GardenService gardenService;

    private static FileService fileService;

    private static User owner;


    @BeforeAll
    static void setup() {
        plantRepository = Mockito.mock(PlantRepository.class);
        gardenService = Mockito.mock(GardenService.class);
        fileService = Mockito.mock(FileService.class);
        plantService = new PlantService(plantRepository, gardenService, fileService);
        owner = new User("John", "Test", "profile.user.test@ProfileController.com", LocalDate.of(2003, 5, 2));
    }


    @Test
    void getPlantsByInteraction_noInteractions_returnEmptyList() {
        //make empty list
        List<UserInteraction> userInteractions = new ArrayList<>();

        //run function
        List<Plant> returnedPlants = plantService.getPlantsByInteraction(userInteractions);

        //make sure return is empty
        Assertions.assertTrue(returnedPlants.isEmpty());

    }


    @Test
    void getPlantsByInteraction_TwoInteractions_returnListOfPlants() {
        // Make List of user interactions by creating plant and garden required
        LocalDate dateOfPlanting1 = LocalDate.of(2024, 3, 14);
        LocalDate dateOfPlanting2 = LocalDate.of(2023, 2, 13);
        Garden garden = gardenService.addGarden(new Garden(
                "John's Garden",
                "",
                "114 Ilam Road",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                15.0,
                false,
                "-43.5214643",
                "172.5796159",
                owner));
        Plant plant1 = new Plant("John's Plant", 3, "Plant owned by John", dateOfPlanting1, garden, PlantCategory.TREE);
        Plant plant2 = new Plant("Jane's Plant", 4, "Plant owned by Jane", dateOfPlanting2, garden,PlantCategory.TREE);
        UserInteraction userInteraction1 = new UserInteraction(owner, 1L, ItemType.PLANT, LocalDateTime.now());
        UserInteraction userInteraction2 = new UserInteraction(owner, 2L, ItemType.PLANT, LocalDateTime.now());
        List<UserInteraction> userInteractions = List.of(userInteraction1, userInteraction2);

        //mock responses from repository
        Mockito.when(plantRepository.findById(1L)).thenReturn(Optional.of(plant1));
        Mockito.when(plantRepository.findById(2L)).thenReturn(Optional.of(plant2));

        //run function
        List<Plant> plants = plantService.getPlantsByInteraction(userInteractions);

        //make sure correct list of plants is returned
        Assertions.assertEquals(2, plants.size());

    }


}
