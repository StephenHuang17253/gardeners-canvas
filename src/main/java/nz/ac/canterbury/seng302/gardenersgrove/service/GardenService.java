package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;

/**
 * Service class for Garden objects.
 */
@Service
public class GardenService {

    private UserService userService;

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
    //might be irrlevant for this sprint
    public List<Garden> getGardens() {
        return gardenRepository.findAll();
    }

    /**
     * Retrieves all gardens from persistence where the owner id matches the inputted id
     *
     * @param id the user's ID
     * @throws IllegalArgumentException if the provided user ID is invalid
     */
    public List<Garden> getAllUsersGardens(long id) {
        if (userService.getUserById(id) != null) {
            return gardenRepository.findByOwnerId(id);
        } else {
            throw new IllegalArgumentException("Invalid user ID: " + id);
        }
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
     * @throws IllegalArgumentException if the user associated with the garden is not in the db
     */
    public Garden addGarden(Garden garden) {
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
    public Garden updateGarden(Long id, Garden newGardenValues) {
        Optional<Garden> optionalGarden = getGardenById(id);
        if (optionalGarden.isPresent()) {
            Garden targetGarden = optionalGarden.get();

            targetGarden.setGardenName(newGardenValues.getGardenName());
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
            throw new IllegalArgumentException("Invalid garden ID");
        }
    }

    /**
     * Adds plant entity to garden entity plant list
     *
     * @param gardenId the id of the garden we wish to add plant to
     * @param plant    the new plant to be added to the garden
     * @throws IllegalArgumentException if invalid garden ID
     */
    public void addPlantToGarden(Long gardenId, Plant plant) {
        Optional<Garden> optionalGarden = getGardenById(gardenId);

        if (optionalGarden.isPresent()) {
            Garden garden = optionalGarden.get();
            garden.getPlants().add(plant);
            gardenRepository.save(garden);
        } else {
            throw new IllegalArgumentException("Invalid garden ID");
        }
    }

    /**
     * Prepares searchValue for Like and IgnoreCase query.
     * Finds all gardens whose name includes searchValue, or whose plantNames include the search value
     *
     * @param searchValue string input to match
     * @return List of Garden Objects
     */
    public List<Garden> getMatchingGardens(String searchValue) {
        searchValue = searchValue.toLowerCase();
        searchValue = "%" + searchValue + "%";
        return gardenRepository.findByGardenNameOrPlantNameContainingIgnoreCase(searchValue);
    }

    public List<Garden> getAllPublicGardens() {
        return gardenRepository.findAllPublicGardens();
    }


}
