package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class of a Garden, reflecting an entry of garden name, location and size (optional)
 * and any plant entity objects that is associated with
 * Based off the SENG202 demo FormResult entity
 */
@Entity
public class Garden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "garden_id")
    private Long gardenId;
    @Column(nullable = false)
    private String gardenName;
    @Column
    private String gardenAddress;
    @Column
    private String gardenSuburb;
    @Column
    private String gardenPostcode;
    @Column
    private String gardenCity;
    @Column
    private String gardenCountry;
    @Column
    private float gardenSize;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User owner;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "garden_id")
    private List<Plant> plants =  new ArrayList<>();


    /**
     * JPA required no-args constructor
     */
    public Garden() {}
    /**
     * Creates a new Garden object.
     * @param gardenName the name of the garden
     * @param gardenAddress the address of the garden
     * @param gardenSuburb the suburb of the garden
     * @param gardenCity the city of the garden
     * @param gardenPostcode the postcode of the garden
     * @param gardenCountry the country of the garden
     * @param gardenSize the size of the garden
     * @param owner the User object that owns the garden
     */
    public Garden(String gardenName, String gardenAddress, String gardenSuburb, String gardenCity,
                  String gardenPostcode, String gardenCountry, float gardenSize, User owner) {
        this.gardenName = gardenName;
        this.gardenAddress = gardenAddress;
        this.gardenSuburb = gardenSuburb;
        this.gardenCity = gardenCity;
        this.gardenPostcode = gardenPostcode;
        this.gardenCountry = gardenCountry;
        this.gardenSize = gardenSize;
        this.owner = owner;
    }

    /**
     * Creates a new Garden object without owner used for update Garden.
     * @param gardenName the name of the garden
     * @param gardenAddress the address of the garden
     * @param gardenSuburb the suburb of the garden
     * @param gardenCity the city of the garden
     * @param gardenPostcode the postcode of the garden
     * @param gardenCountry the country of the garden
     * @param gardenSize the size of the garden
     */
    public Garden(String gardenName, String gardenAddress, String gardenSuburb, String gardenCity,
                  String gardenPostcode, String gardenCountry, float gardenSize) {
        this.gardenName = gardenName;
        this.gardenAddress = gardenAddress;
        this.gardenSuburb = gardenSuburb;
        this.gardenCity = gardenCity;
        this.gardenPostcode = gardenPostcode;
        this.gardenCountry = gardenCountry;
        this.gardenSize = gardenSize;
    }

    public Long getGardenId() {
        return gardenId;
    }
    public String getGardenName() {
        return gardenName;
    }
    public void setGardenName(String gardenName) {
        this.gardenName = gardenName;
    }
    public String getGardenAddress() {
        return gardenAddress;
    }
    public void setGardenAddress(String gardenAddress) {
        this.gardenAddress = gardenAddress;
    }
    public String getGardenSuburb() {
        return gardenSuburb;
    }
    public void setGardenSuburb(String gardenSuburb) {
        this.gardenSuburb = gardenSuburb;
    }
    public String getGardenPostcode() {
        return gardenPostcode;
    }
    public void setGardenPostcode(String gardenPostcode) {
        this.gardenPostcode = gardenPostcode;
    }
    public String getGardenCity() {
        return gardenCity;
    }
    public void setGardenCity(String gardenCity) {
        this.gardenCity = gardenCity;
    }
    public String getGardenCountry() {
        return gardenCountry;
    }
    public void setGardenCountry(String gardenCountry) {
        this.gardenCountry = gardenCountry;
    }
    public float getGardenSize() {
        return gardenSize;
    }
    public void setGardenSize(float gardenSize) {
        this.gardenSize = gardenSize;
    }
    public User getOwner() { return owner; }
    public List<Plant> getPlants(){
        return plants;
    }

    /**
     * Retrieves a garden's location which is a concatenation of its address components.
     *
     * @return garden location string in the format: {address}, {suburb}, {city} {postcode}, {country}
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
                ", location='" + getGardenLocation() + '\'' +
                ", size='" + gardenSize + '\'' +
                ", owner_id='" + owner.getId() + '\'' +
                ", plants='" + plants + '\'' +
                '}';
    }

}
