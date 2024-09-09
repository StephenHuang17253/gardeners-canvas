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

    private float modelScale;

    private String modelName;

    private Long objectId;

    /**
     * create a new Displayable object
     * 
     * @param xCoordinate x location of object
     * @param yCoordinate y location of object
     * @param name        name of represented object e.g plant name
     * @param modelName   name of model used to display in 3d, e.g tree.obj
     * @param modelScale  a value to scale the displayed model by
     */
    public DisplayableItem(int xCoordinate, int yCoordinate, String name, String modelName, float modelScale, long objectId) {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.name = name;
        this.modelName = modelName;
        this.modelScale = modelScale;
        this.objectId = objectId;
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

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public float getModelScale() {
        return modelScale;
    }

    public void setModelScale(float modelScale) {
        this.modelScale = modelScale;
    }

    public Long getObjectId() {return objectId;}

    public void setObjectId(long objectId) {this.objectId = objectId;}

}
