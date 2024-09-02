package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity class of a Garden, reflecting an entry of garden name, location and
 * size (optional)
 * Entity class of a Garden, reflecting an entry of garden name, location and
 * size (optional)
 * and any plant entity objects that is associated with
 * Based off the SENG202 demo FormResult entity
 */
@Entity
public class Garden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "garden_id")
    private Long gardenId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String gardenName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String gardenDescription;

    @Column(columnDefinition = "TEXT")
    private String gardenAddress;

    @Column(columnDefinition = "TEXT")
    private String gardenSuburb;

    @Column(columnDefinition = "TEXT")
    private String gardenPostcode;

    @Column(columnDefinition = "TEXT")
    private String gardenCity;

    @Column(columnDefinition = "TEXT")
    private String gardenCountry;

    @Column(nullable = true)
    private Double gardenSize;

    @Column(columnDefinition = "TEXT")
    private String gardenLongitude;

    @Column(columnDefinition = "TEXT")
    private String gardenLatitude;

    @Column(name = "creation_date")
    @CreatedDate
    private LocalDateTime creationDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User owner;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "garden_id")
    private List<Plant> plants = new ArrayList<>();

    @Column(nullable = false)
    private boolean isPublic;

    @Column(name = "needs_watering")
    private Boolean needsWatering;

    @Column(name = "last_water_check")
    @CreatedDate
    private LocalDateTime lastWaterCheck;

    @Column(name = "last_location_update")
    @CreatedDate
    private LocalDateTime lastLocationUpdate;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "garden_id")
    private List<GridItemLocation> itemLocations = new ArrayList<>();

    /**
     * JPA required no-args constructor
     */
    public Garden() {
    }

    /**
     * Creates a new Garden object.
     * 
     * @param gardenName     the name of the garden
     * @param gardenAddress  the address of the garden
     * @param gardenSuburb   the suburb of the garden
     * @param gardenCity     the city of the garden
     * @param gardenPostcode the postcode of the garden
     * @param gardenCountry  the country of the garden
     * @param gardenSize     the size of the garden
     * @param isPublic       the visibility of the garden
     * @param owner          the User object that owns the garden
     */
    public Garden(String gardenName, String gardenDescription, String gardenAddress, String gardenSuburb,
            String gardenCity,
            String gardenPostcode, String gardenCountry, Double gardenSize, Boolean isPublic, String gardenLatitude,
            String gardenLongitude, User owner) {
        this.gardenName = gardenName;
        this.gardenDescription = gardenDescription;
        this.gardenAddress = gardenAddress;
        this.gardenSuburb = gardenSuburb;
        this.gardenCity = gardenCity;
        this.gardenPostcode = gardenPostcode;
        this.gardenCountry = gardenCountry;
        this.gardenSize = gardenSize;
        this.gardenLatitude = gardenLatitude;
        this.gardenLongitude = gardenLongitude;
        this.isPublic = isPublic;
        this.owner = owner;
        this.creationDate = LocalDateTime.now();
        this.lastLocationUpdate = LocalDateTime.now();
        this.needsWatering = false;
    }

    /**
     * Removes a grid item location from this garden such that it can be deleted.
     * This is required as JPA
     * <a href="https://stackoverflow.com/questions/22688402/delete-not-working-with-jparepository/52525033#52525033">
     *  will not delete objects still linked in bidirectional relationships </a>
     *  Should only be called by the pre delete method in gridItemLocation entity.
     * @param gridItemLocation the grid item location to remove from this garden
     */
    protected void dismissGridLocation(GridItemLocation gridItemLocation)
    {
        this.itemLocations.remove(gridItemLocation);
    }

    /**
     * Creates a new Garden object without owner used for update Garden.
     *
     * @param gardenName     the name of the garden
     * @param gardenAddress  the address of the garden
     * @param gardenSuburb   the suburb of the garden
     * @param gardenCity     the city of the garden
     * @param gardenPostcode the postcode of the garden
     * @param gardenCountry  the country of the garden
     * @param gardenSize     the size of the garden
     * @param isPublic       the visibility of the garden
     */
    public Garden(String gardenName, String gardenDescription, String gardenAddress, String gardenSuburb,
            String gardenCity,
            String gardenPostcode, String gardenCountry, Double gardenSize, Boolean isPublic, String gardenLatitude,
            String gardenLongitude) {
        this.gardenName = gardenName;
        this.gardenDescription = gardenDescription;
        this.gardenAddress = gardenAddress;
        this.gardenSuburb = gardenSuburb;
        this.gardenCity = gardenCity;
        this.gardenPostcode = gardenPostcode;
        this.gardenCountry = gardenCountry;
        this.gardenSize = gardenSize;
        this.gardenLatitude = gardenLatitude;
        this.gardenLongitude = gardenLongitude;
        this.isPublic = isPublic;
        this.needsWatering = false;
        this.lastLocationUpdate = LocalDateTime.now();
    }

    /**
     * removes tha association between a garden and GridItemLocation before it is deleted.
     * This avoids grid items trying to de register themselves from gardens when deleted, which causes a concurent
     * modification exception
     */
    @PreRemove
    public void removeGardenGridAssociations()
    {
        itemLocations.forEach(location -> location.setGarden(null));
    }

    public Long getGardenId() {
        return gardenId;
    }

    public String getGardenName() {
        return gardenName;
    }

    public String getGardenDescription() {
        return gardenDescription;
    }

    public String getGardenAddress() {
        return gardenAddress;
    }

    public String getGardenSuburb() {
        return gardenSuburb;
    }

    public String getGardenPostcode() {
        return gardenPostcode;
    }

    public String getGardenCity() {
        return gardenCity;
    }

    public String getGardenCountry() {
        return gardenCountry;
    }

    public double getGardenSize() {
        return gardenSize;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public String getGardenLongitude() {
        return gardenLongitude;
    }

    public String getGardenLatitude() {
        return gardenLatitude;
    }

    public User getOwner() {
        return owner;
    }

    public List<Plant> getPlants() {
        return plants;
    }

    public boolean getIsPublic() {
        return isPublic;
    }

    public LocalDateTime getLastLocationUpdate() {
        return lastLocationUpdate;
    }

    public boolean getNeedsWatering() {
        if (Objects.isNull(needsWatering)) {
            return false;
        }
        return needsWatering;
    }

    public LocalDateTime getLastWaterCheck() {
        return lastWaterCheck;
    }

    public List<GridItemLocation> getItemLocations() {
        return itemLocations;
    }



    public void setGardenName(String gardenName) {
        this.gardenName = gardenName;
    }

    public void setGardenDescription(String gardenDescription) {
        this.gardenDescription = gardenDescription;
    }

    public void setGardenAddress(String gardenAddress) {
        this.gardenAddress = gardenAddress;
    }

    public void setGardenSuburb(String gardenSuburb) {
        this.gardenSuburb = gardenSuburb;
    }

    public void setGardenPostcode(String gardenPostcode) {
        this.gardenPostcode = gardenPostcode;
    }

    public void setGardenCity(String gardenCity) {
        this.gardenCity = gardenCity;
    }

    public void setGardenCountry(String gardenCountry) {
        this.gardenCountry = gardenCountry;
    }

    public void setGardenSize(double gardenSize) {
        this.gardenSize = gardenSize;
    }

    public void setGardenLongitude(String gardenLongitude) {
        this.gardenLongitude = gardenLongitude;
    }

    public void setGardenLatitude(String gardenLatitude) {
        this.gardenLatitude = gardenLatitude;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * Sets boolean value for if a garden needs watering and sets the date at which
     * that garden was last checked for watering
     *
     * @param needsWatering boolean value for if a garden needs watering
     */
    public void setNeedsWatering(boolean needsWatering) {
        this.needsWatering = needsWatering;
        this.lastWaterCheck = LocalDateTime.now();
    }

    /**
     * Overloaded method where you can manually set lastwatercheck for testing
     * purposes
     * Sets boolean value for if a garden needs watering and sets the date at which
     * that garden was last checked for watering
     *
     * @param needsWatering  boolean value for if a garden needs watering
     * @param lastWaterCheck date manually set for when the watering need was last
     *                       checked
     */
    public void setNeedsWatering(boolean needsWatering, LocalDateTime lastWaterCheck) {
        this.needsWatering = needsWatering;
        this.lastWaterCheck = lastWaterCheck;
    }

    public void setLastLocationUpdate(LocalDateTime lastLocationUpdate) {
        this.lastLocationUpdate = lastLocationUpdate;
    }

    public void updateLocation(String gardenLatitude, String gardenLongitude) {
        this.gardenLatitude = gardenLatitude;
        this.gardenLongitude = gardenLongitude;
        this.lastLocationUpdate = LocalDateTime.now();
    }


    /**
     * Retrieves a garden's location which is a concatenation of its address
     * components.
     *
     * @return garden location string in the format: {address}, {suburb}, {city}
     *         {postcode}, {country}
     */
    public String getGardenLocation() {
        // Concatenate address components to form the complete location string
        String locationString = "";
        if (!gardenAddress.isBlank()) {
            locationString += gardenAddress + ", ";
        }
        if (!gardenSuburb.isBlank()) {
            locationString += gardenSuburb + ", ";
        }
        if (!gardenCity.isBlank()) {
            locationString += gardenCity;
            if (gardenPostcode.isBlank()) {
                locationString += ", ";
            } else {
                locationString += " ";
            }
        }
        if (!gardenPostcode.isBlank()) {
            locationString += gardenPostcode + ", ";
        }
        if (!gardenCountry.isBlank()) {
            locationString += gardenCountry;
        }

        return locationString;
    }

    @Override
    public String toString() {
        return "Garden{" +
                "id=" + gardenId +
                ", name='" + gardenName + '\'' +
                ", description='" + gardenDescription + '\'' +
                ", location='" + getGardenLocation() + '\'' +
                ", size='" + gardenSize + '\'' +
                ", ownerId='" + owner.getId() + '\'' +
                ", plants='" + plants + '\'' +
                ", isPublic='" + isPublic + '\'' +
                '}';
    }

}
