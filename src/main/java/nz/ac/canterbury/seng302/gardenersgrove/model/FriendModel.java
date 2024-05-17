package nz.ac.canterbury.seng302.gardenersgrove.model;

public class FriendModel {
    private String friendProfilePicture;
    private String friendName;
    private String friendGardenLink;

    public FriendModel(String friendProfilePicture, String friendName, String friendGardenLink){
        this.friendProfilePicture = friendProfilePicture;
        this.friendName = friendName;
        this.friendGardenLink = friendGardenLink;

    }

    public String getFriendProfilePicture() {
        return friendProfilePicture;
    }

    public void setFriendProfilePicture(String friendProfilePicture) {
        this.friendProfilePicture = friendProfilePicture;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getFriendGardenLink() {
        return friendGardenLink;
    }

    public void setFriendGardenLink(String friendGardenLink) {
        this.friendGardenLink = friendGardenLink;
    }
}
