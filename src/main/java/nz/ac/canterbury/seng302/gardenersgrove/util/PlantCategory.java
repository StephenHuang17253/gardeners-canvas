package nz.ac.canterbury.seng302.gardenersgrove.util;

/**
 * Enum class for the Plant Category of a Plant.
 * This is used to determine the type of 3D model it has in the 3D Garden View.
 */
public enum PlantCategory {
    TREE("Tree", "tree.png"),
    SHRUB("Shrub", "shrub.png"),
    BUSH("Bush", "fern.png"),
    HERB("Herb", "shrub.png"),
    CREEPER("Creeper", "creeper.png"),
    CLIMBER("Climber", "climber.png"),
    FLOWER("Flower", "flower.png"),
    POT_PLANT("Pot Plant", "potplant.png");

    private final String categoryName;
    private final String categoryImage;
    private PlantCategory(String categoryName, String categoryImage)
    {
        this.categoryName = categoryName;
        this.categoryImage = categoryImage;
    }

    public String getCategoryName(){
        return categoryName;
    }

    public String getCategoryImage(){
        return categoryImage;
    }

    @Override
    public String toString() {
        return this.categoryName;
    }

}
