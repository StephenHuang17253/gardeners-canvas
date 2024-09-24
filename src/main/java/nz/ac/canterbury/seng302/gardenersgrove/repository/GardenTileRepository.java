package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
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
    Optional<GardenTile> findByTileId(long id);

    /**
     * Finds the GardenTile objects belonging to a particular Garden
     *
     * @param garden the Garden the tiles are associated with
     * @return none or a list of GardenTile objects
     */
    List<GardenTile> findGardenTilesByGardenIs(Garden garden);

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
     * @param xCoordinate the x coordinate used in the query
     * @param yCoordinate the y coordinate used in the query
     * @return the tile which belongs to that garden, at the given coordinates.
     */
    @Query("SELECT gt FROM GardenTile gt WHERE gt.garden = :garden AND gt.xCoordinate = :xCoordinate AND gt.yCoordinate = :yCoordinate")
    Optional<GardenTile> findTileByGardenAndCoordinates(@Param("garden") Garden garden, @Param("xCoordinate") int xCoordinate, @Param("yCoordinate") int yCoordinate);


}
