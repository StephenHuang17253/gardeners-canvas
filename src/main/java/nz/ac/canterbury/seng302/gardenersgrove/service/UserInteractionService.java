package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.UserInteraction;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserInteractionRepository;
import nz.ac.canterbury.seng302.gardenersgrove.util.ItemType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserInteractionService {

    private final UserInteractionRepository userInteractionRepository;

    private final UserService userService;

    private final GardenService gardenService;

    private final PlantService plantService;

    private static final int MAX_INTERACTION_COUNT_PER_TYPE = 10;

    /**
     * UserInteractionService constructor
     * @param userInteractionRepository the repository for UserInteractions
     * @param userService the service for the user repository
     */
    @Autowired
    public UserInteractionService(UserInteractionRepository userInteractionRepository,
                                  UserService userService,
                                  GardenService gardenService,
                                  PlantService plantService) {
        this.userInteractionRepository = userInteractionRepository;
        this.userService = userService;
        this.gardenService = gardenService;
        this.plantService = plantService;
    }

    /**
     * Retrieves a UserInteraction by ID
     * @param id the UserInteraction's ID
     * @return the UserInteraction or Optional.empty() if none found
     */
    public Optional<UserInteraction> getUserInteractionById(long id){
        return userInteractionRepository.findById(id);
    }

    /**
     * Retrieves all a users UserInteractions from persistence
     * @param id the user's ID
     * @return a list of UserInteraction objects
     */
    public List<UserInteraction> getAllUsersUserInteractions(long id) throws IllegalArgumentException {
        if (userService.getUserById(id) != null) {
            return userInteractionRepository.findByUserId(id);
        } else {
            throw new IllegalArgumentException("Invalid user ID: " + id);
        }
    }

    /**
     * Retrieves all a users UserInteractions from persistence of the given itemType
     * @param id the user's ID
     * @return a list of UserInteraction objects
     */
    public List<UserInteraction> getAllUsersUserInteractionsByItemType(long id, ItemType itemType) throws IllegalArgumentException {
        if (userService.getUserById(id) == null) {
            throw new IllegalArgumentException("Invalid user ID: " + id);
        }

        return userInteractionRepository.findByUserIdAndItemTypeOrderByInteractionTime(id, itemType);
    }

    /**
     * Helper method to check item exits in the repo
     * @param itemId id of the item
     * @param itemType type of the item
     * @return if valid or not
     */
    private boolean isValidItem(Long itemId, ItemType itemType){
        return switch (itemType) {
            case GARDEN -> gardenService.getGardenById(itemId).isPresent();
            case PLANT -> plantService.findById(itemId).isPresent();
            case USER -> userService.getUserById(itemId) != null;
        };
    }

    /**
     * Add a UserInteraction for a given user
     * @param userId id of the user that the interaction belongs to
     * @param itemId the id of item the user interacted with
     * @param itemType the type of item the user interacted with
     * @param interactionTime the time of the user interaction
     * @return the UserInteraction
     */
    public UserInteraction addUserInteraction(Long userId, Long itemId, ItemType itemType, LocalDateTime interactionTime){
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Invalid user ID: " + userId);
        }

        if (!isValidItem(itemId, itemType)) {
            throw new IllegalArgumentException("Invalid item ID: " + itemId + " for item type: " + itemType);
        }

        if (interactionTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Interaction Time: " + interactionTime + " cannot be in the future");
        }

        Optional<UserInteraction> existingInteractionOpt = userInteractionRepository.findByUserIdAndItemIdAndItemType(userId, itemId, itemType);
        if (existingInteractionOpt.isPresent()) {
            UserInteraction existingInteraction = existingInteractionOpt.get();
            existingInteraction.setInteractionTime(interactionTime);
            return userInteractionRepository.save(existingInteraction);
        }

        UserInteraction userInteraction = new UserInteraction(user, itemId, itemType, interactionTime);

        List<UserInteraction> userInteractions = getAllUsersUserInteractionsByItemType(userId, itemType);

        if(userInteractions.size() == MAX_INTERACTION_COUNT_PER_TYPE){
            userInteractionRepository.delete(userInteractions.get(userInteractions.size()-1));
        }

        return userInteractionRepository.save(userInteraction);
    }
}
