package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Decoration;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.util.DecorationCategory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Decoration object repository accessor using Spring's @link{CrudRepository}.
 * These (basic) methods are provided for us without the need to write our own
 * implementations
 */
@Repository
public interface DecorationRepository extends CrudRepository<Decoration, Long> {

    /**
     * Finds a Decoration object by id
     *
     * @param id the decoration's id
     */
    Optional<Decoration> findById(long id);

    /**
     * Find all decoration objects belonging to a particular decoration category.
     *
     * @param decorationCategory the decoration category used in query
     * @return list of decoration objects with that category
     */
    List<Decoration> findDecorationsByDecorationCategoryIs(DecorationCategory decorationCategory);

    /**
     * Find all decoration objects belonging to a particular garden.
     *
     * @param garden the garden used in query
     * @return list of decoration objects belonging to that garden.
     */
    List<Decoration> findDecorationsByGardenIs(Garden garden);


    /**
     * Find all decoration objects belonging to a particular decoration category and garden.
     *
     * @param garden                the garden the decoration belongs to
     * @param decorationCategory    the category the decoration belongs to
     * @return list of decoration objects belonging to that garden and category.
     */
    List<Decoration> findDecorationsByGardenIsAndDecorationCategoryIs(Garden garden, DecorationCategory decorationCategory);

    /**
     * Find all Decoration objects in repo
     *
     * @return list of decoration objects
     */
    List<Decoration> findAll();
}
