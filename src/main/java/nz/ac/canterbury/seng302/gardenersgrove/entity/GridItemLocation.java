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
    @JoinColumn(name = "garden_id", nullable = false)
    private Garden garden;

    @Column(name = "object_id")
    private Long objectId;

    @Column(name = "x_coord")
    private int xCoordinate;

    @Column(name = "y_coord")
    private int yCoordinate;

    public GridItemLocation(){}

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

    public int getXCoordinates() {
        return xCoordinate;
    }

    public void setXCoordinates(int xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public int getYCoordinates() {
        return yCoordinate;
    }

    public void setYCoordinates(int yCoordinate) {
        this.yCoordinate = yCoordinate;
    }





}
