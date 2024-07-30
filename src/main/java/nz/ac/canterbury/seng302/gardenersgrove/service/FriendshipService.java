package nz.ac.canterbury.seng302.gardenersgrove.service;

import jakarta.persistence.EntityNotFoundException;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service class for Friendship objects.
 */
@Service
public class FriendshipService {
    private final UserService userService;

    /**
     * Interface for generic CRUD operations on a repository for Friendship types.
     */
    private final FriendshipRepository friendshipRepository;

    /**
     * FriendshipService constructor with repository.
     *
     * @param friendshipRepository the repository for Gardens
     */
    @Autowired
    public FriendshipService(FriendshipRepository friendshipRepository, UserService userService) {
        this.friendshipRepository = friendshipRepository;
        this.userService = userService;
    }

    /**
     * Retrieves a Friendship by ID
     *
     * @param id the Friendship's ID
     * @return the Friendship or Optional.empty() if none found
     */
    public Optional<Friendship> getFriendShipById(long id) {
        return friendshipRepository.findById(id);
    }

    /**
     * Checks if a friendship exists between two users
     *
     * @param user1 one half of the friendship relation
     * @param user2 other half of the friendship relation
     * @return boolean true or false
     * @throws IllegalArgumentException if the provided users are not valid
     */
    public boolean checkFriendshipExists(User user1, User user2) {
        validateUsers(user1, user2);

        Optional<Friendship> optionalFriendshipUser1IsSender = friendshipRepository
                .findByUser1IdAndUser2Id(user1.getId(), user2.getId());
        Optional<Friendship> optionalFriendshipUser2IsSender = friendshipRepository
                .findByUser1IdAndUser2Id(user2.getId(), user1.getId());

        return !(optionalFriendshipUser1IsSender.isEmpty() && optionalFriendshipUser2IsSender.isEmpty());
    }

    /**
     * Returns a friendship object or null
     *
     * @param user1 a user
     * @param user2 a user
     * @return Friendship object or null
     */
    public Friendship findFriendship(User user1, User user2) {
        validateUsers(user1, user2);

        Optional<Friendship> optionalFriendshipUser1IsSender = friendshipRepository
                .findByUser1IdAndUser2Id(user1.getId(), user2.getId());
        Optional<Friendship> optionalFriendshipUser2IsSender = friendshipRepository
                .findByUser1IdAndUser2Id(user2.getId(), user1.getId());

        if (optionalFriendshipUser1IsSender.isPresent()) {
            return optionalFriendshipUser1IsSender.get();
        }
        if (optionalFriendshipUser2IsSender.isPresent()) {
            return optionalFriendshipUser2IsSender.get();
        }

        return null;
    }

    /**
     * Checks the friendship status betweeen two users
     *
     * @param user1 one half of the friendship relation
     * @param user2 other half of the friendship relation
     * @return status of friendship
     */
    public FriendshipStatus checkFriendshipStatus(User user1, User user2) {
        validateUsers(user1, user2);

        Optional<Friendship> optionalFriendshipUser1IsSender = friendshipRepository
                .findByUser1IdAndUser2Id(user1.getId(), user2.getId());
        Optional<Friendship> optionalFriendshipUser2IsSender = friendshipRepository
                .findByUser1IdAndUser2Id(user2.getId(), user1.getId());

        if (optionalFriendshipUser1IsSender.isPresent()) {
            return optionalFriendshipUser1IsSender.get().getStatus();
        }
        if (optionalFriendshipUser2IsSender.isPresent()) {
            return optionalFriendshipUser2IsSender.get().getStatus();
        }

        return null;
    }

    /**
     * Retrieves all Friendships from persistence where user1 or user2 id matches
     *
     * @param id the user's ID
     * @throws IllegalArgumentException if the provided user ID is invalid
     */
    public List<Friendship> getAllUsersFriends(long id) throws IllegalArgumentException {
        if (userService.getUserById(id) != null) {
            return friendshipRepository.findByUser1IdOrUser2Id(id, id);
        } else {
            throw new IllegalArgumentException("Invalid user ID: " + id);
        }
    }

    /**
     * Helper function to validate the given users before creating a friendship.
     *
     * @param user1 the first user
     * @param user2 the second user
     * @throws IllegalArgumentException if user IDs are null or refer to the same
     *                                  user
     */
    private void validateUsers(User user1, User user2) throws IllegalArgumentException {
        if (user1.getId() == null || user2.getId() == null) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (Objects.equals(user1.getId(), user2.getId())) {
            throw new IllegalArgumentException("User cannot have a friendship relation with themselves");
        }
        if (userService.getUserById(user1.getId()) == null || userService.getUserById(user2.getId()) == null) {
            throw new IllegalArgumentException("User not found");
        }
    }

    /**
     * Adds a new pending friendship entity to persistence
     * IMPORTANT: cannot add a new friendship relation between two users if one
     * already exists
     * EXCEPTION: When a users friend request gets declined, the recipient may send
     * a request in return
     * IN THAT CASE: the old relation gets updated to pending instead of declined
     * and the order of users gets switched around
     *
     * @param user1 the sender of the friendship relation
     * @param user2 the receiver of the friendship relation
     * @throws IllegalArgumentException if user prams arnt valid or trying to re-add
     *                                  friendship relation (see above)
     */
    public Friendship addFriendship(User user1, User user2) throws IllegalArgumentException {
        validateUsers(user1, user2);

        Optional<Friendship> optionalFriendshipUser1IsSender = friendshipRepository
                .findByUser1IdAndUser2Id(user1.getId(), user2.getId());
        Optional<Friendship> optionalFriendshipUser2IsSender = friendshipRepository
                .findByUser1IdAndUser2Id(user2.getId(), user1.getId());

        if (optionalFriendshipUser1IsSender.isPresent()) {
            Friendship existingFriendship = optionalFriendshipUser1IsSender.get();
            Long existingUser1Id = existingFriendship.getUser1().getId();
            Long existingUser2Id = existingFriendship.getUser2().getId();

            if (Objects.equals(existingUser1Id, user1.getId()) && Objects.equals(existingUser2Id, user2.getId())) {
                throw new IllegalArgumentException("Cant re-add a Friendship relation when one already exists");
            }
        } else if (optionalFriendshipUser2IsSender.isPresent()) {
            Friendship existingFriendship = optionalFriendshipUser2IsSender.get();
            Long existingUser1Id = existingFriendship.getUser1().getId();
            Long existingUser2Id = existingFriendship.getUser2().getId();

            if (existingFriendship.getStatus().equals(FriendshipStatus.DECLINED)) {
                existingFriendship.setUser1(user1);
                existingFriendship.setUser2(user2);
                existingFriendship.setStatus(FriendshipStatus.PENDING);

                return friendshipRepository.save(existingFriendship);
            } else if (Objects.equals(existingUser1Id, user2.getId())
                    && Objects.equals(existingUser2Id, user1.getId())) {
                throw new IllegalArgumentException("Cant re-add a Friendship relation when one already exists");
            }
        }
        Friendship friendship = new Friendship(user1, user2, FriendshipStatus.PENDING);
        return friendshipRepository.save(friendship);
    }

    /**
     * Updates a friendship status of an existing friendship
     *
     * @param id               the id of the existing friendship
     * @param friendshipStatus the status to update it to
     * @throws IllegalArgumentException if id invalid or trying to update a declined
     *                                  relation
     */
    public Friendship updateFriendShipStatus(Long id, FriendshipStatus friendshipStatus)
            throws IllegalArgumentException {
        Optional<Friendship> optionalFriend = friendshipRepository.findById(id);
        if (optionalFriend.isEmpty()) {
            throw new IllegalArgumentException("Invalid Friendship ID: " + id);
        } else if (optionalFriend.get().getStatus() == FriendshipStatus.DECLINED) {
            throw new IllegalArgumentException("Cant update a Friendship relation's status if its declined");
        } else {
            Friendship friendship = optionalFriend.get();
            friendship.setStatus(friendshipStatus);
            return friendshipRepository.save(friendship);
        }
    }

    /**
     * Deletes a friendship by the id of the friendship
     *
     * @param friendshipId the id of the friendship to delete
     * @throws EntityNotFoundException thrown if the friendship does not exist
     */
    public void deleteFriendship(Long friendshipId) throws EntityNotFoundException {
        Optional<Friendship> friendshipToDeleteOptional = friendshipRepository.findById(friendshipId);
        if (friendshipToDeleteOptional.isEmpty()) {
            throw new EntityNotFoundException("No such friendship");
        }
        friendshipRepository.deleteById(friendshipId);
    }

}
