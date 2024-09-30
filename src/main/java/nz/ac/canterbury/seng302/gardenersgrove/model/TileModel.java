package nz.ac.canterbury.seng302.gardenersgrove.model;

import nz.ac.canterbury.seng302.gardenersgrove.util.TileTexture;

public class TileModel {

    private TileTexture texture;

    private int x;

    private int y;

    public TileModel(TileTexture texture, int x, int y) {
        this.texture = texture;
        this.x = x;
        this.y = y;
    }

    public int getXCoordinate() {return x;}
    public int getYCoordinate() {return y;}
    public TileTexture getTileTexture() {return texture;}
}
