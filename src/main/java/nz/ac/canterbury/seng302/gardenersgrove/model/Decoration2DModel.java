package nz.ac.canterbury.seng302.gardenersgrove.model;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Decoration;

/**
 * Model class for decoration info displayed on 2D garden.
 * Used to pass info about decorations to frontend.
 */
public class Decoration2DModel {

    private final Long id;
    private final String category;
    private final String categoryImage;
    private static final String ROOT_PATH = "/images/decoration-icons/";

    /**
     * Constructor for model data struct for 2d gardens page
     * @param decoration to convert
     */
    public Decoration2DModel(Decoration decoration){
        this.id = decoration.getId();
        this.category = decoration.getDecorationCategory().getCategoryName();
        this.categoryImage = ROOT_PATH + decoration.getDecorationCategory().getCategoryImage();
    }

    public String getCategory() {
        return category;
    }

    public String getCategoryImage() {
        return categoryImage;
    }

    public Long getId() {
        return id;
    }
}