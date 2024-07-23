package nz.ac.canterbury.seng302.gardenersgrove.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;

public class UserTest {

    private User user;
    private String firstName = "John";
    private String lastName = "Doe";
    private String emailAddress = "johndoe@email.com";
    private LocalDate dateOfBirth = LocalDate.of(1990, 1, 1);

    private int banDurationSec = 1;

    @BeforeEach
    public void setup() {
        user = new User(firstName, lastName, emailAddress, dateOfBirth);
    }

    @Test
    public void newUser_IsNotBanned() {
        assertFalse(user.isBanned());
    }

    @Test
    public void banUser_IsBanned() {
        Duration banDuration = Duration.ofSeconds(banDurationSec);
        user.ban(banDuration);
        assertTrue(user.isBanned());
    }

    @Test
    public void banUser_WaitHalfDuration_IsBanned() throws InterruptedException {
        Duration banDuration = Duration.ofSeconds(banDurationSec);
        user.ban(banDuration);
        Thread.sleep((int) 0.5 * banDurationSec * 1000);
        assertTrue(user.isBanned());
    }

    @Test
    public void banUser_WaitFullDuration_IsNotBanned() throws InterruptedException {
        Duration banDuration = Duration.ofSeconds(banDurationSec);
        user.ban(banDuration);
        Thread.sleep(banDurationSec * 1000);
        assertFalse(user.isBanned());
    }

    @Test
    public void incrementStrikes_ThenReset_IsCorrectStrikes() {
        user.incrementStrikes();
        assertEquals(user.getStrikes(), 1);
        user.resetStrikes();
        assertEquals(user.getStrikes(), 0);
        user.incrementStrikes();
        assertEquals(user.getStrikes(), 1);
    }


}
