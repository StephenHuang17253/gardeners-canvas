package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Entity class of a Friendship, reflecting a many-to-many relationship between
 * users
 */
@Entity
@Table(name = "friendship_table")
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long friendShipId;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user1;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user2;

    private FriendshipStatus status;

    /**
     * JPA required no-args constructor
     */
    protected Friendship() {
    }

    /**
     * Creates a new Friendship object
     * NOTE the ordering
     *
     * @param user1  IMPORTANT the user that sent the request
     * @param user2  IMPORTANT the user that received the request
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

    public void setUser1(User user) {
        this.user1 = user;
    }

    public void setUser2(User user) {
        this.user2 = user;
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
