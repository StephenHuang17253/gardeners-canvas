package nz.ac.canterbury.seng302.gardenersgrove.model;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;

/**
 * Model class for plant info displayed on 2d garden
 */
public class Plant2DModel {

    private final Long id;
    private final String name;
    private final Integer count;
    private final String img;
    private final String category;
    private final String categoryImage;
    private String ROOT_PATH = "/images/2d-plant-categories/";

    /**
     * Constructor for model data struct for 2d gardens page
     * @param plant to convert
     */
    public Plant2DModel(Plant plant){
        this.id = plant.getPlantId();
        this.name = plant.getPlantName();
        this.count = plant.getPlantCount();
        this.img = plant.getPlantPictureFilename();
        this.category = plant.getPlantCategory().getCategoryName();
        this.categoryImage = ROOT_PATH + plant.getPlantCategory().getCategoryImage();
    }

    public String getName() {
        return name;
    }

    public Integer getCount() {
        return count;
    }

    public String getImg() {
        return img;
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