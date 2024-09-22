package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Decoration;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.DecorationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.DecorationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.util.DecorationCategory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class DecorationServiceUnitTest {

    @Mock
    DecorationRepository decorationRepository;

    @Mock
    UserService userService;

    User testUser1;

    Garden garden;

    Decoration decoration1;

    Decoration decoration2;

    static DecorationService decorationService;

    @BeforeEach
    void before() {
        userService = Mockito.mock(UserService.class);
        decorationRepository = Mockito.mock(DecorationRepository.class);
        decorationService = new DecorationService(decorationRepository);

        testUser1 = Mockito.spy(new User("Lysander", "au Lune", "lysander@DecorationServiceUnitTest.com",
                LocalDate.of(2003,5,2)));
        garden = Mockito.spy(new Garden("Lune Gardens",
                "The Gardens of House Lune",
                "Luna, Sol",
                "",
                "Luna",
                "",
                "Sol",
                50.0,
                false,
                "83",
                "43",
                testUser1));

        Mockito.when(testUser1.getId()).thenReturn(1L);
        Mockito.when(userService.getUserById(1L)).thenReturn(testUser1);

        decoration1 = new Decoration(garden, DecorationCategory.FOUNTAIN);
        decoration1.setId(1L);

        decoration2 = new Decoration(garden, DecorationCategory.GNOME);
        decoration2.setId(2L);

    }

    @Test
    void getById_CorrectId_ReturnDecoration() {
        Mockito.when(decorationRepository.findById(decoration1.getId()))
                .thenReturn(Optional.of(decoration1));

        Optional<Decoration> foundDecoration = decorationService.getById(decoration1.getId());

        Assertions.assertTrue(foundDecoration.isPresent());
        Assertions.assertEquals(decoration1.getId(), foundDecoration.get().getId());
    }

    @Test
    void getById_WrongId_NotFound() {
        Mockito.when(decorationRepository.findById(1000L)).thenReturn(Optional.empty());

        Optional<Decoration> foundDecoration = decorationService.getById(1000L);

        Assertions.assertFalse(foundDecoration.isPresent());
    }

    @Test
    void getDecorations_RepoNotEmpty_ReturnAllDecorationsFound() {
        List<Decoration> decorations = Arrays.asList(decoration1, decoration2);

        Mockito.when(decorationRepository.findAll()).thenReturn(decorations);

        List<Decoration> allDecorations = decorationService.getDecorations();

        Assertions.assertEquals(2, allDecorations.size());
        Assertions.assertEquals(decoration1, allDecorations.getFirst());
    }

    @Test
    void getDecorations_RepoEmpty_ReturnEmpty() {
        List<Decoration> decorations = new ArrayList<>();

        Mockito.when(decorationRepository.findAll()).thenReturn(decorations);

        List<Decoration> allDecorations = decorationService.getDecorations();

        Assertions.assertTrue(allDecorations.isEmpty());
    }

    @Test
    void getDecorationsByCategory_TwoInRepoOneCorrect_ReturnOne() {
        List<Decoration> decorations = Arrays.asList(decoration1);
        Mockito.when(decorationRepository.findDecorationsByDecorationCategoryIs(DecorationCategory.FOUNTAIN))
                .thenReturn(decorations);

        List<Decoration> foundDecorations = decorationService.getDecorationsByCategory(DecorationCategory.FOUNTAIN);

        Assertions.assertEquals(1,foundDecorations.size());
        Assertions.assertEquals(decoration1, foundDecorations.getFirst());
    }

    @Test
    void getDecorationsByCategory_NoneWithThisCategory_ReturnEmptyList() {
        List<Decoration> decorations = new ArrayList<>();
        Mockito.when(decorationRepository.findDecorationsByDecorationCategoryIs(DecorationCategory.ROCK))
                .thenReturn(decorations);

        List<Decoration> foundDecorations = decorationService.getDecorationsByCategory(DecorationCategory.ROCK);

        Assertions.assertTrue(foundDecorations.isEmpty());
    }


    @Test
    void getDecorationsByGarden_CorrectGarden_ReturnDecorations() {
        List<Decoration> decorations = Arrays.asList(decoration1, decoration2);

        Mockito.when(decorationRepository.findDecorationsByGardenIs(garden))
                .thenReturn(decorations);

        List<Decoration> foundDecorations = decorationService.getDecorationsByGarden(garden);

        Assertions.assertFalse(foundDecorations.isEmpty());
        Assertions.assertEquals(decoration1, decorations.get(0));
        Assertions.assertEquals(decoration2, decorations.get(1));
    }

    @Test
    void getDecorationsByGarden_WrongGarden_ReturnEmptyList() {
        List<Decoration> decorations = new ArrayList<>();

        Garden wrongGarden = new Garden();

        Mockito.when(decorationRepository.findDecorationsByGardenIs(wrongGarden))
                .thenReturn(decorations);

        List<Decoration> foundDecorations = decorationService.getDecorationsByGarden(wrongGarden);

        Assertions.assertTrue(foundDecorations.isEmpty());
    }

    @Test
    void getDecorationsByGardenAndCategory_CorrectGardenCorrectCategory_ReturnDecoration() {
        List<Decoration> decorations = Arrays.asList(decoration1);

        Mockito.when(decorationRepository.findDecorationsByGardenIsAndDecorationCategoryIs(garden, DecorationCategory.FOUNTAIN))
                .thenReturn(decorations);

        List<Decoration> foundDecorations = decorationService.getDecorationsByGardenAndCategory(garden, DecorationCategory.FOUNTAIN);

        Assertions.assertEquals(1,foundDecorations.size());
        Assertions.assertEquals(decoration1, foundDecorations.getFirst());
    }

    @Test
    void getDecorationsByGardenAndCategory_CorrectGardenWrongCategory_ReturnEmptyList() {
        List<Decoration> decorations = new ArrayList<>();

        Mockito.when(decorationRepository.findDecorationsByGardenIsAndDecorationCategoryIs(garden, DecorationCategory.GNOME))
                .thenReturn(decorations);

        List<Decoration> foundDecorations = decorationService.getDecorationsByGardenAndCategory(garden, DecorationCategory.GNOME);

        Assertions.assertTrue(foundDecorations.isEmpty());
    }

    @Test
    void getDecorationsByGardenAndCategory_WrongGardenCorrectCategory_ReturnEmptyList() {
        List<Decoration> decorations = new ArrayList<>();

        Garden wrongGarden = new Garden();

        Mockito.when(decorationRepository.findDecorationsByGardenIsAndDecorationCategoryIs(wrongGarden, DecorationCategory.FOUNTAIN))
                .thenReturn(decorations);

        List<Decoration> foundDecorations = decorationService.getDecorationsByGardenAndCategory(wrongGarden, DecorationCategory.FOUNTAIN);

        Assertions.assertTrue(foundDecorations.isEmpty());
    }

    @Test
    void addDecoration_NewDecoration_Success() {
        Mockito.when(decorationRepository.findDecorationsByGardenIsAndDecorationCategoryIs(garden, DecorationCategory.FOUNTAIN))
                .thenReturn(Arrays.asList());

        Mockito.when(decorationRepository.save(decoration1)).thenReturn(decoration1);

        Decoration savedDecoration = decorationService.addDecoration(decoration1);

        Assertions.assertEquals(decoration1, savedDecoration);
        Assertions.assertEquals(decoration1.getDecorationCategory(), savedDecoration.getDecorationCategory());
        Assertions.assertEquals(decoration1.getGarden(), savedDecoration.getGarden());
    }

    @Test
    void testAddDecoration_AlreadyExists_ThrowException() {
        Mockito.when(decorationRepository.findDecorationsByGardenIsAndDecorationCategoryIs(garden, DecorationCategory.FOUNTAIN))
                .thenReturn(Arrays.asList(decoration1));

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> decorationService.addDecoration(decoration1));

        Assertions.assertEquals("Decoration already in garden", thrown.getMessage());
    }

}
