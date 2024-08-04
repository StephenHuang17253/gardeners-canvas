package nz.ac.canterbury.seng302.gardenersgrove.model;

import com.fasterxml.jackson.databind.JsonNode;
import nz.ac.canterbury.seng302.gardenersgrove.entity.PlantInfo;

/**
 * Model class for storing plant data from API call, used for the Plant Details page.
 */
public class PlantInfoModel {

    private String commonName;
    private String scientificName;

    // TODO: Sort out the extra information in the plantInfoModel below. Decide what is useful to keep and what isn't.
    //    private List<String> otherName;
    //    private String family;
    //    private String origin;
    //    private String type;
    //    private String dimensions;
    //    private String cycle;
    //    private String watering;
    //    private String depthWaterRequirement;
    //    private String volumeWaterRequirement;
    //    private String wateringPeriod;
    //    private String wateringGeneralBenchmark;
    //    private List<String> plantAnatomy;
    //    private List<String> sunlight;
    //    private List<String> pruningMonth;
    //    private int pruningCount;
    //    private int seeds;
    //    private List<String> attracts;
    //    private List<String> propagation;
    //    private boolean flowers;
    //    private String floweringSeason;
    //    private List<String> leafColor;
    //    private boolean edibleLeaf;
    //    private String growthRate;
    //    private String maintenance;
    //    private boolean medicinal;
    //    private boolean poisonousToHumans;
    //    private boolean poisonousToPets;
    //    private boolean droughtTolerant;
    //    private boolean saltTolerant;
    //    private boolean thorny;
    //    private boolean invasive;
    //    private boolean rare;
    //    private String rareLevel;
    //    private boolean tropical;
    //    private boolean cuisine;
    //    private boolean indoor;
    //    private String careLevel;
    private String description;
    private String defaultImage;

    /**
     * Constructor for a PlantInfoModel
     * @param plantDetails the JSON response from the API.
     */
    public PlantInfoModel(JsonNode plantDetails) {
        this.commonName = plantDetails.get("common_name").asText();
        this.description = plantDetails.get("description").asText();
        this.defaultImage = getImageURL(plantDetails);
    }
    /**
     * Constructor for a PlantSearchModel for default plants
     * @param plantInfo entity object
     */
    public PlantInfoModel(PlantInfo plantInfo) {
        this.commonName = plantInfo.getName();
        this.scientificName = plantInfo.getScientificName();
        this.defaultImage = plantInfo.getImageURL();
        this.description = plantInfo.getDescription();
    }


    private String getImageURL(JsonNode plantDetails) {

        if (plantDetails.get("default_image").get("regular_url") != null) {
            return plantDetails.get("default_image").get("regular_url").asText();
        }

        if (plantDetails.get("default_image").get("original_url") != null) {
            return plantDetails.get("default_image").get("original_url").asText();
        }
        return "";
    }

    public String getCommonName() {
        return commonName;
    }
    public String getDescription() {
        return description;
    }
    public String getDefaultImage() {
        return defaultImage;
    }


    public String getScientificName() {
        return scientificName;
    }
}
