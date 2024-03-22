package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import org.springframework.data.repository.CrudRepository;
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
     * @param id the garden's id
     */
    Optional<Garden> findById(long id);

    /**
     * Find all Garden objects in repo
     * @return list of garden objects
     */
    List<Garden> findAll();
}
