package nz.ac.canterbury.seng302.gardenersgrove.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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




}
