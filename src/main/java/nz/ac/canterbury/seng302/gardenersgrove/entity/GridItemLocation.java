package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.GridItemType;

@Entity
public class GridItemLocation {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type")
    private GridItemType itemType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "garden_id")
    private Garden garden;

    @Column(name = "object_id")
    private Long objectId;

    @Column(name = "x_coord")
    private int xCoordinate;

    @Column(name = "y_coord")
    private int yCoordinate;

    /**
     * Zero Argument JPA constructor
     */
    public GridItemLocation() {
    }

    /**
     * Constructor for GridItemLocation object.
     * (Used to store the location of a plant or decoration on the 2D grid of our 2D
     * garden view)
     * 
     * @param objectId    id of the plant or decoration
     * @param itemType    enum, PLANT or DECORATION
     * @param garden      the garden the 2D view is for
     * @param xCoordinate the x-coordinate on the grid
     * @param yCoordinate the y-coordinate on the grid
     */
    public GridItemLocation(Long objectId, GridItemType itemType, Garden garden, int xCoordinate, int yCoordinate) {
        this.objectId = objectId;
        this.itemType = itemType;
        this.garden = garden;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GridItemType getItemType() {
        return itemType;
    }

    public void setItemType(GridItemType itemType) {
        this.itemType = itemType;
    }

    public Garden getGarden() {
        return garden;
    }

    public void setGarden(Garden garden) {
        this.garden = garden;
    }

    public int getXCoordinate() {
        return xCoordinate;
    }

    public void setXCoordinates(int xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public int getYCoordinate() {
        return yCoordinate;
    }

    public void setYCoordinates(int yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    @Override
    public String toString() {
        return "GridItemLocation{" +
                "id=" + id +
                ", itemType=" + itemType +
                ", garden=" + garden +
                ", objectId=" + objectId +
                ", xCoordinate=" + xCoordinate +
                ", yCoordinate=" + yCoordinate +
                '}';
    }

}
