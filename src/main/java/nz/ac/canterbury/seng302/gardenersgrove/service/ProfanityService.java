package nz.ac.canterbury.seng302.gardenersgrove.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nz.ac.canterbury.seng302.gardenersgrove.component.ProfanityResponseData;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTag;
import nz.ac.canterbury.seng302.gardenersgrove.util.PriorityType;
import nz.ac.canterbury.seng302.gardenersgrove.util.TagStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Random;
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
    private final HttpClient httpClient;
    ObjectMapper objectMapper = new ObjectMapper();

    private final AtomicLong nextFreeCallTimestamp = new AtomicLong(new Date().getTime());
    private static final long RATE_LIMIT_DELAY_MS = 1500;
    private static final long RATE_LIMIT_DELAY_BUFFER = 5;
    String emptyRegex = "^\\s*$";

    Random random = new Random();

    /**
     * Service to handle tag database checks.
     */
    private final GardenTagService gardenTagService;

    /**
     * General constructor for profanity service, creates new http client.
     * always use this constructor when running real api calls
     */
    @Autowired
    public ProfanityService(GardenTagService gardenTagService) {
        httpClient = HttpClient.newHttpClient();
        this.gardenTagService = gardenTagService;
    }

    /**
     * Overloaded constructor with input for a mock http clients, used to
     * mock the function of api calls
     * Test Use Only
     *
     * @param httpClientMock       mockHttpClient used to mock the api calls when
     *                             testing the Profanity service
     * @param gardenTagServiceMock mocked garden service for unit testing.
     */
    public ProfanityService(HttpClient httpClientMock, GardenTagService gardenTagServiceMock) {
        httpClient = httpClientMock;
        this.gardenTagService = gardenTagServiceMock;
    }

    /**
     * Sends post a post request to the bad words API and then returns a JSON
     * response.
     *
     * @param content The string for which profanity is checked.
     * @return A return string from the api that has been processed. The return
     *         format contains: Original text string,
     *         NormalizedText string, Misrepresentation boolean, Language string,
     *         Terms list, Status object and TrackingID string
     */
    public ProfanityResponseData moderateContent(String content) {
        try {
            logger.info("sending normal priority call to normal moderation queue");
            waitForRateLimit();
        } catch (InterruptedException errorException) {
            logger.error("Automatic Moderation Failure, Moderate Manually {}", errorException.getMessage());
            Thread.currentThread().interrupt();
            return null;
        }
        return moderateContentApiCall(content);

    }

    /**
     * Sends post a post request to the bad words API and then returns a JSON
     * response. Experiences longer than average que times compared to normal
     * function
     *
     * @param content The string for which profanity is checked.
     * @return A return string from the api that has been processed. The return
     *         format contains: Original text string,
     *         NormalizedText string, Misrepresentation boolean, Language string,
     *         Terms list, Status object and TrackingID string
     */
    public ProfanityResponseData moderateContentLowPriority(String content) {
        try {
            logger.info("Queuing low priority moderation call in low priority queue");
            long nextCallSlot = nextFreeCallTimestamp.get();
            long timeToWait = 0;
            while (nextCallSlot > new Date().getTime()) {
                timeToWait = nextCallSlot - new Date().getTime();
                timeToWait = timeToWait + random.nextInt(100, 300);
                Thread.sleep(timeToWait);
                nextCallSlot = nextFreeCallTimestamp.get();
            }

            logger.info("sending low priority call to normal moderation queue");
            waitForRateLimit();
            return moderateContentApiCall(content);
        } catch (InterruptedException errorException) {
            logger.error(
                    String.format("Automatic Moderation Failure, Moderate Manually %s", errorException.getMessage()));
            Thread.currentThread().interrupt();
            return null;
        }

    }

    private ProfanityResponseData moderateContentApiCall(String content) {
        boolean wasProfanityChecked = false;
        int retryCounter = 0;
        while (!wasProfanityChecked && retryCounter < 4) {
            try {

                String encodedContent = URLEncoder.encode(content, StandardCharsets.UTF_8);
                logger.info("Sent profanity API request: {}", new Date().getTime());
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(
                                endPoint + "/contentmoderator/moderate/v1.0/ProcessText/Screen?text=" + encodedContent))
                        .header("Content-Type", "text/plain")
                        .header("Ocp-Apim-Subscription-Key", moderatorKey)
                        .POST(HttpRequest.BodyPublishers.ofString(content))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                logger.info("Profanity service response: {}", response.body());

                JsonNode jsonObject = objectMapper.readTree(response.body());
                ProfanityResponseData profanityResponse = objectMapper.treeToValue(jsonObject,
                        ProfanityResponseData.class);

                logger.info("Call limit passed: {}", profanityResponse.callLimitExceeded());

                if (profanityResponse.callLimitExceeded()) {
                    retryCounter += 1;
                    logger.warn("Could not get profanity response due to ratelimit, retrying {}", retryCounter);
                    waitForRateLimit();
                } else {
                    wasProfanityChecked = true;
                    return profanityResponse;
                }

            } catch (IOException | InterruptedException errorException) {
                if (errorException.getMessage().contains("Moderate have exceeded call rate limit")) {
                    logger.info("Call limit reached!");
                }
                Thread.currentThread().interrupt();
                logger.error(String.format("Automatic Moderation Failure, Moderate Manually %s",
                        errorException.getMessage()));
                return null; /// RETURN ERROR, FIX LATER
            }
        }

        return null;
    }

    /**
     * Checks a string to see if it contains any bad words, if so return True.
     *
     * @param inputString The string to send to the content moderator API Which will
     *                    check for any profanity.
     * @return True if terms has one or more bad words. If not found return false
     */
    public boolean containsProfanity(String inputString, PriorityType priorityType) {

        Boolean precheckResponse = containPriorityPrecheck(inputString);
        if (precheckResponse != null) {
            return precheckResponse;
        }

        ProfanityResponseData returnedData;
        if (priorityType == PriorityType.LOW) {
            returnedData = moderateContentLowPriority(inputString);
            logger.info("Completed Low Priority Profanity Check");
        } else {
            returnedData = moderateContent(inputString);
            logger.info("Completed Normal Priority Profanity Check");
        }
        return returnedData.hasProfanity();
    }

    private Boolean containPriorityPrecheck(String inputString) {
        if (inputString.matches(emptyRegex)) {
            return false;
        }

        // Checking if the input is a tag stored in database with allocated status.
        List<TagStatus> previousOccurrenceOfTag = gardenTagService.getAllSimilar(inputString).stream()
                .map(GardenTag::getTagStatus).toList();
        if (previousOccurrenceOfTag.stream().anyMatch(tagStatus -> tagStatus == TagStatus.INAPPROPRIATE)) {
            return true;
        } else if (previousOccurrenceOfTag.stream().anyMatch(tagStatus -> tagStatus == TagStatus.APPROPRIATE)) {
            return false;
        }
        return null;
    }

    /**
     * Handles profanity API rate limiting, when called instructs thread to wait for
     * next available time slot.
     */
    private void waitForRateLimit() throws InterruptedException {
        long timeToWait = 0;
        boolean couldUpdate = false;
        while (!couldUpdate) {
            long nextCallSlot = nextFreeCallTimestamp.get();
            if (nextCallSlot < new Date().getTime()) {
                timeToWait = 0;
                couldUpdate = nextFreeCallTimestamp.compareAndSet(nextCallSlot,
                        (new Date().getTime() + RATE_LIMIT_DELAY_BUFFER + RATE_LIMIT_DELAY_MS));
            } else {
                timeToWait = nextCallSlot - new Date().getTime();
                couldUpdate = nextFreeCallTimestamp.compareAndSet(nextCallSlot,
                        (nextCallSlot + RATE_LIMIT_DELAY_BUFFER + RATE_LIMIT_DELAY_MS));
            }
        }
        Thread.sleep(timeToWait);

    }
}
