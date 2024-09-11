package nz.ac.canterbury.seng302.gardenersgrove.model;

/**
 * class of objects hat can be passed to the three js javascript to indicate
 * what plants or decorations are
 * stored in what locations
 */
public class DisplayableItem {
    private int xCoordinate;

    private int yCoordinate;

    private String name;

    private String category;

    private Long objectId;

    private String categoryImage;
    private String ROOT_PATH = "/images/2d-plant-categories/";

    /**
     * create a new Displayable object
     * 
     * @param xCoordinate x location of object
     * @param yCoordinate y location of object
     * @param name        name of represented object e.g plant name
     * @param category    category of item
     */
    public DisplayableItem(int xCoordinate, int yCoordinate, String name, String category, long objectId, String categoryImage) {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.name = name;
        this.category = category;
        this.objectId = objectId;
        this.categoryImage = ROOT_PATH + categoryImage;
    }

    public int getXCoordinate() {
        return xCoordinate;
    }

    public void setXCoordinate(int xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public int getYCoordinate() {
        return yCoordinate;
    }

    public void setYCoordinate(int yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getObjectId() {
        return objectId;
    }

    public String getCategoryImage() {
        return categoryImage;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
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
