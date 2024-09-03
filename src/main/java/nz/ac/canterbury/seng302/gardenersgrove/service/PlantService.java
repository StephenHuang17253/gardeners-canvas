package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.UserInteraction;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
import nz.ac.canterbury.seng302.gardenersgrove.util.PlantCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * Service class for Plant objects.
 */
@Service
public class PlantService {

    /**
     * Interface for generic CRUD operations on a repository for Plant types.
     */
    private final PlantRepository plantRepository;

    private final GardenService gardenService;

    private final FileService fileService;

    Logger logger = LoggerFactory.getLogger(PlantService.class);

    /**
     * PlantService constructor with repository and garden service
     *
     *
     * @param plantRepository the repository for Plants
     * @param gardenService   the needed garden service to link plants to gardens
     * @param gardenService   the needed garden service to link plants to gardens
     */
    @Autowired
    public PlantService(PlantRepository plantRepository, GardenService gardenService, FileService fileService) {
        this.plantRepository = plantRepository;
        this.gardenService = gardenService; // Initialize GardenService
        this.fileService = fileService;
    }

    /**
     * Retrieves all plants from persistence
     *
     * @return a list of all plant objects saved in persistence
     */
    public List<Plant> getPlants() {
        return plantRepository.findAll();
    }

    /**
     * Retrieves a plant by ID
     *
     * @param id the plants's ID
     * @return the plant or Optional#empty() if none found
     */
    public Optional<Plant> findById(long id) {
        return plantRepository.findById(id);
    }

    /**
     * Retrieves all plants belonging to a particular plant category
     * 
     * @param plantCategory the plant category used in the query
     * @return a list of plants belonging to that category
     */
    public List<Plant> getPlantsByCategory(PlantCategory plantCategory) {
        return plantRepository.findPlantsByPlantCategoryIs(plantCategory);
    }

    /**
     * Retrieves all plant objects belonging to a particular plant category and
     * garden.
     * 
     * @param garden        the garden the plant belongs to
     * @param plantCategory the category the plant belongs to
     * @return list of plant objects belonging to that garden and category.
     */
    public List<Plant> getPlantsByGardenAndCategory(Garden garden, PlantCategory plantCategory) {
        return plantRepository.findPlantsByGardenIsAndPlantCategoryIs(garden, plantCategory);
    }

    /**
     * Adds a new plant
     *
     * @param plantName        plant's name
     * @param plantCount       count of plants
     * @param plantDescription plant's description
     * @param plantDate        date of planting
     * @param gardenId         id of garden the plant belongs to
     * @param plantCategory    category the plant belongs to
     * @throws IllegalArgumentException if invalid garden ID
     */
    public Plant addPlant(String plantName, int plantCount, String plantDescription, LocalDate plantDate, Long gardenId,
            PlantCategory plantCategory) {
        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);
        if (optionalGarden.isPresent()) {
            Plant plant = new Plant(plantName, plantCount, plantDescription, plantDate, optionalGarden.get(),
                    plantCategory);
            gardenService.addPlantToGarden(gardenId, plant);
            return plantRepository.save(plant);
        } else {
            throw new IllegalArgumentException("Invalid garden ID");
        }
    }

    /**
     * Updates a plant
     *
     * @param id       the id of the existing plant
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
            oldPlant.setPlantCategory(newPlant.getPlantCategory());
            return plantRepository.save(oldPlant);
        } else {
            throw new IllegalArgumentException("Invalid plant IDD");
        }
    }

    /**
     * Updates a plant, this version does not need a garden object passed in
     *
     * @param id       the id of the existing plant
     * @param newName  new name of plant
     * @param newCount new number of plants
     * @param newDesc  new plant description
     * @param newDate  new plant date
     */
    public Plant updatePlant(Long id, String newName, int newCount, String newDesc, LocalDate newDate, PlantCategory newPlantCategory) {
        Optional<Plant> targetPlant = findById(id);
        if (targetPlant.isPresent()) {
            Plant oldPlant = targetPlant.get();

            oldPlant.setPlantName(newName);
            oldPlant.setPlantCount(newCount);
            oldPlant.setPlantDescription(newDesc);
            oldPlant.setPlantDate(newDate);
            oldPlant.setPlantCategory(newPlantCategory);
            return plantRepository.save(oldPlant);
        } else {
            throw new IllegalArgumentException("Invalid plant ID");
        }
    }

    /**
     * Update a plant's picture filename
     *
     * @param filename filename of plant picture
     * @param id       id of plant to update
     */
    public void updatePlantPictureFilename(String filename, long id) {
        Optional<Plant> targetPlant = findById(id);
        if (targetPlant.isPresent()) {
            Plant plant = targetPlant.get();
            plant.setPlantPictureFilename(filename);
            plantRepository.save(plant);
        } else {
            throw new IllegalArgumentException("Invalid plant id");
        }

    }

    /**
     * Update the plant's picture
     *
     * @param plant        plant to update
     * @param plantPicture new plant picture
     */
    public void updatePlantPicture(Plant plant, MultipartFile plantPicture) {
        String fileExtension = Objects.requireNonNull(plantPicture.getOriginalFilename()).split("\\.")[1];
        try {
            String[] allFiles = fileService.getAllFiles();
            // Delete past plant image/s
            for (String file : allFiles) {
                if (file.contains("plant_" + plant.getPlantId() + "_plant_picture")) {
                    fileService.deleteFile(file);
                }
            }

            String fileName = "plant_" + plant.getPlantId() + "_picture." + fileExtension.toLowerCase();
            updatePlantPictureFilename(fileName, plant.getPlantId());
            fileService.saveFile(fileName, plantPicture);

        } catch (IOException error) {
            logger.error(error.getMessage());
        }
    }

    /**
     * Returns all plants that the user interacted with
     *
     * @param userInteractions list of recent user interactions
     * @return list of plants
     */
    public List<Plant> getPlantsByInteraction(List<UserInteraction> userInteractions) {
        return userInteractions.stream()
                .map(userInteraction -> findById(userInteraction.getItemId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    /**
     * Copy the plant's picture
     * Overloaded method
     *
     * @param plant                plant to update
     * @param plantPictureFileName plant picture to copy
     */
    public void updatePlantPicture(Plant plant, String plantPictureFileName) {
        String fileExtension = Objects.requireNonNull(plantPictureFileName).split("\\.")[1];
        try {
            String[] allFiles = fileService.getAllFiles();
            // Delete past plant image/s
            for (String file : allFiles) {
                if (file.contains("plant_" + plant.getPlantId() + "_plant_picture")) {
                    fileService.deleteFile(file);
                }
            }

            String newFileName = "plant_" + plant.getPlantId() + "_picture." + fileExtension.toLowerCase();
            updatePlantPictureFilename(newFileName, plant.getPlantId());
            fileService.saveFile(newFileName, plantPictureFileName);

        } catch (IOException error) {
            logger.error(error.getMessage());
        }
    }

    /**
     * Deletes plant and its associated picture
     *
     * @param plantId id of plant to delete
     * @throws IOException exception cannot delete plant
     */
    public void deletePlant(Long plantId) throws IOException {
        Optional<Plant> targetPlant = findById(plantId);
        if (targetPlant.isPresent()) {
            Plant plantToDelete = targetPlant.get();
            if (plantToDelete.getPlantPictureFilename() != null) {
                fileService.deleteFile(plantToDelete.getPlantPictureFilename());
            }
            Garden garden = plantToDelete.getGarden();
            garden.getPlants().remove(plantToDelete);
            plantRepository.deleteById(plantId);
        }
    }

    public List<PlantCategory> getPlantCategories() {
        return Arrays.asList(PlantCategory.values());
    }
}
