package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.UserInteraction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

/**
 * UserInteraction object repository accessor using Spring's @link{CrudRepository}.
 */
@Repository
public interface UserInteractionRepository extends CrudRepository<UserInteraction, Long> {

    /**
     * Finds a UserInteraction object by id
     * @param id the UserInteraction's id
     * @return none or a UserInteraction object
     */
    Optional<UserInteraction> findById(long id);

    /**
     * Find all UserInteraction objects in repo
     * @return list of UserInteraction objects
     */
    List<UserInteraction> findAll();
}
