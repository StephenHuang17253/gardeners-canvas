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
    @Column(nullable = false)
    private String gardenLocation;
    @Column
    private float gardenSize;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "garden_id")
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
     * @param owner the User object that owns the garden
     */
    public Garden(String gardenName, String gardenLocation, float gardenSize, User owner) {
        this.gardenName = gardenName;
        this.gardenLocation = gardenLocation;
        this.gardenSize = gardenSize;
        this.owner = owner;
    }
    /**
     * Creates a new Garden object without the owner param
     * Useful for updating plants
     * @param gardenName the name of the garden
     * @param gardenLocation the location of the garden
     * @param gardenSize the size of the garden
     */
    public Garden(String gardenName, String gardenLocation, float gardenSize) {
        this.gardenName = gardenName;
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
    public User getOwner() { return owner; }
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
                ", owner_id='" + owner.getId() + '\'' +
                ", plants='" + plants + '\'' +
                '}';
    }

}
