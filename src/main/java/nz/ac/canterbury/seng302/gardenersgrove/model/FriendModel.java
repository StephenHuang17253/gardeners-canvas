package nz.ac.canterbury.seng302.gardenersgrove.model;

import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;

/**
 * Model class for storing friend data for the Manage Friends page.
 */
public class FriendModel {
    private String friendProfilePicture;
    private String friendName;
    private String friendGardenLink;
    private FriendshipStatus friendRequestStatus;
    private long friendId;

    /**
     * Constructor for a FriendModel
     *
     * @param friendProfilePicture filename for the friend's profile picture.
     * @param friendName           first and last name of the friend.
     * @param friendGardenLink     link to the friend's gardens page.
     */
    public FriendModel(String friendProfilePicture, String friendName, String friendGardenLink) {
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

    public void setFriendRequestStatus(FriendshipStatus status) {
        this.friendRequestStatus = status;
    }

    public String getFriendRequestStatusString() {
        if (friendRequestStatus != null) {
            return friendRequestStatus.toString();
        } else {
            return "";
        }

    }

    public void setFriendGardenLink(String friendGardenLink) {
        this.friendGardenLink = friendGardenLink;
    }

    public void setFriendId(Long id) {
        this.friendId = id;
    }

    public Long getFriendId() {
        return this.friendId;
    }

    @Override
    public String toString() {
        return "FriendModel{" +
                "friendProfilePicture='" + friendProfilePicture + '\'' +
                ", friendName='" + friendName + '\'' +
                ", friendGardenLink='" + friendGardenLink + '\'' +
                '}';
    }
}
