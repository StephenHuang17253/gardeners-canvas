package nz.ac.canterbury.seng302.gardenersgrove.component;

import java.util.Date;

public class CacheableWeatherResponseData {

    private String locationIdentifier;
    private WeatherResponseData weatherResponseData;
    private Long creationTime;

    public CacheableWeatherResponseData(String locationId, WeatherResponseData weatherData)
    {
        this.weatherResponseData = weatherData;
        this.locationIdentifier = locationId;
        this.creationTime = new Date().getTime();
    }

    public String getLocationIdentifier() {
        return locationIdentifier;
    }

    public void setLocationIdentifier(String locationIdentifier) {
        this.locationIdentifier = locationIdentifier;
    }

    public WeatherResponseData getWeatherResponseData() {
        return weatherResponseData;
    }

    public void setWeatherResponseData(WeatherResponseData weatherResponseData) {
        this.weatherResponseData = weatherResponseData;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public boolean isExpired()
    {
        return (new Date().getTime() - this.creationTime) > 3600000;

    }
}
