package nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidation;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * class for sending requests to profanity API and checking if any bad words are in the API return.
 */
public class LanguageFilterAPI {
    @Value("${profanity.access.token}")
    private String apiKey;
    /**
     * Sends post a post request to the bad words API and then returns a JSON reponse.
     * @param bodyContent The string for which profanity is checked.
     * @return A JSON string containing any bad words found, deviations and a censored version
     * @throws IOException If an error occurs while making request
     * @throws InterruptedException If request is interrupted
     */
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
    /**
     * Checks a returned json string to see if it contains any bad words, if so return True.
     * @param returnedString The string to check if the api found any bad words.
     * @return True if the json has a bad word count above 0, False if bad word count is 0.
     */
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
