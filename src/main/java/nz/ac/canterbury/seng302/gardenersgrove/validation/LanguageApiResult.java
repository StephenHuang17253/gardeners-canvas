package nz.ac.canterbury.seng302.gardenersgrove.validation;

public enum LanguageApiResult {
    OK("valid"),
    CONTAINSPROFAINTY("contains profainity"),
    BLANK("cannot be empty"),
    ERROR400("400 - Bad Request"),
    ERROR401("Error 401 - Unauthorized"),
    ERROR404("Error 404 - Not Found"),
    ERROR429("Error 429 - Too many requests"),
    ERROR5XX("5xx - Server Error");


    private String message;
    LanguageApiResult(String inMessage) {this.message = inMessage;}
    public boolean valid()
    {
        return this == OK;
    }

    @Override
    public String toString()
    {
        return this.message;
    }

    public void updateMessage(String newMessage)
    {
        this.message = newMessage;
    }
}