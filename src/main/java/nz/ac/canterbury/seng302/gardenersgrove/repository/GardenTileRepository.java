package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTile;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTile;
import nz.ac.canterbury.seng302.gardenersgrove.util.GridItemType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * GardenTile Repository object repository accessor using
 * Spring's @link{CrudRepository}.
 */
@Repository
public interface GardenTileRepository extends CrudRepository<GardenTile, Long> {

    /**
     * Finds a GardenTile object by id
     *
     * @param id the GardenTile's id
     * @return none or a GardenTile object
     */
    Optional<GardenTile> findById(long id);

    /**
     * Finds a GardenTile object by Garden
     *
     * @param garden the Garden the grid belongs to
     * @return none or a list of GardenTile objects
     */
    List<GardenTile> findGardenTileByGardenIs(Garden garden);

    /**
     * Find all GardenItemLocation objects in repo
     *
     * @return list of GardenTile objects
     */
    List<GardenTile> findAll();

    /**
     * Finds a garden tile by garden and coordinates.
     *
     * @param garden the garden used in the query
     * @param xcoord the x coordinate used in the query
     * @param ycoord the y coordinate used in the query
     * @return the tile which belongs to that garden, at the given coordinates.
     */
    Optional<GardenTile> findGardenTileByGardenIsAndXCoordinateIsAndYCoordinateIs(Garden garden, int xcoord, int ycoord);


}
