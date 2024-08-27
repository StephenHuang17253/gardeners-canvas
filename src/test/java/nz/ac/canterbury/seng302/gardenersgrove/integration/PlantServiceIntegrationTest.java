package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.util.PlantCategory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PlantServiceIntegrationTest {

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private GardenService gardenService;

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    private PlantService plantService;

    private static User owner;

    private Garden testGarden;

    private Garden testGarden2;

    @BeforeEach
    void setup() {
        plantService = new PlantService(plantRepository, gardenService, fileService);
        if (!userService.emailInUse("PlantServiceIntegrationTest@email.com")) {
            owner = new User("John", "Test", "PlantServiceIntegrationTest@email.com",
                    LocalDate.of(2003, 5, 2));
            userService.addUser(owner, "cheeseCake");
        }
        if (testGarden == null) {
            testGarden = new Garden(
                    "Test Garden",
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
                    userService.getUserByEmail("PlantServiceIntegrationTest@email.com"));
            gardenService.addGarden(testGarden);
        }
        if (testGarden2 == null) {
            testGarden2 = new Garden(
                    "Test Garden 2",
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
                    userService.getUserByEmail("PlantServiceIntegrationTest@email.com"));
            gardenService.addGarden(testGarden2);
        }
    }

    @Test
    void testFindByCategory_ReturnPlantWithCategory() {
        Plant plant = plantService.addPlant(
                "Shrub",
                3,
                "Shrub planted by Stephen",
                LocalDate.of(2024, 8, 22),
                testGarden.getGardenId(),
                PlantCategory.SHRUB);

        List<Plant> shrubs = plantService.getPlantsByCategory(PlantCategory.SHRUB);

        Assertions.assertEquals(1, shrubs.size());
        Assertions.assertEquals(plant.getPlantName(), shrubs.get(0).getPlantName());
        Assertions.assertEquals(PlantCategory.SHRUB, shrubs.get(0).getPlantCategory());

    }

    @Test
    void testFindByCategory_AddTwoPlantsDifferentCategory_ReturnOnePlant() {

        Plant climber = plantService.addPlant(
                "Climber",
                3,
                "Climber planted by Stephen",
                LocalDate.of(2024, 8, 22),
                testGarden.getGardenId(),
                PlantCategory.CLIMBER);

        Plant creeper = plantService.addPlant(
                "Creeper",
                3,
                "Creeper planted by Stephen",
                LocalDate.of(2024, 8, 22),
                testGarden.getGardenId(),
                PlantCategory.CREEPER);

        List<Plant> climbers = plantService.getPlantsByCategory(PlantCategory.CLIMBER);

        Assertions.assertEquals(1, climbers.size());
        Assertions.assertEquals(climber.getPlantName(), climbers.get(0).getPlantName());
        Assertions.assertEquals(PlantCategory.CLIMBER, climbers.get(0).getPlantCategory());
        Assertions.assertFalse(climbers.contains(creeper));

    }

    @Test
    void testFindByGardenAndCategory_AddTwoPlantsSameCategoryDifferentGarden_ReturnOnePlant() {

        Plant tree = plantService.addPlant(
                "Tree in garden",
                3,
                "Tree planted by Stephen",
                LocalDate.of(2024, 8, 22),
                testGarden.getGardenId(),
                PlantCategory.TREE);

        Plant expectedTree = plantService.addPlant(
                "Tree in other garden",
                3,
                "Tree planted by Stephen",
                LocalDate.of(2024, 8, 22),
                testGarden2.getGardenId(),
                PlantCategory.TREE);

        List<Plant> trees = plantService.getPlantsByGardenAndCategory(testGarden2,PlantCategory.TREE);

        Assertions.assertEquals(1, trees.size());
        Assertions.assertEquals(expectedTree.getPlantName(), trees.get(0).getPlantName());
        Assertions.assertEquals(PlantCategory.TREE, trees.get(0).getPlantCategory());
        Assertions.assertFalse(trees.contains(tree));


    }

}
