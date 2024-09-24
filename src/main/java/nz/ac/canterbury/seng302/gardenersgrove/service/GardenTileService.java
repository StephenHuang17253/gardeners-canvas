package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTile;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenTileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service class for Garden Tile objects
 */
@Service
public class GardenTileService {

    private final GardenTileRepository gardenTileRepository;

    /**
     * GardenTileService constructor with repository
     * @param gardenTileRepository
     */
    @Autowired
    public GardenTileService(GardenTileRepository gardenTileRepository) {
        this.gardenTileRepository = gardenTileRepository;
    }

    /**
     * Retrieves a garden tile from the repository by its id
     * @param tileId the id of the garden tile
     * @return an optional containing the garden tile if it exists
     */
    public Optional<GardenTile> getById(Long tileId) {
        return gardenTileRepository.findByTileId(tileId);
    }

    /**
     * Retrieves all garden tiles from the repository
     * @return list of garden tiles in the repository
     */
    public List<GardenTile> getAllTiles() {
        return gardenTileRepository.findAll();
    }

    /**
     * Retrieves all garden tiles associated with a particular garden.
     * @return list of garden tiles in that garden
     */
    public List<GardenTile> getGardenTilesByGarden(Garden garden) {
        return gardenTileRepository.findGardenTilesByGardenIs(garden);
    }

    /**
     * Retrieves a garden tile by its garden and coordinates
     * @param garden the garden the tile belongs to
     * @param xCoord the x coordinate of the tile
     * @param yCoord the y coordinate of the tile
     * @return an optional containing the garden tile if it exists
     */
    public Optional<GardenTile> getGardenTileByGardenAndCoordinates(Garden garden, int xCoord, int yCoord) {
        return gardenTileRepository.findTileByGardenAndCoordinates(garden, xCoord, yCoord);
    }

    /**
     * Saves/updates a garden tile to the repository
     * @param gardenTile the garden tile being persisted
     * @return the garden tile
     */
    public GardenTile persistGardenTile(GardenTile gardenTile) {
        Optional<GardenTile> overlappingTile = getGardenTileByGardenAndCoordinates(gardenTile.getGarden(), gardenTile.getXCoordinate(), gardenTile.getYCoordinate());

        if (overlappingTile.isEmpty() || Objects.equals(overlappingTile.get().getTileId(), gardenTile.getTileId())) {
            return gardenTileRepository.save(gardenTile);
        } else {
            throw new IllegalArgumentException("Tile already exists at this location");
        }
    }

    /**
     * Deletes a garden tile from the repository
     * @param gardenTile the garden tile being deleted
     */
    public void deleteGardenTile(GardenTile gardenTile) {
        gardenTileRepository.delete(gardenTile);
    }


}
