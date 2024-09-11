package nz.ac.canterbury.seng302.gardenersgrove.service;

import jakarta.persistence.EntityNotFoundException;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GridItemLocation;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GridItemLocationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.util.GridItemType;
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
     * Interface for generic CRUD operations on a repository for GridItemLocation
     * types.
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
     * Gets all Garden tags currently stored in the underlying repository
     *
     * @return all tags in underlying repository as a List of tags
     */
    public List<GridItemLocation> getAllGridItemLocations() {
        return gridItemLocationRepository.findAll();
    }

    /**
     * Get GridItemLocation by garden
     *
     * @param id of the GridItemLocation
     * @return list of GridItemLocations with that garden
     */
    public Optional<GridItemLocation> getGridItemLocationById(Long id) {
        return gridItemLocationRepository.findById(id);
    }

    /**
     * Get GridItemLocation by garden
     *
     * @param garden in the relation
     * @return list of GridItemLocations with that garden
     */
    public List<GridItemLocation> getGridItemLocationByGarden(Garden garden) {
        return gridItemLocationRepository.findGridItemLocationByGardenIs(garden);
    }

    /**
     * Saves a GridItemLocation to the repository
     *
     * @param gridItemLocation the GridItemLocation to save
     * @return the GridItemLocation to save
     */
    public GridItemLocation addGridItemLocation(GridItemLocation gridItemLocation) throws IllegalArgumentException {
        List<GridItemLocation> gridItemLocationList = getGridItemLocationByGarden(gridItemLocation.getGarden());

        List<GridItemLocation> overlappingLocation = gridItemLocationList.parallelStream()
                .filter(gridItem -> (gridItem.getXCoordinate() == gridItemLocation.getXCoordinate() &&
                        gridItem.getYCoordinate() == gridItemLocation.getYCoordinate()))
                .toList();

        if (overlappingLocation.isEmpty()) {
            return gridItemLocationRepository.save(gridItemLocation);
        } else {
            throw new IllegalArgumentException("Item already exists at this location");
        }
    }

    /**
     * Updates a GridItemLocation in the repository
     *
     * @param gridItemLocation the GridItemLocation to update
     * @return the GridItemLocation to update
     */
    public GridItemLocation updateGridItemLocation(GridItemLocation gridItemLocation)
            throws IllegalArgumentException, EntityNotFoundException {

        if (gridItemLocation.getId() == null) {
            throw new IllegalArgumentException("Grid item id cannot be null");
        }

        Optional<GridItemLocation> optionalGridItemLocation = gridItemLocationRepository
                .findById((gridItemLocation.getId()));
        if (optionalGridItemLocation.isEmpty()) {
            throw new EntityNotFoundException("Grid item not found");
        }

        List<GridItemLocation> gridItemLocationList = getGridItemLocationByGarden(gridItemLocation.getGarden());
        List<GridItemLocation> overlappingLocation = gridItemLocationList.parallelStream()
                .filter(gridItem -> (gridItem.getXCoordinate() == gridItemLocation.getXCoordinate() &&
                        gridItem.getYCoordinate() == gridItemLocation.getYCoordinate()))
                .toList();
        if (overlappingLocation.isEmpty() || (overlappingLocation.size() == 1
                && Objects.equals(overlappingLocation.get(0).getId(), gridItemLocation.getId()))) {
            return gridItemLocationRepository.save(gridItemLocation);
        } else {
            throw new IllegalArgumentException("Item already exists at this location");
        }
    }

    /**
     * Remove a gridItemLocation from the repository
     *
     * @param gridItemLocation the gridItemLocation to remove
     */
    public void removeGridItemLocation(GridItemLocation gridItemLocation) {
        gridItemLocationRepository.deleteById(gridItemLocation.getId());
    }

    /**
     * Checks if item with given details already exists in repository
     * Returns match if item exists, null otherwise
     * Note that it is possible for a plant and a decoration to have the same id
     * Note: this method seems to be only being used in tests
     *
     * @param gridItemType plant or decoration
     * @param itemId       unique id of plant or of decoration
     * @param garden       garden that contains the item
     */
    public Optional<GridItemLocation> getMatchingGridItem(GridItemType gridItemType, Long itemId, Garden garden) {
        return gridItemLocationRepository.findGridItemLocationByObjectIdAndItemTypeAndGarden(itemId, gridItemType, garden);
    }

}
