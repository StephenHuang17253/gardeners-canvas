package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class HomePageLayout {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "homePageLayout")
    private User user;

    @Column
    private boolean requestedFriends;

    @Column
    private boolean acceptedFriends;

    @Column
    private boolean recentPlants;

    @Column
    private boolean recentGardens;

    @Column
    private boolean notifications;

    public boolean isRequestedFriends() {
        return requestedFriends;
    }

    public boolean isAcceptedFriends() {
        return acceptedFriends;
    }

    public boolean isRecentPlants() {
        return recentPlants;
    }

    public boolean isRecentGardens() {
        return recentGardens;
    }

    public boolean isNotifications() {
        return notifications;
    }

    public void setRequestedFriends(boolean requestedFriends) {
        this.requestedFriends = requestedFriends;
    }

    public void setAcceptedFriends(boolean acceptedFriends) {
        this.acceptedFriends = acceptedFriends;
    }

    public void setRecentPlants(boolean recentPlants) {
        this.recentPlants = recentPlants;
    }

    public void setRecentGardens(boolean recentGardens) {
        this.recentGardens = recentGardens;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    /**
     * JPA required no-args constructor
     */
    public HomePageLayout() {
    }

    /**
     * Creates a new HomePageLayout object for the given user
     * 
     * @param user user to create the layout for
     */
    public HomePageLayout(User user) {
        this.user = user;
        this.requestedFriends = true;
        this.acceptedFriends = true;
        this.recentPlants = true;
        this.recentGardens = true;
        this.notifications = true;
    }

}
