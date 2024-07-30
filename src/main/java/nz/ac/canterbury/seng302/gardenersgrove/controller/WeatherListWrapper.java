package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.component.DailyWeather;

import java.util.List;

public class WeatherListWrapper {
    private List<DailyWeather> weather;

    public WeatherListWrapper(List<DailyWeather> weather){
        this.weather = weather;
    }

    public List<DailyWeather> getWeather() {
        return weather;
    }

}
