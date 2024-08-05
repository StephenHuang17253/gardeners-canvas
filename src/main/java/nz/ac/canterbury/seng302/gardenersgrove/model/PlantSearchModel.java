package nz.ac.canterbury.seng302.gardenersgrove.model;

import com.fasterxml.jackson.databind.JsonNode;
import nz.ac.canterbury.seng302.gardenersgrove.entity.PlantInfo;

/**
 * Used for displaying search results from the Plant Wiki.
 */
public class PlantSearchModel {

    private Long id;

    private String commonName;

    private String scientificName;

    private String otherNames;

    private String image;

    /**
     * Constructor for a PlantSearchModel
     * @param plantList the JSON response from the API.
     */
    public PlantSearchModel(JsonNode plantList) {
        this.id = plantList.get("id").asLong();
        this.commonName = plantList.get("common_name").asText();
        this.scientificName = plantList.get("scientific_name").get(0).asText();
        this.otherNames = extractOtherNames(plantList.get("other_name"));
        this.image = getImageURL(plantList);
    }

    /**
     * Constructor for a PlantSearchModel for default plants
     * @param plantInfo entity object
     */
    public PlantSearchModel(PlantInfo plantInfo) {
        this.id = plantInfo.getId();
        this.commonName = plantInfo.getName();
        this.scientificName = plantInfo.getScientificName();
        this.image = plantInfo.getImageURL();
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

    private String extractOtherNames(JsonNode otherNames) {
        if (otherNames == null || !otherNames.isArray()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (JsonNode node : otherNames) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(node.asText());
            first = false;
        }

        return sb.toString();
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

    public String getScientificName() {
        return scientificName;
    }

    public String getOtherNames() {
        return otherNames;
    }

}
