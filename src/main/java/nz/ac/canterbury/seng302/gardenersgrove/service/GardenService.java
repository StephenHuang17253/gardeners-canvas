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
    public List<Garden> getGardens() {
        return gardenRepository.findAll();
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
     */
    public Garden addGarden(Garden garden) {
        return gardenRepository.save(garden);
    }

    /**
     * Updates a garden
     * @param id the id of the existing garden
     * @param newGarden the new garden details
     */
    public Garden updateGarden(Long id, Garden newGarden) {
        Optional<Garden> targetGarden = findById(id);
        if (targetGarden.isPresent()) {
            Garden oldGarden = targetGarden.get();

            oldGarden.setGardenName(newGarden.getGardenName());
            oldGarden.setGardenAddress(newGarden.getGardenAddress());
            oldGarden.setGardenSuburb(newGarden.getGardenSuburb());
            oldGarden.setGardenCity(newGarden.getGardenCity());
            oldGarden.setGardenPostcode(newGarden.getGardenPostcode());
            oldGarden.setGardenCountry(newGarden.getGardenCountry());
            oldGarden.setGardenLocation(newGarden.getGardenLocation());
            oldGarden.setGardenSize(newGarden.getGardenSize());

            return gardenRepository.save(oldGarden);

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
        Optional<Garden> optionalGarden = findById(gardenId);

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
