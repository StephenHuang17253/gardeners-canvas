package nz.ac.canterbury.seng302.gardenersgrove;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Token;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;

public class TokenTest {
    @Test
    void testTokenCreation() {
        User mockUser = mock(User.class);
        Duration lifeTime = Duration.ofHours(1);
        Token token = new Token(mockUser, lifeTime);
        assertEquals(token.getTokenString(), "");
    }
}
