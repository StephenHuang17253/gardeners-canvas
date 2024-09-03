package nz.ac.canterbury.seng302.gardenersgrove.model;

/**
 * Model class for storing pending request data for the Manage Friends page.
 */
public class RequestFriendModel {
    private String friendProfilePicture;
    private String friendName;
    private boolean isSender;

    private long userId;

    /**
     * Constructor for a PendingFriendModel
     * 
     * @param friendProfilePicture filename for the friend's profile picture.
     * @param friendName           first and last name of the friend.
     * @param isSender             whether they sent the request
     * @param userId               to have a reference to retrieve the user from the
     *                             repo later
     */
    public RequestFriendModel(String friendProfilePicture, String friendName, boolean isSender, long userId) {
        this.friendProfilePicture = friendProfilePicture;
        this.friendName = friendName;
        this.isSender = isSender;
        this.userId = userId;

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

    public boolean isSender() {
        return isSender;
    }

    public void setSender(boolean sender) {
        isSender = sender;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "FriendModel{" +
                "friendProfilePicture='" + friendProfilePicture + '\'' +
                ", friendName='" + friendName + '\'' +
                ", isSender='" + isSender + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

}
