package nz.ac.canterbury.seng302.gardenersgrove.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nz.ac.canterbury.seng302.gardenersgrove.component.CacheableWeatherResponseData;
import nz.ac.canterbury.seng302.gardenersgrove.component.WeatherResponseData;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WeatherService {

    Logger logger = LoggerFactory.getLogger(WeatherService.class);

    ObjectMapper objectMapper = new ObjectMapper();

    private final AtomicLong nextFreeCallTimestamp = new AtomicLong(new Date().getTime());
    private static final long RATE_LIMIT_DELAY_MS = 100;
    private static final long RATE_LIMIT_DELAY_BUFFER = 5;

    private static final ConcurrentHashMap<String, CacheableWeatherResponseData> cachedWeatherResults = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<CacheableWeatherResponseData> linkedWeatherResults = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger retrievalsSinceCacheClean = new AtomicInteger(0);
    private static final Integer CACHE_CLEAN_THRESHOLD = 100;

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Make a location identifier for the weather data cache form a latitude and
     * longitude
     * 
     * @param latitude  the input latitude
     * @param longitude the input longitude
     * @return the location identifier or null if either lat or long inputted are
     *         empty.
     */
    private String makeCacheLocationIdentifier(String latitude, String longitude) {
        if (latitude.isEmpty() || longitude.isBlank()) {
            return null;
        }
        Double latitudeDouble = Double.parseDouble(latitude);
        Double longtitudeDouble = Double.parseDouble(longitude);

        return String.format("%.2f,%.2f", latitudeDouble, longtitudeDouble);
    }

    /**
     * check weather data cache for a specific locations
     * 
     * @param locationIdentifier the location identifier to check for
     * @return the correct weather data or null if not valid datt can be found
     */
    private WeatherResponseData getCachedResponse(String locationIdentifier) {
        if (locationIdentifier == null) {
            return null;
        }
        int currentCacheAge = retrievalsSinceCacheClean.addAndGet(1);

        if (currentCacheAge >= CACHE_CLEAN_THRESHOLD) {
            retrievalsSinceCacheClean.compareAndSet(currentCacheAge, 0);
            cleanOldCacheData();
        }

        CacheableWeatherResponseData cachedResponse;
        try {
            cachedResponse = cachedWeatherResults.get(locationIdentifier);
        } catch (NullPointerException nullPointerException) {
            return null;
        }

        if (cachedResponse == null) {
            return null;
        }

        if (!cachedResponse.isExpired()) {
            return cachedResponse.getWeatherResponseData();
        }

        cleanOldCacheData();
        return null;
    }

    /**
     * Store a weather response in this cache
     * 
     * @param locationIdentifier  the location identifier for the cached weather
     *                            data
     * @param weatherResponseData the weather data to cache
     */
    private void cacheWeatherResponse(String locationIdentifier, WeatherResponseData weatherResponseData) {
        CacheableWeatherResponseData cacheableWeatherResponseData = new CacheableWeatherResponseData(locationIdentifier,
                weatherResponseData);
        cachedWeatherResults.put(locationIdentifier, cacheableWeatherResponseData);
        linkedWeatherResults.add(cacheableWeatherResponseData);
    }

    /**
     * checks the oldest record in the cached weather data to see if it has expired
     * if it is expired, remove it and all other expired records in order
     * terminate when hitting the firs valid record or the end of the list
     * (whichever comes first)
     * 
     * @return the number of removed records
     */
    private int cleanOldCacheData() {
        logger.info("Cleaning old cache data");
        int clearedRecords = 0;
        try {
            while (linkedWeatherResults.peek().isExpired()) {
                try {
                    CacheableWeatherResponseData oldCachedData = linkedWeatherResults.poll();
                    cachedWeatherResults.remove(oldCachedData.getLocationIdentifier());
                    clearedRecords += 1;
                    logger.info("Removed {}", oldCachedData);
                } catch (NullPointerException nullPointerException) {
                    logger.info("Old cache record failed to finish clearing, cache may be empty");
                }
            }
            logger.info("Cleaned {} old weather data cache values", clearedRecords);
            logger.info("Hash table size {}", cachedWeatherResults.size());
            long oldestRecord = new Date().getTime() - linkedWeatherResults.peek().getCreationTime();
            logger.info("Oldest living records age should be in {} milliseconds",
                    oldestRecord);
        } catch (NullPointerException nullPointerException) {
            logger.warn("Weather data cache cleanup failed, cache empty.");
            logger.info("Hash table size {}", cachedWeatherResults.size());
        }
        return clearedRecords;
    }

    /**
     * Runs an API query to get the weather data for a specific location
     *
     * @param latitude  latitude of the target location
     * @param longitude longitude of the target location
     * @return WeatherResponseData for the given location
     * @throws InterruptedException
     * @throws IOException
     */
    public WeatherResponseData getWeather(String latitude, String longitude) {
        WeatherResponseData cachedResponse = getCachedResponse(makeCacheLocationIdentifier(latitude, longitude));
        if (latitude.isEmpty() || longitude.isEmpty()) {
            return null;
        }

        if (cachedResponse != null) {
            logger.info("Using cached weather response");
            return cachedResponse;
        }

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
            logger.info(request.uri().toString());
            HttpResponse<String> response = HttpClient.newHttpClient().send(request,
                    HttpResponse.BodyHandlers.ofString());

            JsonNode jsonObject = objectMapper.readTree(response.body());
            logger.info("weather retrieval successful");
            WeatherResponseData retrievedWeatherData = new WeatherResponseData(jsonObject);
            cacheWeatherResponse(makeCacheLocationIdentifier(latitude, longitude), retrievedWeatherData);
            return retrievedWeatherData;

        } catch (InterruptedException weatherApiError) {
            logger.error(weatherApiError.toString());
            Thread.currentThread().interrupt();

        } catch (IOException error) {
            logger.error(error.toString());
        }
        return null;
    }

    /**
     * Run through the locations of all gardens and check which ones do not have a
     * location closed to them cached
     * run a garden weather query for every garden without a cached response
     * then return weather data for every garden from cache
     *
     * @param gardens list of gardens
     * @return a list of weather data for gardens
     * @throws InterruptedException
     */
    public List<WeatherResponseData> getWeatherForGardens(List<Garden> gardens) {

        HashMap<String, CompletableFuture<WeatherResponseData>> distinctWeatherQueries = new HashMap<>();
        for (Garden garden : gardens) {
            String locationId = makeCacheLocationIdentifier(garden.getGardenLatitude(), garden.getGardenLongitude());
            if (locationId != null && (!cachedWeatherResults.containsKey(locationId)
                    || cachedWeatherResults.get(locationId).isExpired())) {
                distinctWeatherQueries.computeIfAbsent(locationId, key -> fetchWeatherData(garden));
            }
        }
        distinctWeatherQueries.forEach((key, weatherFuture) -> cacheWeatherResponse(key, weatherFuture.join()));

        // resolves the completableFutures (waits for future to resolve if needed)
        return gardens.stream()
                .map((garden -> makeCacheLocationIdentifier(garden.getGardenLatitude(), garden.getGardenLongitude())))
                .map(this::getCachedResponse)
                .toList();
    }

    /**
     * Fetches weather data through series of HTTP requests
     *
     * @param garden the garden that's weather data is being fetched
     * @return async request
     * @throws InterruptedException
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
     * handles the http response from the api, tries to return that response as
     * weather response data
     * to read weather info from
     *
     * @param response response from the weather
     * @param garden   the garden needing the response data
     * @return JSON that holds weather data
     */
    private WeatherResponseData handleHttpResponse(HttpResponse<String> response, Garden garden) {
        try {
            JsonNode jsonObject = objectMapper.readTree(response.body());
            return new WeatherResponseData(jsonObject);
        } catch (Exception e) {
            logger.error("Error parsing weather data for garden {}: {}", garden.getGardenId(), e.toString());
            return null;
        }
    }

    /**
     * Handles weather API rate limiting, when called instructs thread to wait for
     * next available time slot.
     */
    private void waitForRateLimit() {
        long timeToWait = 0;
        boolean couldUpdate = false;
        while (!couldUpdate) {
            long nextCallSlot = nextFreeCallTimestamp.get();
            if (nextCallSlot < new Date().getTime()) {
                timeToWait = 0;
                couldUpdate = nextFreeCallTimestamp.compareAndSet(nextCallSlot,
                        (new Date().getTime() + RATE_LIMIT_DELAY_BUFFER + RATE_LIMIT_DELAY_MS));
            } else {
                timeToWait = nextCallSlot - new Date().getTime();
                couldUpdate = nextFreeCallTimestamp.compareAndSet(nextCallSlot,
                        (nextCallSlot + RATE_LIMIT_DELAY_BUFFER + RATE_LIMIT_DELAY_MS));
            }
        }
        try {
            Thread.sleep(timeToWait);
            logger.info("Weather api thread woke to make request after {}", timeToWait);
        } catch (InterruptedException exception) {
            logger.error("Thread interrupted while waiting for weather api rate limit");
            Thread.currentThread().interrupt();
        }

    }

}
