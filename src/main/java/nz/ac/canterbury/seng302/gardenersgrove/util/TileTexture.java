package nz.ac.canterbury.seng302.gardenersgrove.util;

public enum TileTexture {

    SOIL("Soil", "soil-tileable.png"),

    GRASS("Grass", "grass-tileable.png"),


    BARK("Bark", "bark-tileable.png"),

    STONE_PATH("Stone Path", "grass-tileable.png"),

    PEBBLE_PATH("Pebble Path", "grass-tileable.png"),

    CONCRETE("Concrete", "grass-tileable.png");

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
}

