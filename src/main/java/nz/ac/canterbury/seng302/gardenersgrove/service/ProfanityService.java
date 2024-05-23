package nz.ac.canterbury.seng302.gardenersgrove.service;

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

    private HttpClient httpClient;

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
     * @return A JSON string containing any bad words found, deviations and a
     *         censored version
     * @throws IOException          If an error occurs while making request
     * @throws InterruptedException If request is interrupted
     */
    public String moderateContent(String content) throws IOException, InterruptedException {

        String encodedContent = URLEncoder.encode(content, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endPoint + "/contentmoderator/moderate/v1.0/ProcessText/Screen?text=" + encodedContent))
                .header("Content-Type", "text/plain")
                .header("Ocp-Apim-Subscription-Key", moderatorKey)
                .POST(HttpRequest.BodyPublishers.ofString(content))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
    /**
     * Checks a returned json string to see if it contains any bad words, if so return True.
     * @param returnedString The string to check if the api found any bad words.
     * @return True if the json contains terms 0, False if bad word count is 0.
     */
    public boolean containsProfanity (String returnedString) {
        String termsIndicator = "\"Terms\":[{";
        return returnedString.contains(termsIndicator);
    }
}
