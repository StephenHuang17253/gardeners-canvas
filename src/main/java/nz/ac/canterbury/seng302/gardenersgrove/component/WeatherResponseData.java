package nz.ac.canterbury.seng302.gardenersgrove.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jdk.jshell.spi.ExecutionControl;

import java.util.ArrayList;
import java.util.List;

/**
 * This entity class is a collection of weather data
 * Note: This is about past, current and future weather data
 */
public class WeatherResponseData {

    ObjectMapper mapper = new ObjectMapper();
    JsonNode weather = mapper.createObjectNode();
    ArrayNode tempMax = mapper.createArrayNode();
    ArrayNode tempMin =  mapper.createArrayNode();
    ArrayNode weatherCodes = mapper.createArrayNode();
    ArrayNode rain  = mapper.createArrayNode();
    ArrayNode dates  = mapper.createArrayNode();



    JsonNode current;
    JsonNode daily;

    JsonNode jsonTempMax;
    JsonNode jsonTempMin;
    JsonNode jsonDailyWeatherCodes;
    JsonNode jsonDailyRain;
    JsonNode jsonDates;

    public WeatherResponseData(JsonNode jsonWeatherData) {
        current = jsonWeatherData.get("current");
        daily = jsonWeatherData.get("daily");

        setDailyWeather();

        WeatherResponseData forecastData = getForecast();
        List<DailyWeather> weatherList = forecastData.getDailyWeather();
        weatherList.add(0, getCurrentWeather());



    }

    public void addTempMax(Double temp) {
        this.tempMax.add(temp);
    }

    public void addTempMin(Double temp) {
        this.tempMin.add(temp);
    }

    public void addWeatherCode(Integer code) {
        this.weatherCodes.add(code);
    }

    public void addRain(Double rain) {
        this.rain.add(rain);
    }

    public void addDate(String date) {
        this.dates.add(date);
    }

    public List<DailyWeather> getDailyWeather() {
       List<DailyWeather> days = new ArrayList<>();
       return ExecutionControl.NotImplementedException;
    }

    /**
     * getWeather() creates and returns the weather as a JsonNode Object
     * @return weather - a JsonNode of the weather data
     */
    public JsonNode getWeather() {

        ((ObjectNode) weather).put("temperature_max", tempMax);
        ((ObjectNode) weather).put("temperature_min", tempMin);
        ((ObjectNode) weather).put("weather_code", weatherCodes);
        ((ObjectNode) weather).put("rain", rain);
        ((ObjectNode) weather).put("date", dates);

        return weather;
    }

    /**
     * This method separates the API response for daily weather into specific JsonNodes
     * @return the daily section of the API response as a JsonNode
     */
    public JsonNode setDailyWeather() {
        jsonTempMax =  daily.get("temperature_2m_max");
        jsonTempMin = daily.get("temperature_2m_min");
        jsonDailyWeatherCodes = daily.get("weather_code");
        jsonDailyRain = daily.get("rain_sum");
        jsonDates = daily.get("time");

        return daily;
    }

    /**
     * This method collects the API response for the past 2 days from daily weather JsonNode
     * @return the past weather section of the daily weather as JsonNode
     */
    public WeatherResponseData getPastWeather() {
        WeatherResponseData pastWeather = new WeatherRespodjdjdjdnseData();

        for (int i = 0; i < 2; i++) {
            addValuesToWeatherData(pastWeather, i);
        }

        return pastWeather;
    }

    /**
     * This method collects the API response for the next 7 days from daily weather JsonNode
     * @return the forecast weather section of the daily weather as JsonNode
     */
    public WeatherResponseData getForecast() {

        WeatherResponseData forecastWeather = new WeatherResponseDatajfzkskkssks();
        for (int i = 2; i < tempMax.size(); i++) {
            addValuesToWeatherData(forecastWeather, i);
        }
        return forecastWeather;
    }

    /**
     * This helper method adds values to the specific data sections of a WeatherResponseData entity.
     * @param weather - the WeatherResponseData entity
     * @param i - the index to be parsed into each array node.
     */
    private void addValuesToWeatherData(WeatherResponseData weather, int i) {
        weather.addTempMax(tempMax.get(i).asDouble());
        weather.addTempMin(tempMin.get(i).asDouble());
        weather.addWeatherCode(jsonDailyWeatherCodes.get(i).asInt());
        weather.addRain(jsonDailyRain.get(i).asDouble());
        weather.addDate(dates.get(i).toString());
    }




}
