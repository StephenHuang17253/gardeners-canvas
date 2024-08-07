package nz.ac.canterbury.seng302.gardenersgrove.model;

import com.fasterxml.jackson.databind.JsonNode;
import nz.ac.canterbury.seng302.gardenersgrove.entity.PlantInfo;


/**
 * Model class for storing plant data from API call, used for the Plant Details page.
 */
public class PlantInfoModel {

    private String commonName;
    private String scientificName;
    private String watering;
    private String cycle;
    private String wateringPeriod;
    private String wateringGeneralBenchmarkValue;
    private String wateringGeneralBenchmarkUnit;
    private String sunlight;
    private String pruningMonth;
    private String pruningCountAmount;

    private String pruningCountInterval;
    private boolean flowers;
    private String floweringSeason;
    private boolean fruits;
    private boolean edibleFruit;
    private String fruitSeason;
    private boolean poisonousToHumans;
    private boolean poisonousToPets;
    private String maintenance;
    private boolean indoor;
    private String description;
    private String defaultImage;

    /**
     * Constructor for a PlantInfoModel
     * @param plantDetails the JSON response from the API.
     */
    public PlantInfoModel(JsonNode plantDetails) {
        this.commonName = plantDetails.get("common_name").asText();
        this.scientificName = plantDetails.get("scientific_name").get(0).asText();
        this.description = plantDetails.get("description").asText();
        this.defaultImage = getImageURL(plantDetails);
        this.watering = nullChecker("watering", plantDetails);
        this.cycle = nullChecker("cycle", plantDetails);
        this.wateringPeriod = nullChecker("wateringPeriod", plantDetails);
        this.wateringGeneralBenchmarkValue = nullChecker("value", plantDetails.get("watering_general_benchmark"));
        this.wateringGeneralBenchmarkUnit = nullChecker("unit", plantDetails.get("watering_general_benchmark"));
        this.sunlight = extractStringNames(plantDetails.get("sunlight").get(0));
        this.pruningMonth = nullChecker("pruning_month", plantDetails);
        this.pruningCountAmount = nullChecker("amount", plantDetails.get("pruning_count"));
        this.pruningCountInterval = nullChecker("interval", plantDetails.get("pruning_count"));
        this.flowers = plantDetails.get("flowers").asBoolean();
        this.floweringSeason = nullChecker("flowering_season", plantDetails);
        this.fruits = plantDetails.get("fruits").asBoolean();
        this.edibleFruit = plantDetails.get("edible_fruit").asBoolean();
        this.fruitSeason = nullChecker("fruit_season", plantDetails);
        this.poisonousToHumans = plantDetails.get("poisonous_to_humans").asBoolean();
        this.poisonousToPets = plantDetails.get("poisonous_to_pets").asBoolean();
        this.maintenance = nullChecker("maintenance", plantDetails);
        this.indoor = plantDetails.get("indoor").asBoolean();
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
        this.watering = plantInfo.getWatering();
        this.cycle = plantInfo.getCycle();
        this.wateringPeriod = plantInfo.getWateringPeriod();
        this.wateringGeneralBenchmarkValue = plantInfo.getWateringGeneralBenchmarkValue();
        this.wateringGeneralBenchmarkUnit = plantInfo.getWateringGeneralBenchmarkUnit();
        this.sunlight = plantInfo.getSunlight();
        this.pruningMonth = plantInfo.getPruningMonth();
        this.pruningCountAmount = plantInfo.getPruningCountAmount();
        this.pruningCountInterval = plantInfo.getPruningCountInterval();
        this.flowers = plantInfo.getFlowers();
        this.floweringSeason = plantInfo.getFloweringSeason();
        this.fruits = plantInfo.getFruits();
        this.edibleFruit = plantInfo.getEdibleFruit();
        this.fruitSeason = plantInfo.getFruitSeason();
        this.poisonousToHumans = plantInfo.getPoisonousToHumans();
        this.poisonousToPets = plantInfo.getPoisonousToPets();
        this.maintenance = plantInfo.getMaintenance();
        this.indoor = plantInfo.getIndoor();

    }


    /**
     * Checks if the attribute value returned from plantDetails is null
     * @param attribute - value to get from plantDetails
     * @param plantDetails - JSON response from Perenual API
     * @return "" if attribute value is null, or the value as string
     */
    public String nullChecker(String attribute, JsonNode plantDetails) {
        if (plantDetails == null) {
            return "";
        }
        JsonNode plantAttribute = plantDetails.get(attribute);
        if (plantAttribute != null) {
            return plantAttribute.asText();
        }
        return "";
    }

    /**
     * Goes through images for a plant from the plantDetails
     * Finds whichever is not null
     * @param plantDetails - the JSON response from Perenual API
     * @return the imageURL or empty string is unavailable
     */
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
    public String getWatering() {
        return watering;
    }

    public String getCycle() {return cycle;}
    public String getWateringGeneralBenchmarkValue() {return wateringGeneralBenchmarkValue;}
    public String getWateringGeneralBenchmarkUnit() {return wateringGeneralBenchmarkUnit;}
    public String getSunlight() {return sunlight;}
    public String getPruningMonth() {return pruningMonth;}
    public String getPruningCountAmount() {return pruningCountAmount;}
    public String getPruningCountInterval() {return pruningCountInterval;}
    public boolean getFlowers() {return flowers;}
    public String getFloweringSeason() {return floweringSeason;}
    public boolean getFruits() {return fruits;}
    public boolean getEdibleFruit() {return edibleFruit;}
    public String getFruitSeason() {return fruitSeason;}
    public boolean getPoisonousToHumans() {return poisonousToHumans;}
    public boolean getPoisonousToPets() {return poisonousToPets;}
    public String getMaintenance() {return maintenance;}
    public boolean getIndoor() {return indoor;}


    /**
     * Takes a List<String> from the JSON data
     * Converts to String with commas separating strings
     * @param stringList the JsonNode data
     * @return String of all the values with commas
     */
    private String extractStringNames(JsonNode stringList) {
        if (stringList == null || !stringList.isArray()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (JsonNode node : stringList) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(node.asText());
            first = false;
        }

        return sb.toString();
    }

}
