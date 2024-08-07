package nz.ac.canterbury.seng302.gardenersgrove.model;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Model class for recent garden data
 */
public class RecentGardenModel {

    private Long gardenId;
    private String gardenName;
    private String gardenLocation;
    private String gardenSize;
    private int plantCount;
    private String ownerImageURL;
    private String ownerName;
    private List<String> plantImageURLs;
    private boolean isOwner;

    /**
     * Constructor for a RecentGardenModel
     *
     * @param garden the recent garden
     * @param owner of the recent garden
     */
    public RecentGardenModel(Garden garden, User owner, boolean isOwner){
        this.gardenId = garden.getGardenId();
        this.gardenName = garden.getGardenName();
        this.gardenLocation = garden.getGardenCity() + ", " + garden.getGardenCountry();
        this.gardenSize = String.valueOf(garden.getGardenSize());
        this.plantCount = garden.getPlants().size();
        this.ownerImageURL = owner.getProfilePictureFilename();
        this.ownerName = owner.getFirstName() + " " + owner.getLastName();
        this.plantImageURLs = garden.getPlants().stream()
                .limit(3)
                .map(Plant::getPlantPictureFilename)
                .collect(Collectors.toList());
        this.isOwner = isOwner;
    }

    public Long getGardenId() {
        return gardenId;
    }

    public String getGardenName() {
        return gardenName;
    }

    public String getGardenLocation() {
        return gardenLocation;
    }

    public int getPlantCount() {
        return plantCount;
    }

    public String getOwnerImageURL() {
        return ownerImageURL;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public List<String> getPlantImageURLs() {
        return plantImageURLs;
    }

    public String getGardenSize() {
        return gardenSize;
    }

    public boolean isOwner() {
        return isOwner;
    }
}
