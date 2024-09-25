package nz.ac.canterbury.seng302.gardenersgrove.integration;


import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTile;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.*;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenTileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.util.TileTexture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class GardenTileServiceIntegrationTest {

    @Autowired
    GardenTileRepository gardenTileRepository;

    @Autowired
    GardenTileService gardenTileService;

    @Autowired
    GardenService gardenService;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GardenRepository gardenRepository;

    @Autowired
    GridItemLocationRepository gridItemLocationRepository;

    @Autowired
    TokenRepository tokenRepository;

    User testUser1;

    Garden garden;

    Garden otherGarden;


    GardenTile tile1;

    GardenTile tile2;

    GardenTile tile3;

    @BeforeEach
    void before() {

        gridItemLocationRepository.deleteAll();
        gardenTileRepository.deleteAll();
        gardenRepository.deleteAll();
        tokenRepository.deleteAll();
        userRepository.deleteAll();


        testUser1 = userService.addUser(new User("Alexandar", "au Arcos", "alexandar@DecorationServiceUnitTest.com",
                LocalDate.of(2003,5,2)),"password");
        garden = gardenService.addGarden(new Garden("Arcos Gardens",
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

        otherGarden = gardenService.addGarden(new Garden("Arcos Gardens",
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


        tile1 = new GardenTile(garden, TileTexture.STONE_PATH, 3, 3);
        tile1.setTileId(1L);

        tile2 = new GardenTile(garden, TileTexture.CONCRETE, 3, 2);
        tile2.setTileId(2L);

        tile3 = new GardenTile(otherGarden, TileTexture.GRASS, 1, 2);
        tile3.setTileId(3L);
    }

    @Test
    void getById_CorrectId_ReturnTile() {
        gardenTileService.addGardenTile(tile1);
        Optional<GardenTile> optionalTile = gardenTileService.getById(tile1.getTileId());

        Assertions.assertTrue(optionalTile.isPresent());
        Assertions.assertEquals(tile1.getTileId(), optionalTile.get().getTileId());
    }

    @Test
    void getById_WrongId_NotFound() {
        gardenTileService.addGardenTile(tile1);
        gardenTileService.addGardenTile(tile2);

        Optional<GardenTile> optionalTile = gardenTileService.getById(1000L);

        Assertions.assertTrue(optionalTile.isEmpty());
    }

    @Test
    void getAllTiles_RepoNotEmpty_ReturnAllTiles() {
        tile1 = gardenTileService.addGardenTile(tile1);
        tile2 = gardenTileService.addGardenTile(tile2);

        List<GardenTile> allTiles = gardenTileService.getAllTiles();

        Assertions.assertEquals(2, allTiles.size());
        Assertions.assertEquals(tile1.getTileId(), allTiles.get(0).getTileId());
    }

    @Test
    void getAllTiles_RepoEmpty_ReturnEmptyList() {

        List<GardenTile> allTiles = gardenTileService.getAllTiles();

        Assertions.assertTrue(allTiles.isEmpty());
    }

    @Test
    void getGardenTilesByGarden_CorrectGarden_ReturnTiles() {
        tile1 = gardenTileService.addGardenTile(tile1);
        tile2 = gardenTileService.addGardenTile(tile2);
        List<GardenTile> tilesInGarden = gardenTileService.getGardenTilesByGarden(garden);

        Assertions.assertFalse(tilesInGarden.isEmpty());
        Assertions.assertEquals(tile1.getTileId(), tilesInGarden.get(0).getTileId());
        Assertions.assertEquals(tile2.getTileId(), tilesInGarden.get(1).getTileId());

    }

    @Test
    void getGardenTilesByGarden_WrongGarden_ReturnEmptyList() {

        gardenTileService.addGardenTile(tile1);
        gardenTileService.addGardenTile(tile2);

        User wrongUser = userService.addUser(new User("John","Doe","x@y.z", LocalDate.now()),"asdasd");
        Garden wrongGarden = gardenService.addGarden(new Garden("Arcos Gardens",
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
                wrongUser));

        List<GardenTile> tilesInGarden = gardenTileService.getGardenTilesByGarden(wrongGarden);

        Assertions.assertTrue(tilesInGarden.isEmpty());
    }

    @Test
    void getGardenTileByGardenAndCoordinates_CorrectGardenCorrectCoords_ReturnTile() {

        tile1 = gardenTileService.addGardenTile(tile1);
        tile2 = gardenTileService.addGardenTile(tile2);
        tile3 = gardenTileService.addGardenTile(tile3);

        Optional<GardenTile> optionalTile = gardenTileService.getGardenTileByGardenAndCoordinates(garden,3,3);

        Assertions.assertTrue(optionalTile.isPresent());
        Assertions.assertEquals(tile1.getTileId(), optionalTile.get().getTileId());
    }

    @Test
    void getGardenTileByGardenAndCoordinates_CorrectGardenWrongCoords_TileNotFound() {

        gardenTileService.addGardenTile(tile1);
        gardenTileService.addGardenTile(tile2);
        gardenTileService.addGardenTile(tile3);

        Optional<GardenTile> optionalTile = gardenTileService.getGardenTileByGardenAndCoordinates(garden,0,0);

        Assertions.assertTrue(optionalTile.isEmpty());
    }

    @Test
    void getGardenTileByGardenAndCoordinates_WrongGardenCorrectCoords_TileNotFound() {
        User wrongUser = userService.addUser(new User("John","Doe","x@y.z", LocalDate.now()),"asdasd");
        Garden wrongGarden = gardenService.addGarden(new Garden("Arcos Gardens",
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
                wrongUser));

        gardenTileService.addGardenTile(tile1);
        gardenTileService.addGardenTile(tile2);
        gardenTileService.addGardenTile(tile3);

        Optional<GardenTile> optionalTile = gardenTileService.getGardenTileByGardenAndCoordinates(wrongGarden,3,3);

        Assertions.assertTrue(optionalTile.isEmpty());
    }

    @Test
    void getGardenTileByGardenAndCoordinates_WrongGardenWrongCoords_TileNotFound() {
        User wrongUser = userService.addUser(new User("John","Doe","x@y.z", LocalDate.now()),"asdasd");
        Garden wrongGarden = gardenService.addGarden(new Garden("Arcos Gardens",
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
                wrongUser));

        gardenTileService.addGardenTile(tile1);
        gardenTileService.addGardenTile(tile2);
        gardenTileService.addGardenTile(tile3);

        Optional<GardenTile> optionalTile = gardenTileService.getGardenTileByGardenAndCoordinates(wrongGarden,0,0);

        Assertions.assertTrue(optionalTile.isEmpty());
    }

    @Test
    void addGardenTile_NoOverlap_Success() {
        gardenTileService.addGardenTile(tile1);
        gardenTileService.addGardenTile(tile2);

        GardenTile newGardenTile = new GardenTile(garden, TileTexture.STONE_PATH, 6, 6);

        GardenTile persistedTile = gardenTileService.addGardenTile(newGardenTile);



        Assertions.assertEquals(newGardenTile, persistedTile);
        Assertions.assertEquals(newGardenTile.getXCoordinate(), persistedTile.getXCoordinate());
        Assertions.assertEquals(newGardenTile.getYCoordinate(), persistedTile.getYCoordinate());
        Assertions.assertEquals(newGardenTile.getGarden(), persistedTile.getGarden());
    }

    @Test
    void addGardenTile_Overlap_DeleteOverlapAddNew() {
        gardenTileService.addGardenTile(tile1);
        gardenTileService.addGardenTile(tile2);

        GardenTile newGardenTile = new GardenTile(garden, TileTexture.STONE_PATH, 3, 3);
        newGardenTile.setTileId(999L);


        gardenTileService.addGardenTile(newGardenTile);


        Optional<GardenTile> optionalTile = gardenTileService.getGardenTileByGardenAndCoordinates(garden, 3, 3);

        Assertions.assertTrue(optionalTile.isPresent());
        Assertions.assertEquals(newGardenTile.getTileId(),optionalTile.get().getTileId());
        Assertions.assertEquals(newGardenTile.getTileTexture(),optionalTile.get().getTileTexture());
    }


}
