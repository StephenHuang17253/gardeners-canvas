package nz.ac.canterbury.seng302.gardenersgrove.util;


/**
 * Enum class for the Plant Category of a Plant.
 * This is used to determine the type of 3D model it has in the 3D Garden View.
 */
public enum PlantCategory {
    TREE("Tree","tree.png","tree.glb", 5),
    SHRUB("Shrub","shrub.png","shrub.glb", 5),
    BUSH("Bush","fern.png","fern.glb", 10),
    HERB("Herb","shrub.png","shrub.glb", 5),
    CREEPER("Creeper","creeper.png","creeper.glb", .5f),
    CLIMBER("Climber","climber.png","climber.glb", 5),
    FLOWER("Flower","flower.png","flower.glb", 10),
    POT_PLANT("Potplant","potplant.png","potplant.glb", 5);

    private String name;
    private String imageName;
    private String modelName;
    private float scaleFactor;

    private PlantCategory(String name, String imageName, String modelName, float scaleFactor)
    {
        this.name = name;
        this.imageName = imageName;
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

    public String getName() {
        return name;
    }

    public String getImageName() {
        return imageName;
    }
}
