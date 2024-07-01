package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Garden object repository accessor using Spring's @link{CrudRepository}.
 * These (basic) methods are provided for us without the need to write our own implementations
 */
@Repository
public interface GardenRepository extends CrudRepository<Garden, Long> {

    /**
     * Finds a Garden object by id
     *
     * @param id the garden's id
     * @return none or a garden object
     */
    Optional<Garden> findById(long id);

    /**
     * Find all Garden objects in repo
     *
     * @return list of garden objects
     */
    List<Garden> findAll();

    /**
     * Find all Garden objects in repo where owner id matches userId
     *
     * @param userId the garden's id
     * @return list of garden objects
     */
    List<Garden> findByOwnerId(long userId);

    /**
     * Find all gardens whose name or one of their plant names includes searchValue
     *
     * @param searchValue string to be included in garden or plant name
     * @return list of garden objects
     */
    @Query("SELECT DISTINCT garden FROM Garden garden LEFT JOIN garden.plants plant " +
            "WHERE garden.isPublic = true AND (LOWER(garden.gardenName) LIKE LOWER(:searchValue) " +
            "OR LOWER(plant.plantName) LIKE LOWER(:searchValue))")
    List<Garden> findByGardenNameOrPlantNameContainingIgnoreCase(@Param("searchValue") String searchValue);

    /**
     * Returns all public gardens
     *
     * @return list of garden objects
     */
    @Query("SELECT DISTINCT garden FROM Garden garden WHERE (garden.isPublic) = true")
    List<Garden> findAllPublicGardens();


}
