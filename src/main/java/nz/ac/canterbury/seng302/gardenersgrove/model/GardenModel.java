package nz.ac.canterbury.seng302.gardenersgrove.model;
/**
 * Model class for storing garden session data for the navbar dropdown.
 */
public class GardenModel {

    private Long gardenId;

    private String gardenName;

    /**
     * Constructor for a GardenModel
     *
     * @param gardenId id of the garden entity
     * @param gardenName name of the garden entity
     */
    public GardenModel(long gardenId, String gardenName){
        this.gardenId = gardenId;
        this.gardenName = gardenName;
    }

    public Long getGardenId() {
        return gardenId;
    }
    public String getGardenName() {
        return gardenName;
    }

}
