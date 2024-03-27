package nz.ac.canterbury.seng302.gardenersgrove;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.time.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Token;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;

public class TokenTest {

    private User user;

    @BeforeAll
    public static void setUp() {
        user = mock(User.class);
    }

    @Test
    void testTokenCreation_GetTokenString_ValidLength() {
        Duration lifeTime = Duration.ofHours(1);
        Token token = new Token(user, lifeTime);
        assertEquals(9, token.getTokenString().length());
    }

    @Test
    void testTokenCreation_GetTokenString_ValidContainsChars() {
        Duration lifeTime = Duration.ofHours(1);
        Token token = new Token(user, lifeTime);
        assertEquals(token.getTokenString(), "");
        assertTrue(token.getTokenString().matches("[0-9a-z]+"));
    }
}
