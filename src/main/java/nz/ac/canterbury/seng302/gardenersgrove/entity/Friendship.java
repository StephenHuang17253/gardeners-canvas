package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;

@Entity
@Table(name = "friendship_table")
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long friendShipId;


    @ManyToOne
    private User user1;

    @ManyToOne
    private User user2;

    private FriendshipStatus status;

    /**
     * JPA required no-args constructor
     */
    protected Friendship() {
    }
    /**
     * Creates a new Friendship object
     *
     * @param user1 one half of the user relationship
     * @param user2 the other half of the user relationship
     * @param status enum status of the two users relationship
     */
    public Friendship(User user1, User user2, FriendshipStatus status) {
        this.user1 = user1;
        this.user2 = user2;
        this.status = status;
    }
    public Long getId() {
        return friendShipId;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = status;
    }
    public User getUser1() {
        return user1;
    }

    public User getUser2() {
        return user2;
    }

    @Override
    public String toString() {
        return "Friendship{" +
                "id=" + friendShipId +
                ", user_1='" + user1.getId() + '\'' +
                ", user_2='" + user2.getId() + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

}
