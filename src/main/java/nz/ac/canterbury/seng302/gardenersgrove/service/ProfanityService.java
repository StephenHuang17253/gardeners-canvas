package nz.ac.canterbury.seng302.gardenersgrove.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nz.ac.canterbury.seng302.gardenersgrove.component.ProfanityResponseData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * class for sending requests to profanity API and checking if any bad words are
 * in the API return.
 */
@Service
public class ProfanityService {
    @Value("${azure.moderator.token}")
    private String moderatorKey;
    @Value("${azure.service.endpoint}")
    private String endPoint;
    Logger logger = LoggerFactory.getLogger(ProfanityService.class);
    private HttpClient httpClient;
    ObjectMapper objectMapper = new ObjectMapper();

    private final AtomicLong nextFreeCallTimestamp = new AtomicLong(new Date().getTime());
    private static final long RATE_LIMIT_DELAY_MS = 1500;
    private static final long RATE_LIMIT_DELAY_BUFFER = 5;
    String emptyRegex = "^\\s*$";
    /**
     * General constructor for profanity service, creates new http client.
     * always use this constructor when running real api calls
     */
    public ProfanityService () {
        httpClient = HttpClient.newHttpClient();
    }
    /**
     * Overloaded constructor with input for a mock http clients, used to
     * mock the function of api calls
     * Test Use Only
     * @param httpClientMock mockHttpClient used to mock the api calls when testing the Profanity service
     */
    public ProfanityService (HttpClient httpClientMock)
    {
        httpClient = httpClientMock;
    }
    /**
     * Sends post a post request to the bad words API and then returns a JSON
     * response.
     * 
     * @param content The string for which profanity is checked.
     * @return A return string form the api that has been processed. The return format contains: Original text string,
     * NormalizedText string, Misrepresentation boolean, Language string, Terms list, Status object and TrackingID string
     */
    public ProfanityResponseData moderateContent(String content) {
        try {
            waitForRateLimit();
            logger.info("Profaintiy service input: "+ content);
            String encodedContent = URLEncoder.encode(content, StandardCharsets.UTF_8);
            logger.debug("Sent profainty API request : " + new Date().getTime());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endPoint + "/contentmoderator/moderate/v1.0/ProcessText/Screen?text=" + encodedContent))
                    .header("Content-Type", "text/plain")
                    .header("Ocp-Apim-Subscription-Key", moderatorKey)
                    .POST(HttpRequest.BodyPublishers.ofString(content))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("Profaintiy service response: : "+ response.body());

            JsonNode jsonObject = objectMapper.readTree(response.body());
            ProfanityResponseData profanityResponse = objectMapper.treeToValue(jsonObject, ProfanityResponseData.class);

            logger.info("Call limit passed: "+ profanityResponse.isCallLimitExceeded());
            return profanityResponse;

        } catch (IOException | InterruptedException errorException) {
            if (errorException.getMessage().contains("Moderate have exceeded call rate limit")) {
                logger.info("Call limit reached!");
            }
            Thread.currentThread().interrupt();
            logger.error(String.format("Automatic Moderation Failure, Moderate Manually %s", errorException.getMessage()));
            return null; ///RETURN ERROR, FIX LATER
        }
    }


    /**
     * Checks a string to see if it contains any bad words, if so return True.
     * @param inputString The string to send to the content moderator API Which will check for any profanity.
     * @return True if terms has one or more bad words. If not found return false
     */
    public boolean containsProfanity (String inputString) {
        if (inputString.matches(emptyRegex)) {
            return false;
        }

        //Checking if the input is a tag stored in database with allocated status.
        List<TagStatus> previousOccurrenceOfTag = gardenTagService.getAllSimilar(inputString).stream().map(GardenTag::getTagStatus).toList();
        if (previousOccurrenceOfTag.stream().anyMatch(tagStatus -> tagStatus == TagStatus.INAPPROPRIATE)) {
            return true;
        } else if (previousOccurrenceOfTag.stream().anyMatch(tagStatus -> tagStatus == TagStatus.APPROPRIATE)) {
            return false;
        }

        ProfanityResponseData returnedData = moderateContent(inputString);
        logger.info(returnedData.toString());

        return returnedData.isHasProfanity();
    }
    /**
     * Handles profanity API rate limiting, when called instructs thread to wait for next available time slot.
     */
    private void waitForRateLimit() throws InterruptedException {
        logger.debug("Scheduled new moderation call at " + new Date().getTime());
        long timeToWait = 0;
        boolean couldUpdate = false;
        while (!couldUpdate)
        {
            long nextCallSlot = nextFreeCallTimestamp.get();
            if (nextCallSlot < new Date().getTime())
            {
                timeToWait = 0;
                couldUpdate = nextFreeCallTimestamp.compareAndSet(nextCallSlot, (new Date().getTime() + RATE_LIMIT_DELAY_BUFFER + RATE_LIMIT_DELAY_MS));
            }
            else
            {
                timeToWait = nextCallSlot - new Date().getTime();
                couldUpdate = nextFreeCallTimestamp.compareAndSet(nextCallSlot, (nextCallSlot+ RATE_LIMIT_DELAY_BUFFER + RATE_LIMIT_DELAY_MS));
            }
        }
        Thread.sleep(timeToWait);

    }
}
