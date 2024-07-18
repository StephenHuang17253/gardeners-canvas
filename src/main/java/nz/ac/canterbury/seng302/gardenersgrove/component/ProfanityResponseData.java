package nz.ac.canterbury.seng302.gardenersgrove.component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

/**
 * This entity class is for representing and processing the response given by the profanity service
 */
public class ProfanityResponseData {

    @JsonProperty("OriginalText")
    private String originalText;

    @JsonProperty("NormalizedText")
    private String normalizedText;

    @JsonProperty("Misrepresentation")
    private String misrepresentation;

    @JsonProperty("Language")
    private String language;

    @JsonProperty("Terms")
    private List<Term> terms;

    @JsonProperty("Status")
    private Status status;

    @JsonProperty("TrackingId")
    private String trackingId;

    private boolean descriptionContainsProfanity;
    private boolean tagsContainsProfanity;

    public ProfanityResponseData(JsonNode jsonProfanityData) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ProfanityResponseData responseData = objectMapper.treeToValue(jsonProfanityData, ProfanityResponseData.class);
            this.originalText = responseData.originalText;
            this.normalizedText = responseData.normalizedText;
            this.misrepresentation = responseData.misrepresentation;
            this.language = responseData.language;
            this.terms = responseData.terms;
            this.status = responseData.status;
            this.trackingId = responseData.trackingId;
            checkForProfanity();
        } catch (Exception profanityServiceReturnError) {
            profanityServiceReturnError.printStackTrace();
        }
    }
    private void checkForProfanity() {
        // TBA
    }

    public static class Term {
        @JsonProperty("Index")
        private int index;

        @JsonProperty("OriginalIndex")
        private int originalIndex;

        @JsonProperty("ListId")
        private int listId;

        @JsonProperty("Term")
        private String term;
    }

    public static class Status {
        @JsonProperty("Code")
        private int code;

        @JsonProperty("Description")
        private String description;

        @JsonProperty("Exception")
        private String exception;
    }
}
