package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for Friendship objects.
 */
@Service
public class FriendshipService {
    private UserService userService;
    /**
     * Interface for generic CRUD operations on a repository for Garden types.
     */
    private FriendshipRepository friendshipRepository;
    /**
     * FriendshipService constructor with repository.
     * @param friendshipRepository the repository for Gardens
     */
    @Autowired
    public FriendshipService(FriendshipRepository friendshipRepository, UserService userService) {
        this.friendshipRepository = friendshipRepository;
        this.userService = userService;
    }
    /**
     * Retrieves a Friendship by ID
     * @param id the Friendship's ID
     * @return the Friendship or Optional.empty() if none found
     */
    public Optional<Friendship> getFriendShipById(long id){
        return null;

    }
    /**
     * Retrieves all Friendship from persistence
     * @return a list of all Friendship objects saved in persistence
     */
    public List<Friendship> getAllFriendship() {
        return null;
    }
    /**
     * Retrieves all Friendships from persistence where user1 or user2 id matches
     * @param id the user's ID
     */
    public List<Garden> getAllUsersGardens(long id) {
        return null;
    }
}
