package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.util.PlantCategory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Plant object repository accessor using Spring's @link{CrudRepository}.
 * These (basic) methods are provided for us without the need to write our own implementations
 */
@Repository
public interface PlantRepository extends CrudRepository<Plant, Long> {

    /**
     * Finds a Plant object by id
     * @param id the plant's id
     */
    Optional<Plant> findById(long id);

    /**
     * Find all plant objects belonging to a particular plant category.
     * @param plantCategory the plant category used in query
     * @return list of plant objects with that category
     */
    List<Plant> findPlantsByPlantCategoryIs(PlantCategory plantCategory);

    /**
     * Find all plant objects belonging to a particular plant category and garden.
     * @param garden the garden the plant belongs to
     * @param plantCategory the category the plant belongs to
     * @return list of plant objects belonging to that garden and category.
     */
    List<Plant> findPlantsByGardenIsAndPlantCategoryIs(Garden garden, PlantCategory plantCategory);

    /**
     * Find all Plant objects in repo
     * @return list of plant objects
     */
    List<Plant> findAll();
}
