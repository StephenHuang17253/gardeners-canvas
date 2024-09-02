package nz.ac.canterbury.seng302.gardenersgrove.integration;

import jakarta.persistence.EntityNotFoundException;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GridItemLocation;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GridItemLocationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GridItemLocationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.util.GridItemType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class GridItemLocationServiceIntegrationTest {

    @Autowired
    private GridItemLocationRepository gridItemLocationRepository;

    private GridItemLocationService gridItemLocationService;

    @Autowired
    private GardenService gardenService;

    @Autowired
    private UserService userService;

    private Garden garden2D;
    private Garden garden2D2;

    private User owner;

    @BeforeEach
    void prepare_test() {
        gridItemLocationService = new GridItemLocationService(gridItemLocationRepository);

        if (!userService.emailInUse("GridItemLocationServiceIntegrationTest@IntegrationTest.com")) {
            owner = new User("John", "Test", "GridItemLocationServiceIntegrationTest@IntegrationTest.com",
                    LocalDate.of(2003, 5, 2));
            userService.addUser(owner, "cheeseCake");
        }
        if (garden2D == null) {
            garden2D = new Garden(
                    "2D Garden",
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
                    userService.getUserByEmail("GridItemLocationServiceIntegrationTest@IntegrationTest.com"));
        }
        garden2D = gardenService.addGarden(garden2D);

        if (garden2D2 == null) {
            garden2D2 = new Garden(
                    "2D2 Garden",
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
                    userService.getUserByEmail("GridItemLocationServiceIntegrationTest@IntegrationTest.com"));
        }
        garden2D2 = gardenService.addGarden(garden2D2);
    }

    @Test
    void updateGridItemLocation_AttemptToUpdateNonExistentGridItem_ThrowError() {

        GridItemLocation testGridItemLocation = new GridItemLocation(
                1000L,
                GridItemType.PLANT,
                garden2D,
                0,
                6
        );
        testGridItemLocation.setId(1L);

        Assertions.assertThrows(EntityNotFoundException.class,() -> {
            gridItemLocationService.updateGridItemLocation(testGridItemLocation);
        });
    }

    @Test
    void updateGridItemLocation_AttemptToUpdateNullGridItem_ThrowError() {

        GridItemLocation testGridItemLocation = new GridItemLocation(
                1000L,
                GridItemType.PLANT,
                garden2D,
                0,
                6
        );
        Assertions.assertThrows(IllegalArgumentException.class,() -> {
            gridItemLocationService.updateGridItemLocation(testGridItemLocation);
        });
    }

    @Test
    void addGridItemLocation_AttemptToAddGridItem_AddsItem() {

        GridItemLocation testGridItemLocation = new GridItemLocation(
                1L,
                GridItemType.PLANT,
                garden2D,
                0,
                6
        );
        GridItemLocation saveGridItemLocation = gridItemLocationService.addGridItemLocation(testGridItemLocation);

        Assertions.assertNotNull(gridItemLocationService.getGridItemLocationById(saveGridItemLocation.getId()).get());
    }

    @Test
    void addGridItemLocation_AttemptToAddGridItemInSameLocation_DoesNotAddItem() {

        GridItemLocation testGridItemLocation1 = new GridItemLocation(
                1L,
                GridItemType.PLANT,
                garden2D,
                1,
                6
        );

        GridItemLocation testGridItemLocation2 = new GridItemLocation(
                1L,
                GridItemType.PLANT,
                garden2D,
                1,
                6
        );
        gridItemLocationService.addGridItemLocation(testGridItemLocation1);
        Assertions.assertThrows(IllegalArgumentException.class,() -> {
            gridItemLocationService.addGridItemLocation(testGridItemLocation2);
        });
    }

    @Test
    void addGridItemLocation_AttemptToAddGridItemInSameLocationDifferentGardens_AddsItem() {

        GridItemLocation testGridItemLocation1 = new GridItemLocation(
                1L,
                GridItemType.PLANT,
                garden2D,
                2,
                6
        );

        GridItemLocation testGridItemLocation2 = new GridItemLocation(
                1L,
                GridItemType.PLANT,
                garden2D2,
                2,
                6
        );

        GridItemLocation savedGrid1 =  gridItemLocationService.addGridItemLocation(testGridItemLocation1);
        GridItemLocation savedGrid2 =  gridItemLocationService.addGridItemLocation(testGridItemLocation2);

        Assertions.assertTrue(gridItemLocationService.getGridItemLocationById(savedGrid1.getId()).isPresent());
        Assertions.assertTrue(gridItemLocationService.getGridItemLocationById(savedGrid2.getId()).isPresent());
    }

    @Test
    void getGridItemLocationByGarden_AttemptToGetGridItemLocationByGardenFromEmptyRepo_ReturnEmptyList() {
        gridItemLocationRepository.deleteAll();

        List<GridItemLocation> gridItemLocationsForGarden = gridItemLocationService.getGridItemLocationByGarden(garden2D);

        Assertions.assertTrue(gridItemLocationsForGarden.isEmpty());
    }

    @Test
    void getGridItemLocationByGarden_AttemptToGetGridItemLocationByGarden_ReturnListWithOneRecord() {
        gridItemLocationRepository.deleteAll();

        gridItemLocationService.addGridItemLocation(new GridItemLocation(
                1L,
                GridItemType.PLANT,
                garden2D,
                2,
                6
        ));

        List<GridItemLocation> gridItemLocationsForGarden = gridItemLocationService.getGridItemLocationByGarden(garden2D);

        Assertions.assertEquals(1, gridItemLocationsForGarden.size());
    }

    @Test
    void getGridItemLocationByGarden_AttemptToGetGridItemLocationByGardenRecordsGardens_ReturnListWithTwoRecords() {
        gridItemLocationRepository.deleteAll();

        gridItemLocationService.addGridItemLocation(new GridItemLocation(
                1L,
                GridItemType.PLANT,
                garden2D,
                2,
                6
        ));

        gridItemLocationService.addGridItemLocation(new GridItemLocation(
                1L,
                GridItemType.PLANT,
                garden2D,
                1,
                6
        ));

        List<GridItemLocation> gridItemLocationsForGarden = gridItemLocationService.getGridItemLocationByGarden(garden2D);

        Assertions.assertEquals(2, gridItemLocationsForGarden.size());
    }

    @Test
    void getGridItemLocationByGarden_AttemptToGetGridItemLocationByGardenMultipleGardens_ReturnListWithOneRecords() {
        gridItemLocationRepository.deleteAll();

        gridItemLocationService.addGridItemLocation(new GridItemLocation(
                1L,
                GridItemType.PLANT,
                garden2D,
                2,
                6
        ));

        gridItemLocationService.addGridItemLocation(new GridItemLocation(
                1L,
                GridItemType.PLANT,
                garden2D2,
                1,
                6
        ));

        List<GridItemLocation> gridItemLocationsForGarden = gridItemLocationService.getGridItemLocationByGarden(garden2D);

        Assertions.assertEquals(1, gridItemLocationsForGarden.size());
    }

    @Test
    void getAllGridItemLocations_GetAllGridItemLocation_ReturnListWithTwoRecords() {
        gridItemLocationRepository.deleteAll();

        gridItemLocationService.addGridItemLocation(new GridItemLocation(
                1L,
                GridItemType.PLANT,
                garden2D,
                2,
                6
        ));

        gridItemLocationService.addGridItemLocation(new GridItemLocation(
                1L,
                GridItemType.PLANT,
                garden2D2,
                1,
                6
        ));

        List<GridItemLocation> gridItemLocationsForGarden = gridItemLocationService.getAllGridItemLocations();

        Assertions.assertEquals(2, gridItemLocationsForGarden.size());
    }

    @Test
    void removeGridItemLocation_removeOneGridItemLocation_ReturnListWithOneRecords() {
        gridItemLocationRepository.deleteAll();

        GridItemLocation testLocation1 = gridItemLocationService.addGridItemLocation(new GridItemLocation(
                1L,
                GridItemType.PLANT,
                garden2D,
                2,
                6
        ));

        GridItemLocation testLocation2 = gridItemLocationService.addGridItemLocation(new GridItemLocation(
                1L,
                GridItemType.PLANT,
                garden2D2,
                1,
                6
        ));

        List<GridItemLocation> allGridItemLocations1 = gridItemLocationService.getAllGridItemLocations();

        int initialNumberOfGridItemLocations = allGridItemLocations1.size();

        Assertions.assertTrue(initialNumberOfGridItemLocations >= 2);

        allGridItemLocations1.forEach(item -> System.out.println(item));

        System.out.println(initialNumberOfGridItemLocations);



        System.out.println(testLocation1);

        gridItemLocationService.removeGridItemLocation(testLocation1);



        List<GridItemLocation> allGridItemLocations2 = gridItemLocationService.getAllGridItemLocations();

        allGridItemLocations2.forEach(item -> System.out.println(item));

        System.out.println(allGridItemLocations2.size());

        Assertions.assertEquals(initialNumberOfGridItemLocations - 1, allGridItemLocations2.size());

        Assertions.assertFalse(gridItemLocationService.getGridItemLocationById(testLocation1.getId()).isPresent());
        Assertions.assertTrue(gridItemLocationService.getGridItemLocationById(testLocation2.getId()).isPresent());
    }
}