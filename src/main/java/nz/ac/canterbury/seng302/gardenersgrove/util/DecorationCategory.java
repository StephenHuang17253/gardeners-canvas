package nz.ac.canterbury.seng302.gardenersgrove.util;

/**
 * Enum class for the category of a Decoration.
 * This is used to determine which 3D model is used for a decoration in the 3D Garden View.
 */
public enum DecorationCategory {

    ROCK("Rock", "rock.png"),
    TABLE("Table", "table.png"),
    POND("Pond", "pond.png"),
    GNOME("Gnome", "gnome.png"),
    FOUNTAIN("Fountain", "fountain.png");

    private final String categoryName;
    private final String categoryImage;

    private DecorationCategory(String categoryName, String categoryImage) {
        this.categoryName = categoryName;
        this.categoryImage = categoryImage;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getCategoryImage() {
        return categoryImage;
    }

    @Override
    public String toString() {
        return this.categoryName;
    }

}
