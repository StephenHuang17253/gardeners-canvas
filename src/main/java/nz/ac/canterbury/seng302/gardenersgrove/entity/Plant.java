package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Entity class of a Plant, reflecting an entry of plant name
 * and optionally a count, description,planted-on date
 * and a Garden entity object that is associated with
 */
@Entity
public class Plant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long plantId;

    @Column(nullable = false, length = 64)
    private String plantName;

    @Column
    private float plantCount;

    @Column(length = 512)
    private String plantDescription;

    @Column
    private LocalDate plantDate;

    @ManyToOne
    @JoinColumn(name = "garden_id",nullable = false)
    private Garden garden;


    /**
     * JPA required no-args constructor
     */
    protected Plant() {}
    /**
     * Creates a new plant object.
     * @param plantName the name of the plant
     * @param plantCount the count of the plant
     * @param plantDescription the description of the plant
     * @param plantDate the date of planting
     //* @param gardenId the ID of the Garden the plant is planted in
     */
    public Plant(String plantName, float plantCount, String plantDescription, LocalDate plantDate, Garden garden) {
        this.plantName = plantName;
        this.plantCount = plantCount;
        this.plantDescription = plantDescription;
        this.plantDate = plantDate;
        this.garden = garden;
    }

    public Long getPlantId() {
        return plantId;
    }
    public String getPlantName() {
        return plantName;
    }
    public void setPlantName(String plantName) {this.plantName = plantName;}
    public String getPlantDescription() {return plantDescription;}
    public Float getPlantCount() {return plantCount;}
    public void setPlantCount(Float plantCount) {this.plantCount = plantCount;}
    public void setPlantDescription(String plantDescription) {
        this.plantDescription = plantDescription;
    }
    public LocalDate getPlantDate() {return plantDate;}
    public void setPlantDate(LocalDate plantDate) {this.plantDate = plantDate;}
    public Garden getGarden() {return garden;}
    @Override
    public String toString() {
        return "Plant{" +
                "id=" + plantId +
                ", name='" + plantName + '\'' +
                ", count='" + plantCount + '\'' +
                ", description='" + plantDescription + '\'' +
                ", plant date='" + plantDate + '\'' +
                ", garden id='" + garden.getGardenId() + '\'' +
                '}';
    }

}
