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
import java.nio.charset.StandardCharsets;

public class LanguageFilterAPI {
    @Value("${profanity.access.token}")
    private String apiKey;
    Logger logger = LoggerFactory.getLogger(LocationService.class);

    public boolean containsProfanity(String query) throws IOException, InterruptedException {
        String requestBody = "{body: \"" + URLEncoder.encode(query, StandardCharsets.UTF_8) + "\"}";
        String url = "https://api.apilayer.com/bad_words?censor_character=" + URLEncoder.encode("*", StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String resultString = response.body();

        int index = resultString.indexOf("\"bad_words_total\":");
        if (index != -1) {
            String fromBadWords = resultString.substring(index);
            String numberStr = fromBadWords.substring(18, fromBadWords.indexOf(',')).trim();
            int badWordsTotal = Integer.parseInt(numberStr);
            return badWordsTotal > 0;

        } else {
            System.out.println("Cannot phase return string");
            return false;
        }
    }
}
