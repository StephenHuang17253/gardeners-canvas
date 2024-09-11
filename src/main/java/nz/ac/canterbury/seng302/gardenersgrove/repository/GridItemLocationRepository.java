package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GridItemLocation;
import nz.ac.canterbury.seng302.gardenersgrove.util.GridItemType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * GridItemLocation Repository object repository accessor using
 * Spring's @link{CrudRepository}.
 */
@Repository
public interface GridItemLocationRepository extends CrudRepository<GridItemLocation, Long> {

    /**
     * Finds a GridItemLocation object by id
     *
     * @param id the GridItemLocation's id
     * @return none or a GridItemLocation object
     */
    Optional<GridItemLocation> findById(long id);

    /**
     * Finds a GridItemLocation object by Garden
     *
     * @param garden the Garden the grid belongs to
     * @return none or a list of GridItemLocation objects
     */
    List<GridItemLocation> findGridItemLocationByGardenIs(Garden garden);

    /**
     * Find all GardenItemLocation objects in repo
     *
     * @return list of GridItemLocation objects
     */
    List<GridItemLocation> findAll();

    /**
     * Finds GridItemLocation matching given details
     * Note: This function is only being used in testing at the moment
     *
     * @param objectId unique id of plant or decoration entity in persistence
     * @param itemType plant or decoration
     * @param garden   garden containing object
     * @return matching item or null
     */
    Optional<GridItemLocation> findGridItemLocationByObjectIdAndItemTypeAndGarden(Long objectId, GridItemType itemType, Garden garden);


}
