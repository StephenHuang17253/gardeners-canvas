package nz.ac.canterbury.seng302.gardenersgrove.entity;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String tokenString;

    private LocalDateTime creationDate;

    private Duration lifeTime;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id")
    private User user;

    /**
     * JPA required no-args constructor
     */
    // protected Token() {
    // }

    /**
     * Creates a new token string for a RegistrationToken object
     * by getting a list of random bytes and converting them to
     * a base 36 integer then a string
     * 
     * @param rnd the random number generator
     * @return the token string
     */
    public String generateTokenString(Random rnd) {

        // Generate a 6 byte number to ensure that a 9 character string is generated
        int numBytes = 6;

        byte[] randomBytes = new byte[numBytes];
        rnd.nextBytes(randomBytes);

        BigInteger largeRandomNumber = new BigInteger(randomBytes).abs();

        return largeRandomNumber.toString(36);
    }

    /**
     * Creates a new RegistrationToken object
     *
     * @param user     the user accociated with this token
     * @param lifeTime the life time that the token should be valid for
     */
    public Token(User user, Duration lifeTime) {

        Random rnd = new Random();
        this.tokenString = generateTokenString(rnd);
        this.creationDate = LocalDateTime.now();
        this.lifeTime = lifeTime;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public String getTokenString() {
        return tokenString;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public Duration getLifeTime() {
        return lifeTime;
    }

    public User getUser() {
        return user;
    }

    /**
     * Checks if the creation date plus the duration of the token's life time has
     * passed
     * 
     * @return true if the token is expired, false otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(creationDate.plus(lifeTime));
    }

    @Override
    public String toString() {
        return "RegistrationToken{" +
                "id=" + id +
                ", tokenString='" + tokenString + '\'' +
                ", creationDate=" + creationDate +
                ", lifeTime=" + lifeTime +
                ", user=" + user +
                '}';
    }
}
