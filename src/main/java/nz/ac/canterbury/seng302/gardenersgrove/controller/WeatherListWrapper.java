package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.component.DailyWeather;

import java.util.List;

/**
 * The WeatherListWrapper class represents a wrapper for a list of DailyWeather
 * objects.
 * It provides methods to access and manipulate the weather data.
 */
public class WeatherListWrapper {

    private List<DailyWeather> weather;

    /**
     * Constructs a new WeatherListWrapper object with the specified list of daily
     * 
     * @param weather the list of daily weather
     */
    public WeatherListWrapper(List<DailyWeather> weather) {
        this.weather = weather;
    }

    /**
     * Returns the list of daily weather.
     *
     * @return the list of daily weather
     */
    public List<DailyWeather> getWeather() {
        return weather;
    }

}
