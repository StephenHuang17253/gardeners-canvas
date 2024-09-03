package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Token;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class TokenTest {

    private static User user;

    @BeforeAll
    static void setup() {
        user = mock(User.class);
    }

    @Test
    void tokenCreation_GetTokenString_ValidLength() {
        Duration lifeTime = Duration.ofHours(1);
        Token token = new Token(user, lifeTime);
        assertEquals(6, token.getTokenString().length());
    }

    @Test
    void tokenCreation_GetTokenString_ValidChars() {
        Duration lifeTime = Duration.ofHours(1);
        Token token = new Token(user, lifeTime);
        assertTrue(token.getTokenString().matches("[0-9A-Z]+"));
    }

    @Test
    void tokenCreation_DelayPastLifetime_IsExpired() throws InterruptedException {
        int lifetimeSeconds = 1;
        Duration lifetime = Duration.ofSeconds(lifetimeSeconds);
        Token token = new Token(user, lifetime);
        Thread.sleep(lifetimeSeconds * 1000);
        assertTrue(token.isExpired());
    }

    @Test
    void tokenCreation_DelayLessThanLifetime_IsNotExpired() throws InterruptedException {
        int lifetimeSeconds = 1;
        Duration lifeTime = Duration.ofSeconds(lifetimeSeconds);
        Token token = new Token(user, lifeTime);
        Thread.sleep(lifetimeSeconds * 1000 - 5);
        assertFalse(token.isExpired());
    }
}
