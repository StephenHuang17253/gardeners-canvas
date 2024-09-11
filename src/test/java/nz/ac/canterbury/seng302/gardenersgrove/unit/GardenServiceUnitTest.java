package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GardenServiceUnitTest {

    @Mock
    GardenRepository gardenRepository;

    @Mock
    private UserService userService;

    static GardenService gardenService;

    private Garden garden;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);

    LocalDate date = LocalDate.parse("01/01/2001", formatter);

    @BeforeEach
    void setup() {
        gardenRepository = Mockito.mock(GardenRepository.class);
        userService = Mockito.mock(UserService.class);
        gardenService = new GardenService(gardenRepository, userService);

        User testUser1 = Mockito.spy(new User("John", "Doe", "jhonDoe@ManageFriendsControllerIntegrationTest.com",
                LocalDate.of(2003, 5, 2)));

        Mockito.when(testUser1.getId()).thenReturn(1L);
        Mockito.when(userService.getUserById(1L)).thenReturn(testUser1);

        garden = new Garden(
                "test",
                "test",
                "test",
                "test",
                "test",
                "80",
                "test",
                10.0,
                false,
                "",
                "",
                testUser1);

        Optional<Garden> gardenOptional = Mockito.mock(Optional.class);
        Mockito.when(gardenOptional.get()).thenReturn(garden);
        Mockito.when(gardenOptional.isPresent()).thenReturn(true);
        Mockito.when(gardenRepository.findById(1L)).thenReturn(gardenOptional);

        // mocks the save function to output whatever was inputted into it.
        // https://stackoverflow.com/questions/2684630/making-a-mocked-method-return-an-argument-that-was-passed-to-it
        Mockito.when(gardenRepository.save(Mockito.any())).thenAnswer(input -> input.getArgument(0));
    }

    @Test
    void testUpdateGardenLocationCoordinates_Success() {
        Garden updatedGarden = gardenService.updateGardenCoordinates(1L, "1.1", "1.1");
        assertEquals("1.1", updatedGarden.getGardenLatitude());
        assertEquals("1.1", updatedGarden.getGardenLongitude());
    }

    @Test
    void testUpdateGardenLocationCoordinates_InvalidId() {
        Mockito.when(gardenRepository.findById(2L)).thenReturn(Optional.empty());
        Exception error = assertThrows(IllegalArgumentException.class, () -> {
            gardenService.updateGardenCoordinates(2L, "1.1", "1.1");
        });
        assertEquals("Invalid garden ID", error.getMessage());
        assertEquals("", garden.getGardenLongitude());
        assertEquals("", garden.getGardenLatitude());
    }
}
