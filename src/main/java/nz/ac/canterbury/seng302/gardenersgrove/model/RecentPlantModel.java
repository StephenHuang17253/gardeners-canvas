package nz.ac.canterbury.seng302.gardenersgrove.model;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;

/**
 * Model class for storing plant data for showing recently added plants on the home page
 */
public class RecentPlantModel {
    private final Long plantId;
    private final String plantName;
    private final String plantPictureFileName;
    private final boolean isOwner;
    private final String gardenName;
    private final Long gardenId;
    private final String ownerImageFileName;
    private final String ownerName;

    /**
     * Constructor for Model. It sets are required details for recent plant cards
     *
     * @param plant   Plant object
     * @param garden  Garden the plant is in
     * @param owner   Owner of the garden and therefore plant
     * @param isOwner boolean if the plant belongs to current user
     */
    public RecentPlantModel(Plant plant, Garden garden, User owner, boolean isOwner) {
        this.plantId = plant.getPlantId();
        this.plantName = plant.getPlantName();
        this.plantPictureFileName = plant.getPlantPictureFilename();
        this.ownerName = owner.getFirstName() + " " + owner.getLastName();
        this.isOwner = isOwner;
        this.gardenId = garden.getGardenId();
        this.gardenName = garden.getGardenName();
        this.ownerImageFileName = owner.getProfilePictureFilename();
    }

    public Long getGardenId() {
        return gardenId;
    }

    public String getGardenName() {
        return gardenName;
    }

    public String getOwnerImageFileName() {
        return ownerImageFileName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getPlantPictureFileName() {
        return plantPictureFileName;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public String getPlantName() {
        return plantName;
    }

    public Long getPlantId() {
        return plantId;
    }
}
