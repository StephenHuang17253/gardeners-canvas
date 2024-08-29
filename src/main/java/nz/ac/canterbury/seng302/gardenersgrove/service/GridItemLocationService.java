package nz.ac.canterbury.seng302.gardenersgrove.service;

import jakarta.persistence.EntityNotFoundException;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GridItemLocation;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GridItemLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service class that manages GridItemLocation objects
 */
@Service
public class GridItemLocationService {

    /**
     * Interface for generic CRUD operations on a repository for GridItemLocation types.
     */
    private final GridItemLocationRepository gridItemLocationRepository;

    /**
     * GridItemLocationService constructor with repository.
     *
     * @param gridItemLocationRepository the repository for GridItemLocation objects
     */
    @Autowired
    public GridItemLocationService(GridItemLocationRepository gridItemLocationRepository) {
        this.gridItemLocationRepository = gridItemLocationRepository;
    }

    /**
     * Gets all Garden locations currently stored in the underlying repository
     * @return all locations in underlying repository as a List of locations
     */
    public List<GridItemLocation> getAllGridItemLocations() {return gridItemLocationRepository.findAll();}

    /**
     * Get GridItemLocation by garden
     * @param id of the GridItemLocation
     * @return list of GridItemLocations with that garden
     */
    public Optional<GridItemLocation> getGridItemLocationById(Long id) {
        return gridItemLocationRepository.findById(id);
    }

    /**
     * Get GridItemLocation by garden
     * @param garden in the relation
     * @return list of GridItemLocations with that garden
     */
    public List<GridItemLocation> getGridItemLocationByGarden(Garden garden) {
        return gridItemLocationRepository.findGridItemLocationByGardenIs(garden);
    }

    /**
     * Saves a GridItemLocation to the repository
     * @param gridItemLocation the GridItemLocation to save
     * @return the GridItemLocation to save
     */
    public GridItemLocation addGridItemLocation(GridItemLocation gridItemLocation) throws IllegalArgumentException {
        List<GridItemLocation> gridItemLocationList = getGridItemLocationByGarden(gridItemLocation.getGarden());

        List<GridItemLocation> overlappingLocation = gridItemLocationList.parallelStream()
                .filter(gridItem -> (gridItem.getXCoordinates() == gridItemLocation.getXCoordinates() &&
                        gridItem.getYCoordinates() == gridItemLocation.getYCoordinates())).toList();

        if (overlappingLocation.isEmpty()) {
            return gridItemLocationRepository.save(gridItemLocation);
        } else {
            throw new IllegalArgumentException("Item already exists at this location");
        }
    }

    /**
     * Updates a GridItemLocation in the repository
     * @param gridItemLocation the GridItemLocation to update
     * @return the GridItemLocation to update
     */
    public GridItemLocation updateGridItemLocation(GridItemLocation gridItemLocation) throws IllegalArgumentException, EntityNotFoundException {

        if (gridItemLocation.getId() == null) {
            throw new IllegalArgumentException("Grid item id cannot be null");
        }

        Optional<GridItemLocation> optionalGridItemLocation = gridItemLocationRepository.findById((gridItemLocation.getId()));
        if (optionalGridItemLocation.isEmpty()) {
            throw new EntityNotFoundException("Grid item not found");
        }

        List<GridItemLocation> gridItemLocationList = getGridItemLocationByGarden(gridItemLocation.getGarden());
        List<GridItemLocation> overlappingLocation = gridItemLocationList.parallelStream()
                .filter(gridItem -> (gridItem.getXCoordinates() == gridItemLocation.getXCoordinates() &&
                        gridItem.getYCoordinates() == gridItemLocation.getYCoordinates())).toList();
        if (overlappingLocation.isEmpty() || (overlappingLocation.size() == 1
                && Objects.equals(overlappingLocation.get(0).getId(), gridItemLocation.getId()))) {
            return gridItemLocationRepository.save(gridItemLocation);
        } else {
            throw new IllegalArgumentException("Item already exists at this location");
        }
    }

    /**
     * Remove a gridItemLocation from the repository
     * @param gridItemLocation the gridItemLocation to remove
     */
    public void removeGridItemLocation(GridItemLocation gridItemLocation)
    {
        gridItemLocationRepository.delete(gridItemLocation);
    }

}
