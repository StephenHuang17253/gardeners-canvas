package nz.ac.canterbury.seng302.gardenersgrove.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Used for displaying search results from the Plant Wiki.
 */
public class PlantSearchModel {

    private Long id;

    private String commonName;

    private String image;

    /**
     * Constructor for a PlantSearchModel
     * @param plantList the JSON response from the API.
     */
    public PlantSearchModel(JsonNode plantList) {
        this.commonName = plantList.get("common_name").asText();
        this.id = plantList.get("id").asLong();
        this.image = getImageURL(plantList);
    }

    private String getImageURL(JsonNode plantList) {

        if (plantList.get("default_image").get("regular_url") != null) {
            return plantList.get("default_image").get("regular_url").asText();
        }

        if (plantList.get("default_image").get("original_url") != null) {
            return plantList.get("default_image").get("original_url").asText();
        }
        return "";
    }

    public Long getId() {
        return id;
    }

    public String getCommonName() {
        return commonName;
    }

    public String getImage() {
        return image;
    }



}
