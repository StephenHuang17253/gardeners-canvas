package nz.ac.canterbury.seng302.gardenersgrove.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class LocationService {

    @Value("${location.iq.access.token}")
    private String locationIqAccessToken;

    public String getLocationSuggestions(String query) throws IOException, InterruptedException {
;
        String encodedQuery = URLEncoder.encode(query, "UTF-8");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.locationiq.com/v1/autocomplete?q=" + encodedQuery + "&key=" + locationIqAccessToken))
                .header("accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        System.out.println("https://api.locationiq.com/v1/autocomplete?q=" + query + "&key=" + locationIqAccessToken);
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        // System.out.println(response.body());
        return response.body();
    }
}
