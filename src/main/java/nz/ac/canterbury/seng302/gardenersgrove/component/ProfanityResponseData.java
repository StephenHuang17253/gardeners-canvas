package nz.ac.canterbury.seng302.gardenersgrove.component;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.ArrayList;
/**
 * This entity class is for representing and processing the response given by the profanity service
 */
public class ProfanityResponseData {
    JsonNode originalText;
    JsonNode normalizedText;
    JsonNode misrepresentation;
    JsonNode language;
    JsonNode terms;
    JsonNode status;
    JsonNode trackingId;
    boolean hasProfanity;
    List<String> foundTerms;
    String statusCode;
    String statusDescription;

    @JsonCreator
    public ProfanityResponseData(JsonNode jsonProfanityData){
        originalText = jsonProfanityData.get("OriginalText");
        normalizedText = jsonProfanityData.get("NormalizedText");
        misrepresentation = jsonProfanityData.get("Misrepresentation");
        language = jsonProfanityData.get("Language");
        terms = jsonProfanityData.get("Terms");
        status = jsonProfanityData.get("Status");
        trackingId = jsonProfanityData.get("TrackingId");

        setFoundTerms();
        setHasProfanity();
        setStatus();
    }
    public String getOriginalText() {
        return originalText.asText();
    }

    public String getNormalizedText() {
        return normalizedText.asText();
    }

    public String getMisrepresentation() {
        return misrepresentation.asText();
    }

    public String getLanguage() {
        return language.asText();
    }

    public List<String> getFoundTerms() {
        return foundTerms;
    }

    public String getStatus() {
        return "Code: " + statusCode + ", Description: " + statusDescription;
    }

    public String getTrackingId() {
        return trackingId.asText();
    }

    public boolean isHasProfanity() {
        return hasProfanity;
    }
    /**
     * Sets the list of found terms from the JSON data.
     */
    void setFoundTerms() {
        foundTerms = new ArrayList<>();
        if (terms.isArray()) {
            for (JsonNode term : terms) {
                foundTerms.add(term.get("Term").asText());
            }
        }
    }
    void setStatus() {
        if (status != null && status.isObject()) {
            statusCode = status.get("Code").asText();
            statusDescription = status.get("Description").asText();
        } else {
            statusCode = "Unknown";
            statusDescription = "Unknown";
        }
    }

    /**
     * Sets the hasProfanity flag based on the presence of terms.
     */
    void setHasProfanity() {
        this.hasProfanity = (terms != null && !terms.isEmpty());
    }

    /**
     * Returns a string representation of the processed profanity
     */
    @Override
    public String toString() {
        return "{" +
                "\"OriginalText\":\"" + getOriginalText() + "\"," +
                "\"NormalizedText\":\"" + getNormalizedText() + "\"," +
                "\"Misrepresentation\":" + getMisrepresentation() + "," +
                "\"Language\":\"" + getLanguage() + "\"," +
                "\"Terms\":" + terms.toString() + "," +
                "\"Status\":" + status.toString() + "," +
                "\"TrackingId\":\"" + getTrackingId() + "\"" +
                "}";
    }
}
