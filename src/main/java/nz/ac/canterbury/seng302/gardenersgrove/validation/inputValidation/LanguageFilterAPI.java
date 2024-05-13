package nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidation;
import nz.ac.canterbury.seng302.gardenersgrove.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LanguageFilterAPI {
    @Value("${profanity.access.token}")
    private String apiKey;
    Logger logger = LoggerFactory.getLogger(LocationService.class);

    public String containsProfanity(String query) throws IOException, InterruptedException {
        String requestBody = "{body: \"" + URLEncoder.encode(query, "UTF-8") + "\"}";
        String url = "https://api.apilayer.com/bad_words?censor_character={*}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("accept", "application/json")
                .header("apikey", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }
}
