package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTag;
import nz.ac.canterbury.seng302.gardenersgrove.util.TagStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Garden Tag object repository accessor using Spring's @link{CrudRepository}.
 */
@Repository
public interface GardenTagRepository extends CrudRepository<GardenTag, Long> {

    /**
     * Finds a GardenTag object by id
     * @param id the GardenTag's id
     * @return none or a GardenTag object
     */
    Optional<GardenTag> findById(long id);

    /**
     * Finds a GardenTag object by name
     * @param name of the GardenTag object
     * @return none or a GardenTag object
     */
    Optional<GardenTag> findByTagNameIs(String name);

    /**
     * Finds garden objects whose name contains a specific string (ignores case)
     * @param name string to match in each gardenTag object
     * @return none or a GardenTag object
     */
    List<GardenTag> findByTagNameContainsIgnoreCaseAndTagStatus(String name, TagStatus tagStatus);

    /**
     * Finds garden objects whose name matches to a string in any case
     * @param name string to match in each gardenTag object
     * @return none or a GardenTag object
     */
    List<GardenTag> findByTagNameIgnoreCase(String name);

    /**
     * Find all GardenTag objects in repo
     * @return list of GardenTag objects
     */
    List<GardenTag> findAll();


}
