package nz.ac.canterbury.seng302.gardenersgrove.entity;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Entity class reflecting a registered user
 * Note the {@link Entity} annotation required for declaring this as a
 * persistence entity
 */
@Entity
@Table(name = "user_table")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
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

    /**
     * Returns a string representation of the user
     */
    @Override
    public String toString() {
        return "User{" +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", profilePictureFilename='" + profilePictureFilename + '\''+
                ", verified='" + verified + '\'' +
                '}';
    }
}
