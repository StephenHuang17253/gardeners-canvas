package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTile;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenTileRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenTileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.util.TileTexture;
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

class GardenTileServiceUnitTest {

    @Mock
    GardenTileRepository gardenTileRepository;

    static GardenTileService gardenTileService;

    @Mock
    UserService userService;

    User testUser1;

    Garden garden;

    GardenTile tile1;

    GardenTile tile2;

    @BeforeEach
    void before() {
        userService = Mockito.mock(UserService.class);
        gardenTileRepository = Mockito.mock(GardenTileRepository.class);
        gardenTileService = new GardenTileService(gardenTileRepository);

        testUser1 = Mockito.spy(new User("Alexandar", "au Arcos", "alexandar@DecorationServiceUnitTest.com",
                LocalDate.of(2003, 5, 2)));
        garden = Mockito.spy(new Garden("Arcos Gardens",
                "The Gardens of House Arcos",
                "Elysium, Mars, Sol",
                "Elysium",
                "Mars",
                "",
                "Sol",
                50.0,
                false,
                "83",
                "43",
                testUser1));

        Mockito.when(testUser1.getId()).thenReturn(1L);
        Mockito.when(userService.getUserById(1L)).thenReturn(testUser1);

        tile1 = new GardenTile(garden, TileTexture.BARK, 3, 3);
        tile1.setTileId(1L);

        tile2 = new GardenTile(garden, TileTexture.CONCRETE, 3, 3);
        tile2.setTileId(2L);
    }

    @Test
    void getById_CorrectId_ReturnTile() {
        Mockito.when(gardenTileRepository.findByTileId(tile1.getTileId()))
                .thenReturn(Optional.of(tile1));

        Optional<GardenTile> optionalTile = gardenTileService.getById(tile1.getTileId());

        Assertions.assertTrue(optionalTile.isPresent());
        Assertions.assertEquals(tile1.getTileId(), optionalTile.get().getTileId());
    }

    @Test
    void getById_WrongId_NotFound() {
        Mockito.when(gardenTileRepository.findByTileId(1000L)).thenReturn(Optional.empty());

        Optional<GardenTile> optionalTile = gardenTileService.getById(1000L);

        Assertions.assertTrue(optionalTile.isEmpty());
    }

    @Test
    void getAllTiles_RepoNotEmpty_ReturnAllTiles() {
        List<GardenTile> tiles = Arrays.asList(tile1, tile2);

        Mockito.when(gardenTileRepository.findAll()).thenReturn(tiles);

        List<GardenTile> allTiles = gardenTileService.getAllTiles();

        Assertions.assertEquals(2, allTiles.size());
        Assertions.assertEquals(tile1, allTiles.get(0));
    }

    @Test
    void getAllTiles_RepoEmpty_ReturnEmptyList() {
        List<GardenTile> tiles = new ArrayList<>();

        Mockito.when(gardenTileRepository.findAll()).thenReturn(tiles);

        List<GardenTile> allTiles = gardenTileService.getAllTiles();

        Assertions.assertTrue(allTiles.isEmpty());
    }

    @Test
    void getGardenTilesByGarden_CorrectGarden_ReturnTiles() {
        List<GardenTile> tiles = Arrays.asList(tile1, tile2);

        Mockito.when(gardenTileRepository.findGardenTilesByGardenIs(garden))
                .thenReturn(tiles);

        List<GardenTile> tilesInGarden = gardenTileService.getGardenTilesByGarden(garden);

        Assertions.assertFalse(tilesInGarden.isEmpty());
        Assertions.assertEquals(tile1, tilesInGarden.get(0));
        Assertions.assertEquals(tile2, tilesInGarden.get(1));
    }

    @Test
    void getGardenTilesByGarden_WrongGarden_ReturnEmptyList() {
        List<GardenTile> tiles = new ArrayList<>();

        Garden wrongGarden = new Garden();

        Mockito.when(gardenTileRepository.findGardenTilesByGardenIs(wrongGarden))
                .thenReturn(tiles);

        List<GardenTile> tilesInGarden = gardenTileService.getGardenTilesByGarden(wrongGarden);

        Assertions.assertTrue(tilesInGarden.isEmpty());
    }

    @Test
    void getGardenTileByGardenAndCoordinates_CorrectGardenCorrectCoords_ReturnTile() {
        Mockito.when(gardenTileRepository.findTileByGardenAndCoordinates(garden, 3, 3))
                .thenReturn(Optional.ofNullable(tile1));

        Optional<GardenTile> optionalTile = gardenTileService.getGardenTileByGardenAndCoordinates(garden, 3, 3);

        Assertions.assertTrue(optionalTile.isPresent());
        Assertions.assertEquals(tile1, optionalTile.get());
    }

    @Test
    void getGardenTileByGardenAndCoordinates_CorrectGardenWrongCoords_TileNotFound() {
        Mockito.when(gardenTileRepository.findTileByGardenAndCoordinates(garden, 3, 3))
                .thenReturn(Optional.empty());

        Optional<GardenTile> optionalTile = gardenTileService.getGardenTileByGardenAndCoordinates(garden, 0, 0);

        Assertions.assertTrue(optionalTile.isEmpty());
    }

    @Test
    void getGardenTileByGardenAndCoordinates_WrongGardenCorrectCoords_TileNotFound() {
        Garden wrongGarden = new Garden();

        Mockito.when(gardenTileRepository.findTileByGardenAndCoordinates(wrongGarden, 3, 3))
                .thenReturn(Optional.empty());

        Optional<GardenTile> optionalTile = gardenTileService.getGardenTileByGardenAndCoordinates(wrongGarden, 3, 3);

        Assertions.assertTrue(optionalTile.isEmpty());
    }

    @Test
    void getGardenTileByGardenAndCoordinates_WrongGardenWrongCoords_TileNotFound() {
        Garden wrongGarden = new Garden();

        Mockito.when(gardenTileRepository.findTileByGardenAndCoordinates(wrongGarden, 3, 3))
                .thenReturn(Optional.empty());

        Optional<GardenTile> optionalTile = gardenTileService.getGardenTileByGardenAndCoordinates(wrongGarden, 0, 0);

        Assertions.assertTrue(optionalTile.isEmpty());
    }

    @Test
    void addGardenTile_NoOverlap_Success() {

        Mockito.when(gardenTileRepository.findTileByGardenAndCoordinates(garden, 6, 6))
                .thenReturn(Optional.empty());

        GardenTile newGardenTile = new GardenTile(garden, TileTexture.BARK, 6, 6);

        Mockito.when(gardenTileRepository.save(newGardenTile)).thenReturn(newGardenTile);

        GardenTile persistedTile = gardenTileService.addGardenTile(newGardenTile);

        Assertions.assertEquals(newGardenTile, persistedTile);
        Assertions.assertEquals(newGardenTile.getXCoordinate(), persistedTile.getXCoordinate());
        Assertions.assertEquals(newGardenTile.getYCoordinate(), persistedTile.getYCoordinate());
        Assertions.assertEquals(newGardenTile.getGarden(), persistedTile.getGarden());
    }

    @Test
    void addGardenTile_Overlap_DeleteOverlapAddNew() {
        GardenTile newGardenTile = new GardenTile(garden, TileTexture.BARK, 3, 3);
        newGardenTile.setTileId(999L);

        Mockito.when(gardenTileRepository.findTileByGardenAndCoordinates(garden, 3, 3))
                .thenReturn(Optional.of(tile1));

        gardenTileService.addGardenTile(newGardenTile);

        Mockito.when(gardenTileRepository.findTileByGardenAndCoordinates(garden, 3, 3))
                .thenReturn(Optional.of(newGardenTile));

        Optional<GardenTile> optionalTile = gardenTileService.getGardenTileByGardenAndCoordinates(garden, 3, 3);

        Assertions.assertTrue(optionalTile.isPresent());
        Assertions.assertEquals(newGardenTile.getTileId(), optionalTile.get().getTileId());
        Assertions.assertEquals(newGardenTile.getTileTexture(), optionalTile.get().getTileTexture());
    }

}
