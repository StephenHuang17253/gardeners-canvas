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

    private User owner;

    @BeforeEach
    void prepare_test() {
        gridItemLocationService = new GridItemLocationService(gridItemLocationRepository);


        if (!userService.emailInUse("GridItemLocationServiceIntegrationTest@IntegrationTest.com")) {
            owner = new User("John", "Test", "GridItemLocationServiceIntegrationTest@IntegrationTest.com",
                    LocalDate.of(2003, 5, 2));
            userService.addUser(owner, "cheeseCake");

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
            gardenService.addGarden(garden2D);
        }
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

}
