package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTag;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTagRelation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Garden Tag object repository accessor using Spring's @link{CrudRepository}.
 */
@Repository
public interface GardenTagRelationRepository extends CrudRepository<GardenTagRelation, Long> {

    /**
     * Finds a GardenTag object by id
     * @param id the GardenTag's id
     * @return none or a GardenTag object
     */
    Optional<GardenTagRelation> findById(long id);

    /**
     * Finds a GardenTagRelation object by GardenTag
     * @param tag the GardenTag in the relation
     * @return none or a list of GardenTagRelation objects
     */
    List<GardenTagRelation> findGardenTagRelationsByTagIs(GardenTag tag);

    /**
     * Finds a GardenTagRelation object by Garden
     * @param garden the Garden in the relation
     * @return none or a list of GardenTagRelation objects
     */
    List<GardenTagRelation> findGardenTagRelationsByGardenIs(Garden garden);

    /**
     * Finds a GardenTagRelation object by Garden and GardenTag
     * @param garden the Garden the relation
     * @param tag the gardenTag in the relation
     * @return none or a GardenTagRelation object
     */
    Optional<GardenTagRelation> findGardenTagRelationsByGardenIsAndTagIs(Garden garden, GardenTag tag);

    /**
     * Find all GardenTagRelation objects in repo
     * @return list of GardenTagRelation objects
     */
    List<GardenTagRelation> findAll();



}
