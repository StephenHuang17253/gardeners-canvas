package nz.ac.canterbury.seng302.gardenersgrove.component;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.ArrayList;
/**
 * This entity class is for representing and processing the response given by the profanity service
 */
public class ProfanityResponseData {
    private JsonNode originalText;
    private JsonNode normalizedText;
    private JsonNode misrepresentation;
    private JsonNode language;
    private JsonNode terms;
    private JsonNode status;
    private JsonNode trackingId;
    private boolean hasProfanity;
    private List<String> foundTerms;
    private String statusCode;
    private String statusDescription;
    private boolean callLimitExceeded;
    public int errorCode;

    @JsonCreator
    public ProfanityResponseData(JsonNode jsonProfanityData){
        detectError(jsonProfanityData);
        if (!callLimitExceeded) {
            originalText = jsonProfanityData.get("OriginalText");
            normalizedText = jsonProfanityData.get("NormalizedText");
            misrepresentation = jsonProfanityData.get("Misrepresentation");
            language = jsonProfanityData.get("Language");
            terms = jsonProfanityData.get("Terms");
            trackingId = jsonProfanityData.get("TrackingId");
            status = jsonProfanityData.get("Status");
            setStatus();
            setFoundTerms();
            setHasProfanity();
        }
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

    public boolean hasProfanity() {
        return hasProfanity;
    }

    public boolean callLimitExceeded() {return callLimitExceeded;}

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
    /**
     * Sets status variables from the JSON data.
     */
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
     * Checks for errors and sets errorCode if error is found
     */
    void detectError(JsonNode jsonProfanityData) {
        if (jsonProfanityData.has("error")) {
            JsonNode errorNode = jsonProfanityData.get("error");
            if (errorNode.has("code")) {
                String codeString = errorNode.get("code").asText();
                errorCode = Integer.parseInt(codeString);
                if (errorCode == 429) {
                    callLimitExceeded = true;
                }
            }
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
