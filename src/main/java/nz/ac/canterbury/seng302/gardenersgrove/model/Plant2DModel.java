package nz.ac.canterbury.seng302.gardenersgrove.model;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;

/**
 * Model class for plant info displayed on 2d garden
 */
public class Plant2DModel {

    private Long id;
    private String name;
    private Integer count;
    private String img;
    private String category;
    private String categoryImage;
    private String ROOT_PATH = "images/2d-plant-types/";

    /**
     * Constructor
     * @param plant
     */
    public Plant2DModel(Plant plant){
        this.id = plant.getPlantId();
        this.name = plant.getPlantName();
        this.count = plant.getPlantCount();
        this.img = plant.getPlantPictureFilename();
        this.category = plant.getPlantCategory().getName();
        this.categoryImage = ROOT_PATH + plant.getPlantCategory().getImageName();
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
