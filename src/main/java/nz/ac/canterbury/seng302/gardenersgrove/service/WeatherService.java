package nz.ac.canterbury.seng302.gardenersgrove.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class WeatherService {

    Logger logger = LoggerFactory.getLogger(WeatherService.class);

    ObjectMapper objectMapper = new ObjectMapper();

    private final AtomicLong nextFreeCallTimestamp = new AtomicLong(new Date().getTime());
    private static final long RATE_LIMIT_DELAY_MS = 100;
    private static final long RATE_LIMIT_DELAY_BUFFER = 5;

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public WeatherResponseData getWeather(String latitude, String longitude) {
        waitForRateLimit();
        String url = "https://api.open-meteo.com/v1/forecast?latitude="
                + latitude
                + "&longitude=" + longitude
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

//    public List<DailyWeather> getGardenWeatherData(Garden garden) {
//        List<DailyWeather> weatherList = new ArrayList<>();
//        DailyWeather noWeather = null;
//        try {
//            WeatherResponseData gardenWeather = getWeather(garden.getGardenLatitude(),
//                    garden.getGardenLongitude());
//            weatherList.addAll(gardenWeather.getRetrievedWeatherData());
//        } catch (NullPointerException error) {
//            noWeather = new DailyWeather("no_weather_available_icon.png", null, null);
//            noWeather.setError("Location not found, please update your location to see the weather");
//        } catch (UnavailableException e) {
//            noWeather = new DailyWeather("not_found.png", null, null);
//        }
//
//        if (noWeather != null) {
//            weatherList.add(noWeather);
//        }
//
//        return weatherList;
//    }


    /**
     * Processes multiple weather requests in chunks of gardens
     *
     * @param gardens list of gardens
     * @return a list of weather data for gardens
     * @throws UnavailableException
     */
    public List<WeatherResponseData> getWeatherForGardens(List<Garden> gardens) throws UnavailableException {

        return gardens.stream()
                .map(this::fetchWeatherData)
                .map(CompletableFuture::join)
                .toList();
    }



    /**
     * Fetches weather data through series of HTTP requests
     *
     * @param garden the garden that's weather data is being fetched
     * @return async request
     */
    private CompletableFuture<WeatherResponseData> fetchWeatherData(Garden garden) {
        waitForRateLimit();
        HttpRequest request = buildHttpRequest(garden);

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> handleHttpResponse(response, garden));
    }



    /**
     * Constructs an http request for the open-meteo weather api
     *
     * @param garden the garden the request is built for
     * @return HTTP request
     */
    private HttpRequest buildHttpRequest(Garden garden) {
        String url = "https://api.open-meteo.com/v1/forecast?latitude="
                + garden.getGardenLatitude()
                + "&longitude=" + garden.getGardenLongitude()
                + "&current=temperature_2m,relative_humidity_2m,precipitation,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum&timezone=auto&past_days=2";

        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
    }

    /**
     * handles the http response from the api, tries to return that response as weather response data
     * to read weather info from
     *
     * @param response response from the weather
     * @param garden the garden needing the response data
     * @return JSON that holds weather data
     */
    private WeatherResponseData handleHttpResponse(HttpResponse<String> response, Garden garden) {
        try {
            JsonNode jsonObject = objectMapper.readTree(response.body());
            logger.info("Weather for garden {}: {}", garden.getGardenId(), jsonObject);
            return new WeatherResponseData(jsonObject);
        } catch (Exception e) {
            logger.error("Error parsing weather data for garden {}: {}", garden.getGardenId(), e.toString());
            return null;
        }
    }

    /**
     * Handles weather API rate limiting, when called instructs thread to wait for next available time slot.
     */
    private void waitForRateLimit() {
        long timeToWait = 0;
        boolean couldUpdate = false;
        while (!couldUpdate) {
            long nextCallSlot = nextFreeCallTimestamp.get();
            if (nextCallSlot < new Date().getTime()) {
                timeToWait = 0;
                couldUpdate = nextFreeCallTimestamp.compareAndSet(nextCallSlot, (new Date().getTime() + RATE_LIMIT_DELAY_BUFFER + RATE_LIMIT_DELAY_MS));
            } else {
                timeToWait = nextCallSlot - new Date().getTime();
                couldUpdate = nextFreeCallTimestamp.compareAndSet(nextCallSlot, (nextCallSlot + RATE_LIMIT_DELAY_BUFFER + RATE_LIMIT_DELAY_MS));
            }
        }
        try
        {
            Thread.sleep(timeToWait);
            logger.info("Weather api thread woke to make request after {}",timeToWait);
        }
        catch (InterruptedException exception)
        {
            logger.error("Thread interrupted while waiting for weather api rate limit");
        }

    }

}
