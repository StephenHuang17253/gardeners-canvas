package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.http.HttpClient;

public class ProfanityServiceTest {

    private ProfanityService profanityService;
    HttpClient httpClientMock;
    @BeforeEach
    void init()
    {
        httpClientMock = Mockito.mock(HttpClient.class);
        profanityService = new ProfanityService(httpClientMock);
    }

    @Test
    void moderateContent_NoProfanityFound_BadWordListed()
    {
        String profanity_sentence = "Hello there John Doe";
        Mockito.doReturn("{\"OriginalText\":\"No bad input\",\"NormalizedText\":\" bad input\",\"Misrepresentation\":null,\"Language\":\"eng\",\"Terms\":[{\"Index\":7,\"OriginalIndex\":12,\"ListId\":0,\"Term\":\"BadWord\"}],\"Status\":{\"Code\":3000,\"Description\":\"OK\",\"Exception\":null},\"TrackingId\":\"e7b5c1ba-48cf-4b58-b3f1-41dce34ae0c5\"}")
                .when(profanityService).containsProfanity(Mockito.anyString());



    }

    @Test
    void moderateContent_profanityFound_noProfanityListed()
    {
        String profanity_sentence = "Hello there BadWord";
        Mockito.doReturn("{\"OriginalText\":\"No bad input\",\"NormalizedText\":\" bad input\",\"Misrepresentation\":null,\"Language\":\"eng\",\"Terms\":[{\"Index\":7,\"OriginalIndex\":12,\"ListId\":0,\"Term\":\"BadWord\"}],\"Status\":{\"Code\":3000,\"Description\":\"OK\",\"Exception\":null},\"TrackingId\":\"e7b5c1ba-48cf-4b58-b3f1-41dce34ae0c5\"}")
                .when(profanityService).containsProfanity(Mockito.anyString());


    }

    @Test
    void containsProfanity_NoProfanityFound_returnsFalse()
    {
        String profanity_sentence = "Hello there John Doe";
        Mockito.doReturn("{\"OriginalText\":\"No bad input\",\"NormalizedText\":\" bad input\",\"Misrepresentation\":null,\"Language\":\"eng\",\"Terms\":[{\"Index\":7,\"OriginalIndex\":12,\"ListId\":0,\"Term\":\"BadWord\"}],\"Status\":{\"Code\":3000,\"Description\":\"OK\",\"Exception\":null},\"TrackingId\":\"e7b5c1ba-48cf-4b58-b3f1-41dce34ae0c5\"}")
                .when(profanityService).containsProfanity(Mockito.anyString());


    }

    @Test
    void containsProfanity_profanityFound_returnsTrue()
    {
        String profanity_sentence = "Hello there BadWord";
        Mockito.doReturn("{\"OriginalText\":\"No bad input\",\"NormalizedText\":\" bad input\",\"Misrepresentation\":null,\"Language\":\"eng\",\"Terms\":[{\"Index\":7,\"OriginalIndex\":12,\"ListId\":0,\"Term\":\"BadWord\"}],\"Status\":{\"Code\":3000,\"Description\":\"OK\",\"Exception\":null},\"TrackingId\":\"e7b5c1ba-48cf-4b58-b3f1-41dce34ae0c5\"}")
                .when(profanityService).containsProfanity(Mockito.anyString());


    }





}
