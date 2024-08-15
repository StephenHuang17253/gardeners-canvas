package nz.ac.canterbury.seng302.gardenersgrove.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nz.ac.canterbury.seng302.gardenersgrove.component.CacheableWeatherResponseData;
import nz.ac.canterbury.seng302.gardenersgrove.component.WeatherResponseData;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    private static AtomicInteger retreivalsSinceCacheClean = new AtomicInteger(0);
    private static final Integer CACHE_CLEAN_THRESHOLD = 100;

    private static final HttpClient httpClient = HttpClient.newHttpClient();


    private String makeCacheLocationIdentifier(String latitude, String longitude)
    {
        Double latitdueDouble = Double.parseDouble(latitude);
        Double longtitudeDouble = Double.parseDouble(longitude);

        return String.format("%.2f,%.2f",latitdueDouble,longtitudeDouble);
    }

    private WeatherResponseData getCachedResponse(String locationIdentifier)
    {
        int currentCacheAge = retreivalsSinceCacheClean.addAndGet(1);
        if (currentCacheAge >= CACHE_CLEAN_THRESHOLD)
        {
            retreivalsSinceCacheClean.compareAndSet(currentCacheAge,0);
            cleanOldCacheData();
        }

        CacheableWeatherResponseData cachedResponse;
        try
        {
            cachedResponse =  cachedWeatherResults.get(locationIdentifier);
        }
        catch (NullPointerException nullPointerException)
        {
            return null;
        }
        if(cachedResponse == null)
        {
            return null;
        }
        else
        {
            if (!cachedResponse.isExpired())
            {
                return cachedResponse.getWeatherResponseData();
            }
            else
            {
                return null;
            }

        }
    }

    private void cacheWeatherResponse(String locationIdentifier, WeatherResponseData weatherResponseData)
    {
        CacheableWeatherResponseData cacheableWeatherResponseData = new CacheableWeatherResponseData(locationIdentifier,weatherResponseData);
        cachedWeatherResults.put(locationIdentifier,cacheableWeatherResponseData);
        linkedWeatherResults.add(cacheableWeatherResponseData);
    }

    private int cleanOldCacheData()
    {
        logger.info("Cleaning old cache data");
        int clearedRecords = 0;
        try
        {
            while (linkedWeatherResults.peek().isExpired())
            {
                try
                {
                    CacheableWeatherResponseData oldCachedData = linkedWeatherResults.poll();
                    cachedWeatherResults.remove(oldCachedData.getLocationIdentifier());
                    clearedRecords += 1;
                    logger.info("removed {}",oldCachedData);
                }
                catch (NullPointerException nullPointerException)
                {
                    logger.warn("Old cache record failed to clear");
                }
            }
            logger.info("cache cleaning complete");
            logger.debug("Hash table size {}", cachedWeatherResults.size());
            logger.debug("Oldest living records ageshould be in {} mili seconds",new Date().getTime() - linkedWeatherResults.peek().getCreationTime());
        }
        catch (NullPointerException nullPointerException)
        {
            logger.warn("Weather data cache cleanup failed, cache empty.");
            logger.debug("Hash table size {}",cachedWeatherResults.size());
        }
        return clearedRecords;

    }


    /**
     * Runs an API query to get the weather data for a specific location
     *
     * @param latitude  latitude of the target location
     * @param longitude longitude of the target location
     * @return WeatherResponseData for the given location
     */
    public WeatherResponseData getWeather(String latitude, String longitude) {
        WeatherResponseData cachedResponse = getCachedResponse(makeCacheLocationIdentifier(latitude,longitude));
        if (cachedResponse != null)
        {
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
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode jsonObject = objectMapper.readTree(response.body());
            logger.info("weather retrieval successful");
            WeatherResponseData retrievedWeatherData = new WeatherResponseData(jsonObject);
            cacheWeatherResponse(makeCacheLocationIdentifier(latitude,longitude),retrievedWeatherData);
            return retrievedWeatherData;

        } catch (Exception weatherApiError) {
            logger.error(weatherApiError.toString());
        }
        return null;
    }


    /**
     * Gathers weather data for a list of gardens at a time
     *
     * @param gardens list of gardens
     * @return a list of weather data for gardens
     */
    public List<WeatherResponseData> getWeatherForGardens(List<Garden> gardens) {

        HashMap<String,CompletableFuture<WeatherResponseData>> distinctWeatherQueries = new HashMap<>();
        for (Garden garden: gardens)
        {
            String locationId = makeCacheLocationIdentifier(garden.getGardenLatitude(),garden.getGardenLongitude());
            if ((!cachedWeatherResults.containsKey(locationId) || cachedWeatherResults.get(locationId).isExpired()))
            {
                if(!distinctWeatherQueries.containsKey(locationId))
                {
                    distinctWeatherQueries.put(locationId,fetchWeatherData(garden));
                }
            }
        }
        distinctWeatherQueries.forEach((key,weatherFuture) -> cacheWeatherResponse(key,weatherFuture.join()));


        // resolves the completableFutures (waits for future to resolve if needed)
        return new ArrayList<>(gardens.stream()
                .map((garden -> makeCacheLocationIdentifier(garden.getGardenLatitude(), garden.getGardenLongitude())))
                .map(this::getCachedResponse)
                .toList());
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
        try {
            Thread.sleep(timeToWait);
            logger.info("Weather api thread woke to make request after {}", timeToWait);
        } catch (InterruptedException exception) {
            logger.error("Thread interrupted while waiting for weather api rate limit");
        }

    }

}
