package nz.ac.canterbury.seng302.gardenersgrove.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.UnavailableException;
import nz.ac.canterbury.seng302.gardenersgrove.component.DailyWeather;
import nz.ac.canterbury.seng302.gardenersgrove.component.WeatherResponseData;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;


/**
 * This service class communicates with the weather API and converts response to useful formats
 */
@Service
public class WeatherService {

    Logger logger = LoggerFactory.getLogger(WeatherService.class);

    ObjectMapper objectMapper = new ObjectMapper();

    private static final int MAX_REQUESTS_PER_SECOND = 10;

    private volatile long lastRequestTime = Instant.now().getEpochSecond();

    private final Semaphore semaphore = new Semaphore(MAX_REQUESTS_PER_SECOND);


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
                + "&current=temperature_2m,relative_humidity_2m,precipitation,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum&timezone=auto&past_days=2";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode jsonObject = objectMapper.readTree(response.body());
            logger.info("weather: " + jsonObject);
            return new WeatherResponseData(jsonObject);

        } catch (Exception weatherApiError) {
            logger.error(weatherApiError.toString());
        }
        return null;
    }

    /**
     * Helper method for getting weather data for a garden
     *
     * @param garden Garden entity of
     * @return list of DailyWeather components
     */
    public List<DailyWeather> getGardenWeatherData(Garden garden) {
        List<DailyWeather> weatherList = new ArrayList<>();
        DailyWeather noWeather = null;
        try {
            WeatherResponseData gardenWeather = showGardenWeather(garden.getGardenLatitude(),
                    garden.getGardenLongitude());
            weatherList.addAll(gardenWeather.getRetrievedWeatherData());
        } catch (NullPointerException error) {
            noWeather = new DailyWeather("no_weather_available_icon.png", null, null);
            noWeather.setError("Location not found, please update your location to see the weather");
        } catch (UnavailableException e) {
            noWeather = new DailyWeather("not_found.png", null, null);
        }

        if (noWeather != null) {
            weatherList.add(noWeather);
        }

        return weatherList;
    }

    WeatherResponseData showGardenWeather(String gardenLatitude, String gardenLongitude) throws UnavailableException {

        long currentTime = Instant.now().getEpochSecond();
        long timeElapsed = currentTime - lastRequestTime;

        logger.info("Time elapsed: {}", timeElapsed);
        // Every second, the number of available permits is reset to 2
        if (timeElapsed >= 1) {
            semaphore.drainPermits();
            semaphore.release(MAX_REQUESTS_PER_SECOND);
            logger.info("A second or more has elapsed, permits reset to: {}", semaphore.availablePermits());
            lastRequestTime = currentTime;
        }

        logger.info("Permits left before request: {}", semaphore.availablePermits());

        // Check if rate limit exceeded
        if (!semaphore.tryAcquire()) {
            logger.info("Exceeded location API rate limit of 2 requests per second.");
            throw new UnavailableException("429");
        }
        logger.info("Permits left after request: {}", semaphore.availablePermits());
        return getWeather(gardenLatitude, gardenLongitude);

    }


}
