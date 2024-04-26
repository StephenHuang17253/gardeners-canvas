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

    @Autowired
    private UserService userService;

    /**
     * Interface for generic CRUD operations on a repository for Garden types.
     */
    private GardenRepository gardenRepository;

    /**
     * GardenService constructor with repository.
     * @param gardenRepository the repository for Gardens
     */
    @Autowired
    public GardenService(GardenRepository gardenRepository) {
        this.gardenRepository = gardenRepository;
    }

    /**
     * Retrieves all gardens from persistence
     * @return a list of all garden objects saved in persistence
     */
    //might be irrlevant for this sprint
    public List<Garden> getGardens() {
        return gardenRepository.findAll();
    }
    /**
     * Retrieves all gardens from persistence where the owner id matches the inputted id
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
     * @param id the garden's ID
     * @return the garden or Optional#empty() if none found
     */
    public Optional<Garden> findById(long id) {
        return gardenRepository.findById(id);
    }

    /**
     * Adds a new garden
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
     * Updates a garden
     * @param id the id of the existing garden
     * @param newGardenValues the new garden values
     */
    public Garden updateGarden(Long id, Garden newGardenValues) {
        Optional<Garden> optionalGarden = getGardenById(id);
        if (optionalGarden.isPresent()) {
            Garden targetGarden = optionalGarden.get();

            oldGarden.setGardenName(newGarden.getGardenName());
            oldGarden.setGardenAddress(newGarden.getGardenAddress());
            oldGarden.setGardenSuburb(newGarden.getGardenSuburb());
            oldGarden.setGardenCity(newGarden.getGardenCity());
            oldGarden.setGardenPostcode(newGarden.getGardenPostcode());
            oldGarden.setGardenCountry(newGarden.getGardenCountry());
            oldGarden.setGardenLocation(newGarden.getGardenLocation());
            oldGarden.setGardenSize(newGarden.getGardenSize());
            targetGarden.setGardenName(newGardenValues.getGardenName());
            targetGarden.setGardenLocation(newGardenValues.getGardenLocation());
            targetGarden.setGardenSize(newGardenValues.getGardenSize());

            return gardenRepository.save(targetGarden);

        } else {
            throw new IllegalArgumentException("Invalid garden ID");
        }
    }
    /**
     * Adds plant entity to garden entity plant list
     * @param gardenId the id of the garden we wish to add plant to
     * @param plant the new plant to be added to the garden
     * @throws IllegalArgumentException if invalid garden ID
     */
    public void addPlantToGarden(Long gardenId, Plant plant) {
        Optional<Garden> optionalGarden = getGardenById(gardenId);

        if (optionalGarden.isPresent()) {
            Garden garden = optionalGarden.get();
            //add plant to the garden's list
            garden.getPlants().add(plant);
            //since list is updated save changes to repo
            gardenRepository.save(garden);
        } else {
            throw new IllegalArgumentException("Invalid garden ID");
        }
    }
}
