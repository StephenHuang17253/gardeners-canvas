package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

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
    public PlantInfo(Long id, String name, String scientificName, String imageURL, String description, String detailJSONResponseString, String watering){
        this.id = id;
        this.name = name;
        this.scientificName = scientificName;
        this.imageURL = imageURL;
        this.description = description;
        this.detailJSONResponseString = detailJSONResponseString;
        this.watering = watering;
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
}
