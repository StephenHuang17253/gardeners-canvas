package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTagRelation;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GridItemLocation;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GridItemLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
     * Gets all Garden tags currently stored in the underlying repository
     * @return all tags in underlying repository as a List of tags
     */
    public List<GridItemLocation> getAllGridItemLocations() {return gridItemLocationRepository.findAll();}

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
     * @return the GridItemLocation to save (with filled in id field)
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



}
