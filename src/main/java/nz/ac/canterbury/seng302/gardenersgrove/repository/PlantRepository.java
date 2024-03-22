package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
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
     * Find all Plant objects in repo
     * @return list of plant objects
     */
    List<Plant> findAll();
}
