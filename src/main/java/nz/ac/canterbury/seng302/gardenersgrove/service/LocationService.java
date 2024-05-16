package nz.ac.canterbury.seng302.gardenersgrove.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Service class for obtaining location autocomplete suggestions using the LocationIQ API.
 */
@Service
public class LocationService {

    @Value("${locationiq.access.token}")
    private String locationIqAccessToken;

    Logger logger = LoggerFactory.getLogger(LocationService.class);

    /**
     * Retrieves location suggestions based on provided query string.
     * @param query The string for which autocomplete suggestions are requested.
     * @return A JSON string containing location suggestions
     * @throws IOException If an error occurs while making request
     * @throws InterruptedException If request is interrupted
     */
    public String getLocationSuggestions(String query) throws IOException, InterruptedException {
;
        String encodedQuery = URLEncoder.encode(query, "UTF-8");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.locationiq.com/v1/autocomplete?q=" + encodedQuery + "&key=" + locationIqAccessToken))
                .header("accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        logger.info("Location API request, query = '" + query + "'");
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
