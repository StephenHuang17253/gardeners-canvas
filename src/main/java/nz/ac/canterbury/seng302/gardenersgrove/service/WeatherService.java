package nz.ac.canterbury.seng302.gardenersgrove.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import nz.ac.canterbury.seng302.gardenersgrove.component.DailyWeather;
import nz.ac.canterbury.seng302.gardenersgrove.component.WeatherResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


/**
 * This service class communicates with the weather API and converts response to useful formats
 */
@Service
public class WeatherService {

    Logger logger = LoggerFactory.getLogger(WeatherService.class);

    ObjectMapper objectMapper = new ObjectMapper();


    /**
     * This method calls the Open-Meteo.com API, and receives the response as a JSON object.
     *
     * @param gardenLatitude  - the latitude of the user's garden
     * @param gardenLongitude - the longitude of the user's garden
     * @return the response from the API
     */

    public WeatherResponseData getWeather(String gardenLatitude, String gardenLongitude) {
        String url = "https://api.open-meteo.com/v1/forecast?latitude="
                + gardenLatitude
                + "&longitude=" + gardenLongitude
                + "&current=temperature_2m,relative_humidity_2m,precipitation,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum&past_days=2";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode jsonObject = objectMapper.readTree(response.body());
            WeatherResponseData weatherData = new WeatherResponseData(jsonObject);
            logger.info("weather: " + String.valueOf(jsonObject));
            return weatherData;

        } catch (Exception e) {
            logger.error(e.toString());
        }
        return null;
    }




}
