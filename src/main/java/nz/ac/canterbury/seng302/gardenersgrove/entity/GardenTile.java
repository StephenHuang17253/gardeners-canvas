package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.TagStatus;
import nz.ac.canterbury.seng302.gardenersgrove.util.TileType;

/**
 * Entity class of a tile in the garden
 */
@Entity
@Table(name = "garden_tile",
    indexes = {@Index(name="id_index", columnList = "tile_id"),
                @Index(name="garden_and_locaton",columnList = "garden_id,x_coord,y_coord")})
public class GardenTile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tile_id")
    private Long tileId;


    /**
     * The type of tile
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tile_type")
    private TileType tileType = TileType.GRASS;

    @Column(name = "x_coord")
    private int xCoordinate;

    @Column(name = "y_coord")
    private int yCoordinate;
    @ManyToOne
    @JoinColumn(name = "garden_id")
    private Garden garden;

    public Long getTileId() {
        return tileId;
    }

    public void setTileId(Long tileId) {
        this.tileId = tileId;
    }

    public TileType getTileType() {
        return tileType;
    }

    public void setTileType(TileType tileType) {
        this.tileType = tileType;
    }

    public int getxCoordinate() {
        return xCoordinate;
    }

    public void setxCoordinate(int xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public int getyCoordinate() {
        return yCoordinate;
    }

    public void setyCoordinate(int yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public Garden getGarden() {
        return garden;
    }

    public void setGarden(Garden garden) {
        this.garden = garden;
    }
}
