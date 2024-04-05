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
    @Column(nullable = false)
    private String gardenLocation;
    @Column
    private float gardenSize;

    @OneToMany(mappedBy = "garden", cascade = CascadeType.ALL)
    private List<Plant> plants =  new ArrayList<>();


    /**
     * JPA required no-args constructor
     */
    protected Garden() {}
    /**
     * Creates a new Garden object.
     * @param gardenName the name of the garden
     * @param gardenLocation the location of the garden
     * @param gardenSize the size of the garden
     */
    public Garden(String gardenName, String gardenAddress, String gardenSuburb, String gardenCity,
                  String gardenPostcode, String gardenCountry, String gardenLocation, float gardenSize) {
        this.gardenName = gardenName;
        this.gardenAddress = gardenAddress;
        this.gardenSuburb = gardenSuburb;
        this.gardenPostcode = gardenPostcode;
        this.gardenCity = gardenCity;
        this.gardenCountry = gardenCountry;
        this.gardenLocation = gardenLocation;
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
    public String getGardenLocation() {
        return gardenLocation;
    }
    public void setGardenLocation(String gardenLocation) {
        this.gardenLocation = gardenLocation;
    }
    public float getGardenSize() {
        return gardenSize;
    }
    public void setGardenSize(float gardenSize) {
        this.gardenSize = gardenSize;
    }
    public List<Plant> getPlants(){
        return plants;
    }
    @Override
    public String toString() {
        return "Garden{" +
                "id=" + gardenId +
                ", name='" + gardenName + '\'' +
                ", location='" + gardenLocation + '\'' +
                ", size='" + gardenSize + '\'' +
                '}';
    }

}
