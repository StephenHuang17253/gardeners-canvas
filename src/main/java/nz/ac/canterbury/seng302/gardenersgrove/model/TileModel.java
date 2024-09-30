package nz.ac.canterbury.seng302.gardenersgrove.model;

import nz.ac.canterbury.seng302.gardenersgrove.util.TileTexture;

/**
 * Model of Tiles in a Garden to be used in tiles.js
 * Used for sending information about tiles to frontend
 */
public class TileModel {

    private TileTexture texture;

    private int x;

    private int y;

    /**
     * Create TileModel
     * @param texture - the type of texture the tile is
     * @param x - the xCoordinate of the tile
     * @param y - the yCoordinate of the tile
     */
    public TileModel(TileTexture texture, int x, int y) {
        this.texture = texture;
        this.x = x;
        this.y = y;
    }

    public int getXCoordinate() {return x;}
    public int getYCoordinate() {return y;}
    public TileTexture getTileTexture() {return texture;}
}
