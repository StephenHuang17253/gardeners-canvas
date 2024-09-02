package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.UserInteraction;
import nz.ac.canterbury.seng302.gardenersgrove.util.ItemType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserInteraction object repository accessor using
 * Spring's @link{CrudRepository}.
 */
@Repository
public interface UserInteractionRepository extends CrudRepository<UserInteraction, Long> {

    /**
     * Finds a UserInteraction object by id
     * 
     * @param id the UserInteraction's id
     * @return none or a UserInteraction object
     */
    Optional<UserInteraction> findById(long id);

    /**
     * Find all UserInteraction objects for a given user
     * 
     * @param userId the id to match the user objects id
     * @return list of UserInteraction objects
     */
    List<UserInteraction> findByUserId(long userId);

    /**
     * Find all UserInteraction objects for a given user of a given item type
     * 
     * @param userId the id to match the user objects id
     * @return list of UserInteraction objects
     */
    List<UserInteraction> findByUserIdAndItemTypeOrderByInteractionTimeDesc(long userId, ItemType itemType);

    Optional<UserInteraction> findByUserIdAndItemIdAndItemType(Long userId, Long itemId, ItemType itemType);
}
