package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.TileTexture;

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
    private TileTexture tileTexture;

    @Column(name = "x_coord")
    private int xCoordinate;

    @Column(name = "y_coord")
    private int yCoordinate;
    @ManyToOne
    @JoinColumn(name = "garden_id")
    private Garden garden;

    /**
     * JPA required no-args constructor
     */
    protected GardenTile() {
    }

    /**
     * Garden Tile constructor
     * @param garden        the garden the tile belongs to
     * @param tileTexture      the type of tile
     * @param xCoordinate   the tile's x-coordinate on the grid
     * @param yCoordinate   the tile's y-coordinate on the grid
     */
    public GardenTile(Garden garden,
                      TileTexture tileTexture,
                      int xCoordinate,
                      int yCoordinate) {
        this.garden = garden;
        this.tileTexture = tileTexture;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }


    public Long getTileId() {
        return tileId;
    }

    public void setTileId(Long tileId) {
        this.tileId = tileId;
    }

    public TileTexture getTileTexture() {
        return tileTexture;
    }

    public void setTileTexture(TileTexture tileTexture) {
        this.tileTexture = tileTexture;
    }

    public int getXCoordinate() {
        return xCoordinate;
    }

    public void setXCoordinate(int xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public int getYCoordinate() {
        return yCoordinate;
    }

    public void setYCoordinate(int yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public Garden getGarden() {
        return garden;
    }

    public void setGarden(Garden garden) {
        this.garden = garden;
    }
}
