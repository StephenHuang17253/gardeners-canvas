package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidation.InputValidator;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.LocalDate;

/**
 * Class for unit testing PlantService methods
 */
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
}
