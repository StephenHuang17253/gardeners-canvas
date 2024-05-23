package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static org.mockito.Mockito.when;

class ProfanityServiceTest {

    private ProfanityService profanityService;
    HttpClient httpClientMock;
    @BeforeEach
    void init()
    {
        httpClientMock = Mockito.mock(HttpClient.class);
        profanityService = new ProfanityService(httpClientMock);
        ReflectionTestUtils.setField(profanityService, "endPoint", "https://gg-content-moderator.cognitiveservices.anImaginaryWebsite.com/");
        ReflectionTestUtils.setField(profanityService, "moderatorKey", "NotARealKey123");
    }

    @Test
    void moderateContent_ProfanityMatchesApiCall_CorrectApiCallResponse() throws IOException, InterruptedException {

        HttpResponse<String> mockHttpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(mockHttpResponse.body()).thenReturn("{\"OriginalText\":\"No bad input\",\"NormalizedText\":\" bad input\",\"Misrepresentation\":null,\"Language\":\"eng\",\"Terms\":[{\"Index\":7,\"OriginalIndex\":12,\"ListId\":0,\"Term\":\"BadWord\"}],\"Status\":{\"Code\":3000,\"Description\":\"OK\",\"Exception\":null},\"TrackingId\":\"e7b5c1ba-48cf-4b58-b3f1-41dce34ae0c5\"}");
        Mockito.when(httpClientMock.send(Mockito.any(), Mockito.eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(mockHttpResponse);
        String profanity_sentence = "Hello there John Doe";
        Assertions.assertEquals("{\"OriginalText\":\"No bad input\",\"NormalizedText\":\" bad input\",\"Misrepresentation\":null,\"Language\":\"eng\",\"Terms\":[{\"Index\":7,\"OriginalIndex\":12,\"ListId\":0,\"Term\":\"BadWord\"}],\"Status\":{\"Code\":3000,\"Description\":\"OK\",\"Exception\":null},\"TrackingId\":\"e7b5c1ba-48cf-4b58-b3f1-41dce34ae0c5\"}",
                profanityService.moderateContent(profanity_sentence));

    }

    @Test
    void containsProfanity_NoProfanityFound_returnsFalse() throws IOException, InterruptedException {
        HttpResponse<String> mockHttpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(mockHttpResponse.body()).thenReturn("{\"OriginalText\":\"No bad input\",\"NormalizedText\":\" bad input\",\"Misrepresentation\":null,\"Language\":\"eng\",\"Terms\":null,\"Status\":{\"Code\":3000,\"Description\":\"OK\",\"Exception\":null},\"TrackingId\":\"e7b5c1ba-48cf-4b58-b3f1-41dce34ae0c5\"}");
        Mockito.when(httpClientMock.send(Mockito.any(), Mockito.eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(mockHttpResponse);
        String profanity_sentence = "Hello there John Doe";
        Assertions.assertFalse(profanityService.containsProfanity(profanity_sentence));
    }

    @Test
    void containsProfanity_profanityFound_returnsTrue() throws IOException, InterruptedException {
        HttpResponse<String> mockHttpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(mockHttpResponse.body()).thenReturn("{\"OriginalText\":\"No bad input\",\"NormalizedText\":\" bad input\",\"Misrepresentation\":null,\"Language\":\"eng\",\"Terms\":[{\"Index\":7,\"OriginalIndex\":12,\"ListId\":0,\"Term\":\"BadWord\"}],\"Status\":{\"Code\":3000,\"Description\":\"OK\",\"Exception\":null},\"TrackingId\":\"e7b5c1ba-48cf-4b58-b3f1-41dce34ae0c5\"}");
        Mockito.when(httpClientMock.send(Mockito.any(), Mockito.eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(mockHttpResponse);
        String profanity_sentence = "Hello there John Doe";
        Assertions.assertTrue(profanityService.containsProfanity(profanity_sentence));

    }





}
