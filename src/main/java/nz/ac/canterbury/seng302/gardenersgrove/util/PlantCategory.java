package nz.ac.canterbury.seng302.gardenersgrove.util;


/**
 * Enum class for the Plant Category of a Plant.
 * This is used to determine the type of 3D model it has in the 3D Garden View.
 */
public enum PlantCategory {
    TREE("tree.glb", 5),
    SHRUB("shrub.glb", 5),
    BUSH("shrub.glb", 10),
    HERB("fern.glb", 1),
    CREEPER("creeper.glb", .5f),
    CLIMBER("climber.glb", 5),
    FLOWER("flower.glb", 10),
    POT_PLANT("potplant.glb", 5);

    private String modelName;
    private float scaleFactor;
    private PlantCategory(String modelName, float scaleFactor)
    {
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

}
