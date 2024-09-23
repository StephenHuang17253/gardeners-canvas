package nz.ac.canterbury.seng302.gardenersgrove.model;

import nz.ac.canterbury.seng302.gardenersgrove.util.GridItemType;

/**
 * class of objects hat can be passed to the three js javascript to indicate
 * what plants or decorations are
 * stored in what locations
 */
public class DisplayableItem {
    private final int xCoordinate;

    private final int yCoordinate;

    private final String name;

    private final String category;

    private final Long objectId;

    private final GridItemType type;

    private final String categoryImage;

    private static final String ROOT_PATH = "/images/2d-plant-categories/";

    /**
     * create a new Displayable object
     * 
     * @param xCoordinate x location of object
     * @param yCoordinate y location of object
     * @param name        name of represented object e.g plant name
     * @param category    category of item
     */
    public DisplayableItem(int xCoordinate, int yCoordinate, String name, String category, long objectId, GridItemType type, String categoryImage) {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.name = name;
        this.category = category;
        this.objectId = objectId;
        this.type = type;
        this.categoryImage = ROOT_PATH + categoryImage;
    }

    public int getXCoordinate() {
        return xCoordinate;
    }

    public int getYCoordinate() {
        return yCoordinate;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public Long getObjectId() {
        return objectId;
    }

    public GridItemType getType() {
        return type;
    }

    public String getCategoryImage() {
        return categoryImage;
    }


    @Override
    public String toString() {
        return "DisplayableItem{" +
                "xCoordinate=" + xCoordinate +
                ", yCoordinate=" + yCoordinate +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", objectId=" + objectId +
                '}';
    }

}
