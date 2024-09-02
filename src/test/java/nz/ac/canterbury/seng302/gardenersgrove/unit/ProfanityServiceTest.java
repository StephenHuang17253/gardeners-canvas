package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTag;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenTagService;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityService;
import nz.ac.canterbury.seng302.gardenersgrove.util.PriorityType;
import nz.ac.canterbury.seng302.gardenersgrove.util.TagStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.List;

class ProfanityServiceTest {

    private ProfanityService profanityService;

    private HttpClient httpClientMock;

    private GardenTagService gardenTagServiceMock;

    private ObjectMapper objectMapper;

    private String profanitySentence = "Hello there John Doe";

    @BeforeEach
    void init() {
        httpClientMock = Mockito.mock(HttpClient.class);
        gardenTagServiceMock = Mockito.mock(GardenTagService.class);
        objectMapper = new ObjectMapper();
        profanityService = new ProfanityService(httpClientMock, gardenTagServiceMock, objectMapper);
        ReflectionTestUtils.setField(profanityService, "endPoint",
                "https://gg-content-moderator.cognitiveservices.anImaginaryWebsite.com/");
        ReflectionTestUtils.setField(profanityService, "moderatorKey", "NotARealKey123");
    }

    @Test
    void moderateContent_ProfanityMatchesApiCall_CorrectApiCallResponse() throws IOException, InterruptedException {

        HttpResponse<String> mockHttpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(mockHttpResponse.body()).thenReturn(
                "{\"OriginalText\":\"No bad input\",\"NormalizedText\":\" bad input\",\"Misrepresentation\":null,\"Language\":\"eng\",\"Terms\":[{\"Index\":7,\"OriginalIndex\":12,\"ListId\":0,\"Term\":\"BadWord\"}],\"Status\":{\"Code\":3000,\"Description\":\"OK\",\"Exception\":null},\"TrackingId\":\"e7b5c1ba-48cf-4b58-b3f1-41dce34ae0c5\"}");
        Mockito.when(httpClientMock.send(Mockito.any(), Mockito.eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockHttpResponse);

        Assertions.assertEquals(
                "{\"OriginalText\":\"No bad input\",\"NormalizedText\":\" bad input\",\"Misrepresentation\":null,\"Language\":\"eng\",\"Terms\":[{\"Index\":7,\"OriginalIndex\":12,\"ListId\":0,\"Term\":\"BadWord\"}],\"Status\":{\"Code\":3000,\"Description\":\"OK\",\"Exception\":null},\"TrackingId\":\"e7b5c1ba-48cf-4b58-b3f1-41dce34ae0c5\"}",
                profanityService.moderateContent(profanitySentence).toString());
    }

    @Test
    void containsProfanity_NoProfanityFound_returnsFalse() throws IOException, InterruptedException {
        HttpResponse<String> mockHttpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(mockHttpResponse.body()).thenReturn(
                "{\"OriginalText\":\"No bad input\",\"NormalizedText\":\" bad input\",\"Misrepresentation\":null,\"Language\":\"eng\",\"Terms\":null,\"Status\":{\"Code\":3000,\"Description\":\"OK\",\"Exception\":null},\"TrackingId\":\"e7b5c1ba-48cf-4b58-b3f1-41dce34ae0c5\"}");
        Mockito.when(httpClientMock.send(Mockito.any(), Mockito.eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockHttpResponse);
        Assertions.assertFalse(profanityService.containsProfanity(profanitySentence, PriorityType.NORMAL));
    }

    @Test
    void containsProfanity_profanityFound_returnsTrue() throws IOException, InterruptedException {
        HttpResponse<String> mockHttpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(mockHttpResponse.body()).thenReturn(
                "{\"OriginalText\":\"No bad input\",\"NormalizedText\":\" bad input\",\"Misrepresentation\":null,\"Language\":\"eng\",\"Terms\":[{\"Index\":7,\"OriginalIndex\":12,\"ListId\":0,\"Term\":\"BadWord\"}],\"Status\":{\"Code\":3000,\"Description\":\"OK\",\"Exception\":null},\"TrackingId\":\"e7b5c1ba-48cf-4b58-b3f1-41dce34ae0c5\"}");
        Mockito.when(httpClientMock.send(Mockito.any(), Mockito.eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockHttpResponse);
        Assertions.assertTrue(profanityService.containsProfanity(profanitySentence, PriorityType.NORMAL));
    }

    @Test
    void containsProfanityCall_BackToBackCalls_CallsRateLimited() throws IOException, InterruptedException {
        HttpResponse<String> mockHttpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(mockHttpResponse.body()).thenReturn(
                "{\"OriginalText\":\"No bad input\",\"NormalizedText\":\" bad input\",\"Misrepresentation\":null,\"Language\":\"eng\",\"Terms\":[{\"Index\":7,\"OriginalIndex\":12,\"ListId\":0,\"Term\":\"BadWord\"}],\"Status\":{\"Code\":3000,\"Description\":\"OK\",\"Exception\":null},\"TrackingId\":\"e7b5c1ba-48cf-4b58-b3f1-41dce34ae0c5\"}");
        Mockito.when(httpClientMock.send(Mockito.any(), Mockito.eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockHttpResponse);
        profanityService.moderateContent(profanitySentence);
        long preSecondCallTime = new Date().getTime();
        profanityService.moderateContent(profanitySentence);
        long postSecondCallTime = new Date().getTime();
        Assertions.assertTrue((preSecondCallTime + 1100L) < postSecondCallTime);
    }

    @Test
    void containsProfanity_inappropriateTagStatusFoundInPersistence_returnsTrue() {
        GardenTag matchingTag = new GardenTag(profanitySentence);
        matchingTag.setTagStatus(TagStatus.INAPPROPRIATE);
        Mockito.when(gardenTagServiceMock.getAllSimilar(profanitySentence)).thenReturn(List.of(matchingTag));
        Assertions.assertTrue(profanityService.containsProfanity(profanitySentence, PriorityType.NORMAL));
    }

    @Test
    void containsProfanity_inappropriateTagStatusFoundInPersistence_doesNotCallAPI() {
        GardenTag matchingTag = new GardenTag(profanitySentence);
        matchingTag.setTagStatus(TagStatus.INAPPROPRIATE);
        Mockito.when(gardenTagServiceMock.getAllSimilar(profanitySentence)).thenReturn(List.of(matchingTag));
        Assertions.assertTrue(profanityService.containsProfanity(profanitySentence, PriorityType.NORMAL));
        Mockito.verifyNoInteractions(httpClientMock);
    }

    @Test
    void containsProfanity_appropriateTagStatusFoundInPersistence_returnsFalse() {
        GardenTag matchingTag = new GardenTag(profanitySentence);
        matchingTag.setTagStatus(TagStatus.APPROPRIATE);
        Mockito.when(gardenTagServiceMock.getAllSimilar(profanitySentence)).thenReturn(List.of(matchingTag));
        Assertions.assertFalse(profanityService.containsProfanity(profanitySentence, PriorityType.NORMAL));
    }

    @Test
    void containsProfanity_appropriateTagStatusFoundInPersistence_doesNotCallAPI() {
        GardenTag matchingTag = new GardenTag(profanitySentence);
        matchingTag.setTagStatus(TagStatus.APPROPRIATE);
        Mockito.when(gardenTagServiceMock.getAllSimilar(profanitySentence)).thenReturn(List.of(matchingTag));
        Assertions.assertFalse(profanityService.containsProfanity(profanitySentence, PriorityType.NORMAL));
        Mockito.verifyNoInteractions(httpClientMock);
    }

    @Test
    void containsProfanity_pendingTagStatusFoundInPersistence_callsAPI() throws IOException, InterruptedException {
        GardenTag matchingTag = new GardenTag(profanitySentence);
        matchingTag.setTagStatus(TagStatus.PENDING);
        Mockito.when(gardenTagServiceMock.getAllSimilar(profanitySentence)).thenReturn(List.of(matchingTag));
        HttpResponse<String> mockHttpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(mockHttpResponse.body()).thenReturn(
                "{\"OriginalText\":\"No bad input\",\"NormalizedText\":\" bad input\",\"Misrepresentation\":null,\"Language\":\"eng\",\"Terms\":[{\"Index\":7,\"OriginalIndex\":12,\"ListId\":0,\"Term\":\"BadWord\"}],\"Status\":{\"Code\":3000,\"Description\":\"OK\",\"Exception\":null},\"TrackingId\":\"e7b5c1ba-48cf-4b58-b3f1-41dce34ae0c5\"}");
        Mockito.when(httpClientMock.send(Mockito.any(), Mockito.eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockHttpResponse);
        Assertions.assertTrue(profanityService.containsProfanity(profanitySentence, PriorityType.NORMAL));
        Mockito.verify(httpClientMock, Mockito.times(1)).send(Mockito.any(),
                Mockito.eq(HttpResponse.BodyHandlers.ofString()));
        Mockito.verifyNoMoreInteractions(httpClientMock);
    }
}
