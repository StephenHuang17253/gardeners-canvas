package nz.ac.canterbury.seng302.gardenersgrove.util;

/**
 * Enum class for the Plant Category of a Plant.
 * This is used to determine the type of 3D model it has in the 3D Garden View.
 */
public enum PlantCategory {
    TREE("Tree"),
    SHRUB("Shrub"),
    BUSH("Bush"),
    HERB("Herb"),
    CREEPER("Creeper"),
    CLIMBER("Climber"),
    FLOWER("Flower"),
    POT_PLANT("Pot Plant");

    private final String categoryName;
    private PlantCategory(String categoryName)
    {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        return this.categoryName;
    }

}
