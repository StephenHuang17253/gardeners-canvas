package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTag;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTagRelation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
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
     *
     * @param id the GardenTag's id
     * @return none or a GardenTag object
     */
    Optional<GardenTagRelation> findById(long id);

    /**
     * Finds a GardenTagRelation object by GardenTag
     *
     * @param tag the GardenTag in the relation
     * @return none or a list of GardenTagRelation objects
     */
    List<GardenTagRelation> findGardenTagRelationsByTagIs(GardenTag tag);

    /**
     * Finds a GardenTagRelation object by Garden
     *
     * @param garden the Garden in the relation
     * @return none or a list of GardenTagRelation objects
     */
    List<GardenTagRelation> findGardenTagRelationsByGardenIs(Garden garden);

    /**
     * Finds a GardenTagRelation object by Garden and GardenTag
     *
     * @param garden the Garden the relation
     * @param tag    the gardenTag in the relation
     * @return none or a GardenTagRelation object
     */
    Optional<GardenTagRelation> findGardenTagRelationsByGardenIsAndTagIs(Garden garden, GardenTag tag);

    /**
     * Finds all matching gardens that meet the following criteria:
     * The garden name or the name of any plant in the garden contains <i>search string</i>
     * And at least one tag applied to the garden has a garden name that
     * matches at least one of the tag names in tag string
     * @param searchValue Garden name to match
     * @param tagsString Tag names to match, should be a string representing a list of comma separate values
     *                   e.g "<b>tag1,tag2,tag3</b>". should not contain brackets
     * @return A list of garden type objects
     */
    @Query("SELECT DISTINCT g FROM Garden g " +
       "JOIN GardenTagRelation gtr ON g.gardenId = gtr.garden.gardenId " +
       "JOIN GardenTag t ON gtr.tag.tagId = t.tagId " +
       "JOIN Plant p ON g.gardenId = p.garden.gardenId " +
       "WHERE g.isPublic = true " +
       "AND (LOWER(p.plantName) LIKE LOWER(CONCAT('%', :searchValue, '%')) " +
       "OR LOWER(g.gardenName) LIKE LOWER(CONCAT('%', :searchValue, '%'))) " +
       "AND t.tagName IN (:tagsString)")
    List<Garden> findByGardenNameContainsAndGardenHasTags(@Param("searchValue") String searchValue,
            @Param("tagsString") String tagsString);

    /**
     * Find all GardenTagRelation objects in repo
     *
     * @return list of GardenTagRelation objects
     */
    List<GardenTagRelation> findAll();
}
