package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Friendship object repository accessor using Spring's @link{CrudRepository}.
 */
@Repository
public interface FriendshipRepository extends CrudRepository<Friendship, Long> {

    /**
     * Finds a Friendship object by id
     * 
     * @param id the Friendship's id
     * @return none or a Friendship object
     */
    Optional<Friendship> findById(long id);

    /**
     * Find all Friendship objects in repo
     * 
     * @return list of Friendship objects
     */
    List<Friendship> findAll();

    /**
     * Find all Friendship objects where user1_id or user2_id matches userId
     * 
     * @param userId1 the id to match the user1 column
     * @param userId2 the id to match the user2 column
     * @return list of all Friendship objects matching the requirements
     */
    List<Friendship> findByUser1IdOrUser2Id(long userId1, long userId2);

    /**
     * Find Friendship object where user1_id and user2_id matches the input params
     * 
     * @param userId1 the id to match the user1 column
     * @param userId2 the id to match the user2 column
     * @return list of all Friendship objects matching the requirements
     */
    Optional<Friendship> findByUser1IdAndUser2Id(long userId1, long userId2);

}
