package nz.ac.canterbury.seng302.gardenersgrove.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.time.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Token;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;

public class TokenTest {

    private static User user;

    @BeforeAll
    public static void setup() {
        user = mock(User.class);
    }

    @Test
    public void tokenCreation_GetTokenString_ValidLength() {
        Duration lifeTime = Duration.ofHours(1);
        Token token = new Token(user, lifeTime);
        assertEquals(6, token.getTokenString().length());
    }

    @Test
    public void tokenCreation_GetTokenString_ValidChars() {
        Duration lifeTime = Duration.ofHours(1);
        Token token = new Token(user, lifeTime);
        assertTrue(token.getTokenString().matches("[0-9A-Z]+"));
    }

    @Test
    public void tokenCreation_DelayPastLifetime_IsExpired() throws InterruptedException {
        int lifetimeSeconds = 1;
        Duration lifetime = Duration.ofSeconds(lifetimeSeconds);
        Token token = new Token(user, lifetime);
        Thread.sleep(lifetimeSeconds * 1000);
        assertTrue(token.isExpired());
    }

    @Test
    public void tokenCreation_DelayLessThanLifetime_IsNotExpired() throws InterruptedException {
        int lifetimeSeconds = 1;
        Duration lifeTime = Duration.ofSeconds(lifetimeSeconds);
        Token token = new Token(user, lifeTime);
        Thread.sleep(lifetimeSeconds * 1000 - 5);
        assertFalse(token.isExpired());
    }
}
