package nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidation;
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

    public String sendPostRequest(String bodyContent) throws IOException, InterruptedException {
        String requestBody = "{body: \"" + URLEncoder.encode(bodyContent, StandardCharsets.UTF_8) + "\"}";
        String url = "https://api.apilayer.com/bad_words?censor_character=" + URLEncoder.encode("*", StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    public boolean containsProfanity (String returnedString) {
        int index = returnedString.indexOf("\"bad_words_total\":");
        if (index != -1) {
            String fromBadWords = returnedString.substring(index);
            String numberStr = fromBadWords.substring(18, fromBadWords.indexOf(',')).trim();
            int badWordsTotal = Integer.parseInt(numberStr);
            return badWordsTotal > 0;

        } else {
            System.out.println("Cannot phase return string");
            return false;
        }
    }
}
