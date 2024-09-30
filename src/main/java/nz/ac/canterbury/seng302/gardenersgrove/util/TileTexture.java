package nz.ac.canterbury.seng302.gardenersgrove.util;

public enum TileTexture {

    SOIL("Soil", "soil.png"),

    GRASS("Grass", "grass.png"),

    BARK("Bark", "bark.png"),

    STONE_PATH("Stone Path", "stonePath.png"),

    PEBBLE_PATH("Pebble Path", "pebblePath.png"),

    CONCRETE("Concrete", "concrete.png");

    private final String tileName;
    private final String imgFile;

    private TileTexture(String tileName, String imgFile) {
        this.tileName = tileName;
        this.imgFile = imgFile;
    }
    @Override
    public String toString() {
        return this.tileName;
    }

    public String getTileName() {
        return tileName;
    }

    public String getImgFile() {
        return imgFile;
    }

    public static TileTexture getTileTextureByName(String tileName) {
        for (TileTexture tile : values()) {
            if (tile.tileName.equals(tileName)) {
                return tile;
            }
        }
        return null;
    }
}

