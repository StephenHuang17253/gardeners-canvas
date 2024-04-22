package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Plant objects.
 */
@Service
public class PlantService {

    /**
     * Interface for generic CRUD operations on a repository for Plant types.
     */
    private PlantRepository plantRepository;

    private GardenService gardenService;

    /**
     * PlantService constructor with repository and garden service
     * @param plantRepository the repository for Plants
     * @param gardenService the needed garden service to link plants to gardens
     */
    @Autowired
    public PlantService(PlantRepository plantRepository, GardenService gardenService) {
        this.plantRepository = plantRepository;
        this.gardenService = gardenService; // Initialize GardenService
    }

    /**
     * Retrieves all plants from persistence
     * @return a list of all plant objects saved in persistence
     */
    public List<Plant> getPlants() {
        return plantRepository.findAll();
    }

    /**
     * Retrieves a plant by ID
     * @param id the plants's ID
     * @return the plant or Optional#empty() if none found
     */
    public Optional<Plant> findById(long id) {return plantRepository.findById(id);}

    /**
     * Adds a new plant
     * @param plantName plant's name
     * @param plantCount count of plants
     * @param plantDescription plant's description
     * @param plantDate date of planting
     * @param gardenId id of garden the plant belongs to
     * @throws IllegalArgumentException if invalid garden ID
     */

    public Plant addPlant(String plantName, float plantCount, String plantDescription, LocalDate plantDate, Long gardenId) {
        Optional<Garden> optionalGarden = gardenService.findById(gardenId);
        if (optionalGarden.isPresent()) {
            Plant plant = new Plant(plantName, plantCount, plantDescription, plantDate, optionalGarden.get());
            gardenService.addPlantToGarden(gardenId, plant);
            return plantRepository.save(plant);
        } else {
            throw new IllegalArgumentException("Invalid garden ID");
        }
    }

    /**
     * Updates a plant
     * @param id the id of the existing plant
     * @param newPlant the new plant details
     */
    public Plant updatePlant(Long id, Plant newPlant) {
        Optional<Plant> targetPlant = findById(id);
        if (targetPlant.isPresent()) {
            Plant oldPlant = targetPlant.get();

            oldPlant.setPlantName(newPlant.getPlantName());
            oldPlant.setPlantCount(newPlant.getPlantCount());
            oldPlant.setPlantDescription(newPlant.getPlantDescription());
            oldPlant.setPlantDate(newPlant.getPlantDate());
            return plantRepository.save(oldPlant);
        } else {
            throw new IllegalArgumentException("Invalid plant IDD");
        }
    }

    /**
     * Updates a plant, this version does not need a garden object passed in
     * @param id the id of the existing plant
     * @param newName new name of plant
     * @param newCount new number of plants
     * @param newDesc new plant description
     * @param newDate new plant date
     */
    public Plant updatePlant(Long id, String newName, Float newCount, String newDesc, LocalDate newDate) {
        Optional<Plant> targetPlant = findById(id);
        if (targetPlant.isPresent()) {
            Plant oldPlant = targetPlant.get();

            oldPlant.setPlantName(newName);
            oldPlant.setPlantCount(newCount);
            oldPlant.setPlantDescription(newDesc);
            oldPlant.setPlantDate(newDate);
            return plantRepository.save(oldPlant);
        } else {
            throw new IllegalArgumentException("Invalid plant IDD");
        }
    }

}
