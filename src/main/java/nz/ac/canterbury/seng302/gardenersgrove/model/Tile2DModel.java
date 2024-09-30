package nz.ac.canterbury.seng302.gardenersgrove.model;

import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTile;

/**
 * Model class for tiles displayed on 2D garden.
 * Used to pass info about tiles to frontend.
 */
public class Tile2DModel {
    private final Long id;
    private final int xCoordinate;
    private final int yCoordinate;
    private final String tileTexture;
    private final String tileImage;
    private static final String ROOT_PATH = "/images/2d-tile-textures/";

    /**
     * Constructor for model data struct for 2d gardens page
     * @param tile to convert
     */
    public Tile2DModel(GardenTile tile){
        this.id = tile.getTileId();
        this.xCoordinate = tile.getXCoordinate();
        this.yCoordinate = tile.getYCoordinate();
        this.tileTexture = tile.getTileTexture().getTileName();
        this.tileImage = ROOT_PATH + tile.getTileTexture().getImgFile();
    }

    public int getXCoordinate() {
        return xCoordinate;
    }

    public int getYCoordinate() {
        return yCoordinate;
    }

    public String getTileTexture() {
        return tileTexture;
    }

    public String getTileImage() {
        return tileImage;
    }

    public Long getId() {
        return id;
    }
}