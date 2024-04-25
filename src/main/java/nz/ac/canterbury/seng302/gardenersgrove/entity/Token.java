package nz.ac.canterbury.seng302.gardenersgrove.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;

/**
 * Entity class reflecting a token used for registration/verification
 * Note the {@link Entity} annotation required for declaring this as a
 * persistence entity
 */
@Entity
public class Token {
    @Transient
    private static final int TOKEN_LENGTH = 6;

    @Transient
    private static final int TOKEN_LIFETIME = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String tokenString;

    @Column
    private LocalDateTime creationDate;

    @Column
    private Duration lifetime;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * JPA required no-args constructor
     */
    protected Token() {
    }

    /**
     * Creates a new token string for a Token object with a given length
     * 
     * @param length the length of the token string
     * @return the token string
     */
    private String generateTokenString(int length) {
        String alphanumericChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder token = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(alphanumericChars.length());
            token.append(alphanumericChars.charAt(index));
        }

        return token.toString();
    }

    /**
     * Creates a new Token object
     *
     * @param user     the user accociated with this token
     * @param lifetime the lifetime that the token should be valid for, or null to
     *                 use default value
     */
    public Token(User user, Duration lifetime) {
        this.tokenString = generateTokenString(TOKEN_LENGTH);
        this.creationDate = LocalDateTime.now();
        if (lifetime == null) {
            this.lifetime = Duration.ofMinutes(TOKEN_LIFETIME);
        } else {
            this.lifetime = lifetime;
        }
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

    public Duration getLifetime() {
        return lifetime;
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
        return LocalDateTime.now().isAfter(creationDate.plus(lifetime));
    }

    @Override
    public String toString() {
        return "Token{" +
                "id=" + id +
                ", tokenString='" + tokenString + '\'' +
                ", creationDate=" + creationDate +
                ", lifetime=" + lifetime +
                ", user=" + user +
                '}';
    }
}
