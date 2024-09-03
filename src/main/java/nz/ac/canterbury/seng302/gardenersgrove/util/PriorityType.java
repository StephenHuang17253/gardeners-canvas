package nz.ac.canterbury.seng302.gardenersgrove.util;

public enum PriorityType {
    NORMAL("Normal Priority"),
    LOW("Low Priority");

    private String message;

    PriorityType(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return this.message;
    }
}
