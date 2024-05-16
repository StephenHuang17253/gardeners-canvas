package nz.ac.canterbury.seng302.gardenersgrove.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import nz.ac.canterbury.seng302.gardenersgrove.entity.WeatherResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;


/**
 * This service class communicates with the weather API and converts response to useful formats
 */
@Service
public class WeatherService {

    Logger logger = LoggerFactory.getLogger(WeatherService.class);

    ObjectMapper objectMapper = new ObjectMapper();

    JsonNode current;
    JsonNode daily;

    JsonNode tempMax;
    JsonNode tempMin;
    JsonNode dailyWeatherCodes;
    JsonNode dailyRain;
    JsonNode dates;


    /**
     * This method calls the Open-Meteo.com API, and receives the response as a JSON object.
     *
     * @param gardenLatitude  - the latitude of the user's garden
     * @param gardenLongitude - the longitude of the user's garden
     * @return the response from the API
     */

    public ArrayList<JsonNode> getWeather(String gardenLatitude, String gardenLongitude) {
        String url = "https://api.open-meteo.com/v1/forecast?latitude="
                + gardenLatitude
                + "&longitude=" + gardenLongitude
                + "&current=temperature_2m,relative_humidity_2m,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min,rain_sum&timezone=auto&past_days=2";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode jsonObject = objectMapper.readTree(response.body());

            current = jsonObject.get("current");
            daily = jsonObject.get("daily");

            getDailyWeather();
            ArrayList<JsonNode> weatherList = new ArrayList<>();
            weatherList.add(getPastWeather());
            weatherList.add(getCurrentWeather());
            weatherList.add(getForecast());

            logger.info("weather: " + String.valueOf(jsonObject));

            return weatherList;

        } catch (Exception e) {
            logger.error(e.toString());
        }
        return null;
    }

    public JsonNode getCurrentWeather() {
        return current;
    }

    public double getCurrentTemperature() {
        return current.get("temperature_2m").asDouble();
    }

    public double getCurrentHumidity() {
        return current.get("relative_humidity_2m").asDouble();
    }

    public int getCurrentWeatherCode() {
        return current.get("weather_code").asInt();
    }

    /**
     * This method separates the API response for daily weather into specific JsonNodes
     * @return the daily section of the API response as a JsonNode
     */
    public JsonNode getDailyWeather() {
        tempMax =  daily.get("temperature_2m_max");
        tempMin = daily.get("temperature_2m_min");
        dailyWeatherCodes = daily.get("weather_code");
        dailyRain = daily.get("rain_sum");
        dates = daily.get("time");

        return daily;
    }

    /**
     * This method collects the API response for the past 2 days from daily weather JsonNode
     * @return the past weather section of the daily weather as JsonNode
     */
    public JsonNode getPastWeather() {
        WeatherResponseData pastWeather = new WeatherResponseData();

        for (int i = 0; i < 2; i++) {
            addValuesToWeatherData(pastWeather, i);
        }
        logger.info(pastWeather.toString());

        return pastWeather.getWeather();
    }

    /**
     * This method collects the API response for the next 7 days from daily weather JsonNode
     * @return the forecast weather section of the daily weather as JsonNode
     */
    public JsonNode getForecast() {

        WeatherResponseData forecastWeather = new WeatherResponseData();
        for (int i = 2; i < tempMax.size(); i++) {
            addValuesToWeatherData(forecastWeather, i);
        }
        logger.info(forecastWeather.toString());

        return forecastWeather.getWeather();
    }

    /**
     * This helper method adds values to the specific data sections of a WeatherResponseData entity.
     * @param weather - the WeatherResponseData entity
     * @param i - the index to be parsed into each array node.
     */
    private void addValuesToWeatherData(WeatherResponseData weather, int i) {
        weather.addTempMax(tempMax.get(i).asDouble());
        weather.addTempMin(tempMin.get(i).asDouble());
        weather.addWeatherCode(dailyWeatherCodes.get(i).asInt());
        weather.addRain(dailyRain.get(i).asDouble());
        weather.addDate(dates.get(i).toString());
    }

}
