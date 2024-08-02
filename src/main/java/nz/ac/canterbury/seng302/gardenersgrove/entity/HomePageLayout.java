package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class HomePageLayout {

    @Id
    @Column(name = "layout_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    public boolean showRequestedFriends() {
        return requestedFriends;
    }

    public boolean showAcceptedFriends() {
        return acceptedFriends;
    }

    public boolean showRecentPlants() {
        return recentPlants;
    }

    public boolean showRecentGardens() {
        return recentGardens;
    }

    public boolean showNotifications() {
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
     * Creates a new HomePageLayout object
     * Also JPA required no-args constructor
     * 
     */
    public HomePageLayout() {
        // this.requestedFriends = true;
        // this.acceptedFriends = true;
        // this.recentPlants = true;
        // this.recentGardens = true;
        // this.notifications = true;
    }

    public HomePageLayout(boolean requestedFriends, boolean acceptedFriends, boolean recentPlants,
            boolean recentGardens, boolean notifications) {
        this.requestedFriends = requestedFriends;
        this.acceptedFriends = acceptedFriends;
        this.recentPlants = recentPlants;
        this.recentGardens = recentGardens;
        this.notifications = notifications;
    }

}
