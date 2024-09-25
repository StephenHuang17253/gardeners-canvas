package nz.ac.canterbury.seng302.gardenersgrove.util;

public enum TileTexture {

    SOIL("Soil"),

    GRASS("Grass"),


    BARK("Bark"),

    STONE_PATH("Stone Path"),

    PEBBLE_PATH("Pebble Path"),

    CONCRETE("Concrete");

    private final String tileName;

    private TileTexture(String tileName) {
        this.tileName = tileName;
    }
    @Override
    public String toString() {
        return this.tileName;
    }

}
