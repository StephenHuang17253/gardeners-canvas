package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.ItemType;
import java.time.LocalDateTime;

@Entity
public class UserInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userInteractionId;

    @ManyToOne
    private User user;

    private ItemType itemType;

    private LocalDateTime interactionTime;

    protected UserInteraction() {
    }

    public UserInteraction(User user, ItemType itemType, LocalDateTime interactionTime){
        this.user = user;
        this.itemType = itemType;
        this.interactionTime = interactionTime;
    }

    public User getUser() {
        return user;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public LocalDateTime getInteractionTime() {
        return interactionTime;
    }

    @Override
    public String toString() {
        return "UserInteraction{" +
                "id=" + userInteractionId +
                ", user='" + user.getId() + '\'' +
                ", itemType='" + itemType + '\'' +
                ", interactionTime='" + interactionTime + '\'' +
                '}';
    }
}
