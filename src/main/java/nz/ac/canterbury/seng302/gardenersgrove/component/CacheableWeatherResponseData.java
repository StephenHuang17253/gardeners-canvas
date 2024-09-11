package nz.ac.canterbury.seng302.gardenersgrove.component;

import java.util.Date;

/**
 * Stores weather response data with an added location identifier and expiry
 * data to make caching implementation easier
 */
public class CacheableWeatherResponseData {

    private String locationIdentifier;
    private WeatherResponseData weatherResponseData;
    private Long creationTime;

    /**
     * create a new Cacheable Weather Response object
     * objects creation time will be set to time when this command is run
     * 
     * @param locationId  objects location id
     * @param weatherData objects weather data object
     */
    public CacheableWeatherResponseData(String locationId, WeatherResponseData weatherData) {
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

    /**
     * @return True if this record is older than 1H, false otherwise
     */
    public boolean isExpired() {
        return (new Date().getTime() - this.creationTime) > 3600000;

    }
}
