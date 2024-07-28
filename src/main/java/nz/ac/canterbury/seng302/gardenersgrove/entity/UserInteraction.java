package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.ItemType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/**
 * Entity class of a UserInteraction, reflecting user data related to visiting pages
 */
@Entity
public class UserInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userInteractionId;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    private Long itemId;

    private ItemType itemType;

    private LocalDateTime interactionTime;

    protected UserInteraction() {
    }

    /**
     * Creates a new UserInteraction object
     *
     * @param user the user of whom the interaction pertains to
     * @param itemId the id of the item
     * @param itemType the item type of the interaction
     * @param interactionTime time of interaction
     */
    public UserInteraction(User user, Long itemId, ItemType itemType, LocalDateTime interactionTime){
        this.user = user;
        this.itemId = itemId;
        this.itemType = itemType;
        this.interactionTime = interactionTime;
    }

    public Long getUserInteractionId() {
        return userInteractionId;
    }

    public User getUser() {
        return user;
    }

    public Long getItemId() {
        return itemId;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public LocalDateTime getInteractionTime() {
        return interactionTime;
    }

    public void setInteractionTime(LocalDateTime interactionTime){
        this.interactionTime = interactionTime;
    }

    @Override
    public String toString() {
        return "UserInteraction{" +
                "id=" + userInteractionId +
                ", user='" + user.getId() + '\'' +
                ", itemId='" + itemId + '\'' +
                ", itemType='" + itemType + '\'' +
                ", interactionTime='" + interactionTime + '\'' +
                '}';
    }
}
