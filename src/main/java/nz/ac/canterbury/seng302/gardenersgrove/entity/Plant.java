package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.PlantCategory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    @Column(nullable = false, columnDefinition = "TEXT")
    private String plantName;

    @Column
    private int plantCount;

    @Column(columnDefinition = "TEXT")
    private String plantDescription;

    @Column
    private LocalDate plantDate;

    @ManyToOne
    @JoinColumn(name = "garden_id")
    private Garden garden;

    @Enumerated(EnumType.STRING)
    @Column
    private PlantCategory plantCategory;

    @Column
    private String plantPictureFilename;

    /**
     * JPA required no-args constructor
     */
    protected Plant() {
    }

    /**
     * Creates a new plant object.
     *
     * @param plantName        the name of the plant
     * @param plantCount       the count of the plant
     * @param plantDescription the description of the plant
     * @param plantDate        the date of planting
     * @param garden           the Garden object that the plant belongs to
     */
    public Plant(String plantName, int plantCount, String plantDescription, LocalDate plantDate, Garden garden,
            PlantCategory plantCategory) {
        this.plantName = plantName;
        this.plantCount = plantCount;
        this.plantDescription = plantDescription;
        this.plantDate = plantDate;
        this.garden = garden;
        this.plantCategory = plantCategory;
    }

    public Long getPlantId() {
        return plantId;
    }

    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    public String getPlantDescription() {
        return plantDescription;
    }

    public int getPlantCount() {
        return plantCount;
    }

    public void setPlantCount(int plantCount) {
        this.plantCount = plantCount;
    }

    public void setPlantDescription(String plantDescription) {
        this.plantDescription = plantDescription;
    }

    public LocalDate getPlantDate() {
        return plantDate;
    }

    public String getFormattedPlantDate() {
        String formattedDate = "";
        if (plantDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            formattedDate = plantDate.format(formatter);
        }
        return formattedDate;
    }

    public void setPlantDate(LocalDate plantDate) {
        this.plantDate = plantDate;
    }

    public Garden getGarden() {
        return garden;
    }

    public PlantCategory getPlantCategory() {
        if (this.plantCategory == null) {
            this.plantCategory = PlantCategory.POT_PLANT;
        }
        return this.plantCategory;
    }

    public void setPlantCategory(PlantCategory plantCategory) {
        this.plantCategory = plantCategory;
    }

    public String getPlantPictureFilename() {
        return this.plantPictureFilename;
    }

    public void setPlantPictureFilename(String plantPictureFilename) {
        this.plantPictureFilename = plantPictureFilename;
    }

    @Override
    public String toString() {
        return "Plant{" +
                "id=" + plantId +
                ", name='" + plantName + '\'' +
                ", count='" + plantCount + '\'' +
                ", description='" + plantDescription + '\'' +
                ", plant date='" + plantDate + '\'' +
                ", plant picture filename='" + plantPictureFilename + '\'' +
                ", garden id='" + garden.getGardenId() + '\'' +
                ", plant category='" + plantCategory + '\'' +
                '}';
    }

}
