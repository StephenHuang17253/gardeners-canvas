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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

@Service
public class WeatherService {

    Logger logger = LoggerFactory.getLogger(WeatherService.class);

    ObjectMapper objectMapper = new ObjectMapper();

    private static final int MAX_REQUESTS_PER_SECOND = 10;

    private volatile long lastRequestTime = Instant.now().getEpochSecond();

    private final Semaphore semaphore = new Semaphore(MAX_REQUESTS_PER_SECOND);

    private static final HttpClient httpClient = HttpClient.newHttpClient();

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
        if (timeElapsed >= 1) {
            semaphore.drainPermits();
            semaphore.release(MAX_REQUESTS_PER_SECOND);
            logger.info("A second or more has elapsed, permits reset to: {}", semaphore.availablePermits());
            lastRequestTime = currentTime;
        }

        logger.info("Permits left before request: {}", semaphore.availablePermits());

        if (!semaphore.tryAcquire()) {
            logger.info("Exceeded location API rate limit of 2 requests per second.");
            throw new UnavailableException("429");
        }
        logger.info("Permits left after request: {}", semaphore.availablePermits());
        return getWeather(gardenLatitude, gardenLongitude);
    }

    /**
     * Processes multiple weather requests in chunks of gardens
     *
     * @param gardens list of gardens
     * @return a list of weather data for gardens
     * @throws UnavailableException
     */
    public List<WeatherResponseData> getWeatherForGardens(List<Garden> gardens) throws UnavailableException {
        List<WeatherResponseData> weatherDataList = new ArrayList<>();
        List<List<Garden>> gardenChunks = chunkGardens(gardens, MAX_REQUESTS_PER_SECOND - 1);

        for (List<Garden> chunk : gardenChunks) {
            processChunk(chunk, weatherDataList);
            delayBetweenChunks();
        }

        logger.info(String.valueOf(weatherDataList.size()));

        return weatherDataList;
    }


    /**
     * Process chunks of gardens, adds weatherdata to the list
     *
     * @param chunk sublist of gardens that will have their weather retrieved together
     * @param weatherDataList list of weather data
     * @throws UnavailableException
     */
    private void processChunk(List<Garden> chunk, List<WeatherResponseData> weatherDataList) throws UnavailableException {
        resetPermitsIfNeeded();

        if (chunk.size() > semaphore.availablePermits()) {
            logger.info("Exceeded weather API rate limit for batch request.");
            throw new UnavailableException("429");
        }

        List<CompletableFuture<WeatherResponseData>> futures = chunk.stream()
                .map(this::fetchWeatherData)
                .toList();

        weatherDataList.addAll(futures.stream()
                .map(CompletableFuture::join)
                .toList());
    }

    /**
     * Fetches weather data through series of HTTP requests
     *
     * @param garden the garden that's weather data is being fetched
     * @return async request
     */
    private CompletableFuture<WeatherResponseData> fetchWeatherData(Garden garden) {
        semaphore.acquireUninterruptibly();
        logger.info("Permits left after request for garden {}: {}", garden.getGardenId(), semaphore.availablePermits());

        HttpRequest request = buildHttpRequest(garden);

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> handleHttpResponse(response, garden))
                .whenComplete((response, ex) -> semaphore.release());
    }

    /**
     * Splits users gardens into chunks, default size = 9
     *
     * @param gardens list of gardens
     * @param chunkSize size of chunk to split the gardens into sublists of that length
     * @return 2-dimensional list of chunks of gardens
     */
    private List<List<Garden>> chunkGardens(List<Garden> gardens, int chunkSize) {
        List<List<Garden>> chunks = new ArrayList<>();
        for (int i = 0; i < gardens.size(); i += chunkSize) {
            chunks.add(gardens.subList(i, Math.min(gardens.size(), i + chunkSize)));
        }
        return chunks;
    }

    /**
     * Delays the chunk weather requests to avoid running out of permits
     *
     * @throws UnavailableException
     */
    private void delayBetweenChunks() throws UnavailableException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UnavailableException("Thread interrupted");
        }
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
     * rate limit is 10 calls per second, so if a second has passed this will reset the available permits
     */
    private void resetPermitsIfNeeded() {
        long currentTime = Instant.now().getEpochSecond();
        long timeElapsed = currentTime - lastRequestTime;
        logger.info("Time elapsed: {}", timeElapsed);

        if (timeElapsed >= 1) {
            semaphore.drainPermits();
            semaphore.release(MAX_REQUESTS_PER_SECOND);
            logger.info("A second or more has elapsed, permits reset to: {}", semaphore.availablePermits());
            lastRequestTime = currentTime;
        }

        logger.info("Permits left before request: {}", semaphore.availablePermits());
    }

}
