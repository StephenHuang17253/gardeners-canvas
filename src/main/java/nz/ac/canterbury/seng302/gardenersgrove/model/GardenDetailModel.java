package nz.ac.canterbury.seng302.gardenersgrove.model;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;

/**
 * Model class for storing garden data for garden detail pages
 */
public class GardenDetailModel {
    private Long gardenId;
    private boolean gardenIsPublic;
    private String gardenName;
    private String gardenDescription;
    private String gardenLocation;
    private Double gardenSize;

    /**
     * Constructor for a GardenDetailModel
     *
     * @param garden entity object to extract attributes from
     */
    public GardenDetailModel(Garden garden) {
        this.gardenId = garden.getGardenId();
        this.gardenIsPublic = garden.getIsPublic();
        this.gardenName = garden.getGardenName();
        this.gardenDescription = garden.getGardenDescription();
        this.gardenLocation = garden.getGardenLocation();
        this.gardenSize = garden.getGardenSize();
    }

    public Long getGardenId() {
        return gardenId;
    }

    public String getGardenName() {
        return gardenName;
    }

    public boolean isGardenIsPublic() {
        return gardenIsPublic;
    }

    public String getGardenDescription() {
        return gardenDescription;
    }

    public String getGardenLocation() {
        return gardenLocation;
    }

    public Double getGardenSize() {
        return gardenSize;
    }

}
