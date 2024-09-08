package nz.ac.canterbury.seng302.gardenersgrove.util;

/**
 * Enum class for the Plant Category of a Plant.
 * This is used to determine the type of 3D model it has in the 3D Garden View.
 */
public enum PlantCategory {
    TREE("Tree", "tree.glb", 5),
    SHRUB("Shrub","shrub.glb", 5),
    BUSH("Bush", "shrub.glb", 10),
    HERB("Herb", "fern.glb", 1),
    CREEPER("Creeper", "creeper.glb", .5f),
    CLIMBER("Climber", "climber.glb", 5),
    FLOWER("Flower", "flower.glb", 10),
    POT_PLANT("Pot Plant", "potplant.glb", 5);

    private String categoryName;
    private String modelName;
    private float scaleFactor;
    private PlantCategory(String categoryName, String modelName, float scaleFactor)
    {
        this.categoryName = categoryName;
        this.modelName = modelName;
        this.scaleFactor = scaleFactor;
    }

    public String getModelName()
    {
        return this.modelName;
    }

    public float getScaleFactor()
    {
        return this.scaleFactor;
    }

    @Override
    public String toString() {
        return this.categoryName;
    }

}
