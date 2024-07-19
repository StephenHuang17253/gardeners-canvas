package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.UserInteraction;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendshipRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserInteractionRepository;
import nz.ac.canterbury.seng302.gardenersgrove.util.ItemType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserInteractionService {

    private final UserInteractionRepository userInteractionRepository;

    private final UserService userService;

    /**
     * UserInteractionService constructor
     * @param userInteractionRepository the repository for UserInteractions
     * @param userService the service for the user repository
     */
    @Autowired
    public UserInteractionService(UserInteractionRepository userInteractionRepository, UserService userService) {
        this.userInteractionRepository = userInteractionRepository;
        this.userService = userService;
    }

    /**
     * Retrieves a UserInteraction by ID
     * @param id the UserInteraction's ID
     * @return the UserInteraction or Optional.empty() if none found
     */
    public Optional<UserInteraction> getUserInteractionById(long id){
        return null;
    }

    /**
     * Retrieves all a users UserInteractions from persistence
     * @param id the user's ID
     * @return a list of UserInteraction objects
     */
    public List<UserInteraction> getAllUsersUserInteractions(long id) {
        return null;
    }

    /**
     * Add a UserInteraction for a given user
     * @param user to add the UserInteraction for
     * @return the UserInteraction
     */
    public UserInteraction addUserInteraction(User user, Long itemId, ItemType itemType){
        return null;
    }

    /**
     * Delete a UserInteraction
     * @param id the UserInteraction's id
     */
    public void deleteUserInteracton(Long id){
    }
}
