package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.UserInteraction;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

/**
 * Service class for Garden objects.
 */
@Service
public class GardenService {

    private UserService userService;

    private String invalidGardenId = "Invalid garden ID";

    /**
     * Interface for generic CRUD operations on a repository for Garden types.
     */
    private GardenRepository gardenRepository;

    /**
     * GardenService constructor with repository.
     *
     * @param gardenRepository the repository for Gardens
     */
    @Autowired
    public GardenService(GardenRepository gardenRepository, UserService userService) {
        this.gardenRepository = gardenRepository;
        this.userService = userService;
    }

    /**
     * Retrieves all gardens from persistence
     *
     * @return a list of all garden objects saved in persistence
     */
    public List<Garden> getGardens() {
        return gardenRepository.findAll();
    }

    /**
     * Retrieves all gardens from persistence where the owner id matches the
     * inputted id
     *
     * @param id the user's ID
     * @throws IllegalArgumentException if the provided user ID is invalid
     */
    public List<Garden> getAllUsersGardens(long id) throws IllegalArgumentException {
        if (userService.getUserById(id) == null) {
            throw new IllegalArgumentException("Invalid user ID: " + id);
        }
        return gardenRepository.findByOwnerId(id);
    }

    /**
     * Retrieves a garden by ID
     *
     * @param id the garden's ID
     * @return the garden or Optional#empty() if none found
     */
    public Optional<Garden> getGardenById(long id) {
        return gardenRepository.findById(id);
    }

    /**
     * Adds a new garden
     *
     * @param garden the garden to add
     * @throws IllegalArgumentException if the user associated with the garden is
     *                                  not in the db
     */
    public Garden addGarden(Garden garden) throws IllegalArgumentException {
        if (garden.getOwner().getId() != null && userService.getUserById(garden.getOwner().getId()) != null) {
            userService.addGardenToGardenList(garden, garden.getOwner().getId());
            return gardenRepository.save(garden);
        } else {
            throw new IllegalArgumentException("User " + garden.getOwner().getFirstName()
                    + " " + garden.getOwner().getLastName()
                    + " does not exist");
        }
    }

    /**
     * Updates a garden values
     *
     * @param id              the id of the existing garden
     * @param newGardenValues the new garden values
     * @throws IllegalArgumentException if invalid garden id
     */
    public Garden updateGarden(Long id, Garden newGardenValues) throws IllegalArgumentException {
        Optional<Garden> optionalGarden = getGardenById(id);
        if (optionalGarden.isPresent()) {
            Garden targetGarden = optionalGarden.get();

            targetGarden.setGardenName(newGardenValues.getGardenName());
            targetGarden.setGardenDescription(newGardenValues.getGardenDescription());
            targetGarden.setGardenAddress(newGardenValues.getGardenAddress());
            targetGarden.setGardenSuburb(newGardenValues.getGardenSuburb());
            targetGarden.setGardenCity(newGardenValues.getGardenCity());
            targetGarden.setGardenPostcode(newGardenValues.getGardenPostcode());
            targetGarden.setGardenCountry(newGardenValues.getGardenCountry());
            targetGarden.setGardenSize(newGardenValues.getGardenSize());
            targetGarden.setGardenLongitude(newGardenValues.getGardenLongitude());
            targetGarden.setGardenLatitude(newGardenValues.getGardenLatitude());
            return gardenRepository.save(targetGarden);

        } else {
            throw new IllegalArgumentException(invalidGardenId);
        }
    }

    /**
     * Updates the status of the garden's publicity (isPublic)
     * 
     * @param id           - the garden's id
     * @param publicStatus - whether isPublic should be made true or false
     * @return the updated garden as saved in the repository
     */
    public Garden updateGardenPublicity(Long id, boolean publicStatus) {
        Optional<Garden> optionalGarden = getGardenById(id);
        if (optionalGarden.isPresent()) {
            Garden targetGarden = optionalGarden.get();

            targetGarden.setIsPublic(publicStatus);

            return gardenRepository.save(targetGarden);

        } else {
            throw new IllegalArgumentException(invalidGardenId);
        }
    }

    /**
     * Updates the watering need status of the garden
     * 
     * @param gardenId      the garden's id
     * @param needsWatering the boolean attribute used to determine if a garden
     *                      needs watering
     * @return the updated garden as saved in the repository
     */
    public Garden changeGardenNeedsWatering(Long gardenId, boolean needsWatering) {
        Optional<Garden> optionalGarden = getGardenById(gardenId);

        if (optionalGarden.isPresent()) {
            Garden targetGarden = optionalGarden.get();

            targetGarden.setNeedsWatering(needsWatering);

            return gardenRepository.save(targetGarden);

        } else {
            throw new IllegalArgumentException(invalidGardenId);
        }
    }

    /**
     * Updates the longitude and latitude of a Garden entity.
     * 
     * @param id        the id of the garden we wish to update the coordinates of
     * @param latitude  the new latitude
     * @param longitude the new longitude
     */
    public Garden updateGardenCoordinates(Long id, String latitude, String longitude) {
        Optional<Garden> optionalGarden = getGardenById(id);
        if (optionalGarden.isPresent()) {
            Garden targetGarden = optionalGarden.get();

            targetGarden.setGardenLatitude(latitude);
            targetGarden.setGardenLongitude(longitude);
            targetGarden.setLastLocationUpdate(LocalDateTime.now());

            return gardenRepository.save(targetGarden);

        } else {
            throw new IllegalArgumentException(invalidGardenId);
        }
    }

    /**
     * Adds plant entity to garden entity plant list
     *
     * @param gardenId the id of the garden we wish to add plant to
     * @param plant    the new plant to be added to the garden
     * @throws IllegalArgumentException if invalid garden ID
     */
    public void addPlantToGarden(Long gardenId, Plant plant) throws IllegalArgumentException {
        Optional<Garden> optionalGarden = getGardenById(gardenId);

        if (optionalGarden.isPresent()) {
            Garden garden = optionalGarden.get();
            garden.getPlants().add(plant);
            gardenRepository.save(garden);
        } else {
            throw new IllegalArgumentException(invalidGardenId);
        }
    }

    /**
     * Prepares searchValue for Like and IgnoreCase query.
     * Finds all gardens whose name includes searchValue, or whose plantNames
     * include the search value
     *
     * @param searchValue string input to match
     * @return List of Garden Objects
     */
    public List<Garden> getMatchingGardens(String searchValue) {
        return gardenRepository.findByGardenNameOrPlantNameContainingIgnoreCase(searchValue);
    }

    /**
     * Retrieves all public gardens from persistence
     * 
     * @return a list of all public garden objects saved in persistence
     */
    public List<Garden> getAllPublicGardens() {
        return gardenRepository.findAllPublicGardens();
    }

    /**
     * Retrieves all gardens from persistence that have been interacted with by the
     * user
     * 
     * @param userInteractions the user interactions
     * @return a list of all garden objects saved in persistence
     */
    public List<Garden> getGardensByInteraction(List<UserInteraction> userInteractions) {
        return userInteractions.stream()
                .map(userInteraction -> getGardenById(userInteraction.getItemId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

}
