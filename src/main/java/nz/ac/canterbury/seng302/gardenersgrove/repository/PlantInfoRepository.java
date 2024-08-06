package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.PlantInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Plant Info object repository accessor using Spring's @link{CrudRepository}.
 */
@Repository
public interface PlantInfoRepository extends CrudRepository<PlantInfo, Long> {

    /**
     * Finds a Plant Info object by id
     * @param id of the Plant Info Entity
     */
    Optional<PlantInfo> findById(long id);

    /**
     * Find all Plant Info objects in repo
     * @return list of Plant Info Entity objects
     */
    List<PlantInfo> findAll();
}
