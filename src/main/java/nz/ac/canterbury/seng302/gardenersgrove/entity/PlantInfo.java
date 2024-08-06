package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.Collections;
import java.util.List;

/**
 * Plant Info SpringBoot Entity object
 */

@Entity
public class PlantInfo {
    @Id
    private Long id;
    private String name;
    private String scientificName;
    private String imageURL;
    @Column(length = 1024)
    private String description;
    @Column(length = 500)
    private String detailJSONResponseString;
    private String watering;

    private String cycle;
    private String wateringPeriod;
    private String wateringGeneralBenchmarkValue;
    private String wateringGeneralBenchmarkUnit;
//    private List<String> sunlight;
//    private List<String> pruningMonth;
    private int pruningCount;
    private boolean flowers;
    private String floweringSeason;
    private boolean fruits;
    private boolean edibleFruit;
    private String fruitSeason;
    private boolean poisonousToHumans;
    private boolean poisonousToPets;
    private String maintenance;
    private boolean indoor;

    /**
     * JPA required no-args constructor
     */
    protected PlantInfo() {
    }

    /**
     * Constriuctor for Plant Info entity object
     * @param id of the plant that matches https://perenual.com database id of plants
     * @param name the common name of the plant
     * @param scientificName the scientific name of the plant
     * @param imageURL the image url that contain the plant image
     * @param detailJSONResponseString TODO
     */
    public PlantInfo(Long id, String name, String scientificName, String imageURL, String description, String detailJSONResponseString, String watering, String cycle, String wateringPeriod, String wateringGeneralBenchmark, List<String> sunlight, List<String> pruningMonth, int pruningCount, boolean flowers, String floweringSeason, boolean fruits, boolean edibleFruit, String fruitSeason, boolean poisonousToHumans, boolean poisonousToPets, String maintenance, boolean indoor;
){
        this.id = id;
        this.name = name;
        this.scientificName = scientificName;
        this.imageURL = imageURL;
        this.description = description;
        this.detailJSONResponseString = detailJSONResponseString;
        this.watering = watering;
        this.cycle = cycle;
        this.wateringPeriod = plantDetails.get("water_period").asText();
        this.wateringGeneralBenchmarkValue = plantDetails.get("water_general_benchmark").get("value").asText();
        this.wateringGeneralBenchmarkUnit = plantDetails.get("water_general_benchmark").get("unit").asText();
//        this.sunlight = Collections.singletonList(plantDetails.get("sunlight").asText());
//        this.pruningMonth = Collections.singletonList(plantDetails.get("pruning_month").asText());
        this.pruningCount = plantDetails.get("pruning_count").asInt();
        this.flowers = plantDetails.get("flowers").asBoolean();
        this.floweringSeason = plantDetails.get("flowering_season").asText();
        this.fruits = plantDetails.get("fruits").asBoolean();
        this.edibleFruit = plantDetails.get("edible_fruit").asBoolean();
        this.fruitSeason = plantDetails.get("fruitSeason").asText();
        this.poisonousToHumans = plantDetails.get("poisonousToHumans").asBoolean();
        this.poisonousToPets = plantDetails.get("poisonousToPets").asBoolean();
        this.maintenance = plantDetails.get("maintenance").asText();
        this.indoor = plantDetails.get("indoor").asBoolean();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getScientificName() {
        return scientificName;
    }

    public String getDetailJSONResponseString() {
        return detailJSONResponseString;
    }

    public String getDescription() {
        return description;
    }
    public String getWatering() {
        return watering;
    }
    public String getCycle() {return cycle;};
    public String getWateringPeriod() {return wateringPeriod;};
    public String getWateringGeneralBenchmarkValue() {return wateringGeneralBenchmarkValue;};
    public String getWateringGeneralBenchmarkUnit() {return wateringGeneralBenchmarkUnit;};
//    public List<String> getSunlight() {return sunlight;};
//    public List<String> getPruningMonth() {return pruningMonth;};
    public int getPruningCount() {return pruningCount;};
    public boolean getFlowers() {return flowers;};
    public String getFloweringSeason() {return floweringSeason;};
    public boolean getFruits() {return fruits;};
    public boolean getEdibleFruit() {return edibleFruit;};
    public String getFruitSeason() {return fruitSeason;};
    public boolean getPoisonousToHumans() {return poisonousToHumans;};
    public boolean getPoisonousToPets() {return poisonousToPets;};
    public String getMaintenance() {return maintenance;};
    public boolean getIndoor() {return indoor;};

}
