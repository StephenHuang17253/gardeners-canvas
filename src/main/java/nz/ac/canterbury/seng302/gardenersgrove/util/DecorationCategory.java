package nz.ac.canterbury.seng302.gardenersgrove.util;

/**
 * Enum class for the category of a Decoration.
 * This is used to determine which 3D model is used for a decoration in the 3D Garden View.
 */
public enum DecorationCategory {

    // TODO: Person who adds decoration images will replace 'tree.png' with suitable image.

    ROCK("Rock", "tree.png"),
    TABLE("Table", "tree.png"),
    POND("Pond", "tree.png"),
    GNOME("Gnome", "tree.png"),
    FOUNTAIN("Fountain", "tree.png");

    private final String categoryName;
    private final String categoryImage;
    private DecorationCategory(String categoryName, String categoryImage)
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
