package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class reflecting a registered user
 * Note the {@link Entity} annotation required for declaring this as a
 * persistence entity
 */
@Entity
@Table(name = "user_table")
public class User {

    private static final int STRIKES_TO_BAN = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 64)
    private String firstName;

    @Column(length = 64)
    private String lastName;

    @Column(nullable = false, unique = true, length = 320)
    private String emailAddress;

    @Column(nullable = false)
    private String password;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateOfBirth;

    @Column(length = 64)
    private String profilePictureFilename;

    @Column
    private boolean verified;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<Garden> gardens = new ArrayList<>();

    @Column
    private LocalDateTime lastBanDate;

    @Column
    private Duration banDuration;

    @Column
    private Integer strikes;

    /**
     * JPA required no-args constructor
     */
    protected User() {
    }

    /**
     * Creates a new User object
     *
     * @param firstName    first name of user
     * @param lastName     last name of user
     * @param emailAddress email of user
     * @param dateOfBirth  Date of Birth of user in dd/MM/yyyy format
     */
    public User(String firstName, String lastName, String emailAddress,
            LocalDate dateOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.dateOfBirth = dateOfBirth;
        this.verified = false;
        this.lastBanDate = null;
        this.banDuration = null;
        this.strikes = 0;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProfilePictureFilename(String profilePictureFilename) {
        this.profilePictureFilename = profilePictureFilename;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBanDuration(Duration banDuration) {
        this.banDuration = banDuration;
    }

    public void incrementStrikes() {
        strikes++;
    }

    public void resetStrikes() {
        strikes = 0;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getProfilePictureFilename() {
        return profilePictureFilename;
    }

    public String getEncodedPassword() {
        return password;
    }

    public boolean isVerified() {
        return verified;
    }

    public List<Garden> getGardens() {
        return gardens;
    }

    public int daysUntilUnban() {
        if (lastBanDate == null) {
            return 0;
        }
        LocalDateTime banEnds = lastBanDate.plus(banDuration);
        return (int) Duration.between(LocalDateTime.now(), banEnds).toDays();
    }

    public boolean isBanned() {
        if (lastBanDate == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(lastBanDate.plus(banDuration));
    }

    public void ban(Duration banDuration) {
        lastBanDate = LocalDateTime.now();
        this.banDuration = banDuration;
    }

    public int getStrikes() {
        return strikes;
    }

    /**
     * Returns a string representation of the user
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", profilePictureFilename='" + profilePictureFilename + '\'' +
                ", gardens='" + gardens + '\'' +
                ", verified='" + verified + '\'' +
                '}';
    }
}
