package nz.ac.canterbury.seng302.gardenersgrove.unit;

import jakarta.mail.MessagingException;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SecurityServiceUnitTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private FriendshipService friendshipService;

    @Mock
    private UserInteractionService userInteractionService;

    @Mock
    private EmailService emailService;

    static SecurityService securityService;

    private static final int NUM_STRIKES_FOR_WARN = 5;

    private static final int NUM_STRIKES_FOR_BAN = 6;

    private User user;

    private final Long userId = 1L;

    @BeforeAll
    void before_or_after_all() {
        userService = Mockito.mock(UserService.class);
        emailService = Mockito.mock(EmailService.class);
        user = Mockito.spy(new User("username", "password", "email@example.com", null));
        Mockito.when(userService.getUserById(userId)).thenReturn(user);
        securityService = new SecurityService(userService, authenticationManager, friendshipService,
                userInteractionService, emailService);
    }

    @Test
    void handelStrikeUser_UserNotInRepo_ThrowsIllegalArgumentException() {
        User nonRepoUser = new User("", "", "", null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> securityService.handleStrikeUser(nonRepoUser));
    }

    @Test
    void handelStrikeUser_UserInRepoAndFirstStrike_returnsInteger() {
        user.setStrikes(0);
        Mockito.when(user.getId()).thenReturn(userId);
        Mockito.when(userService.getUserById(userId)).thenReturn(user);
        Mockito.doAnswer(invocation -> {
            user.setStrikes(user.getStrikes() + 1);
            return null;
        }).when(userService).strikeUser(user);
        securityService.handleStrikeUser(user);
        Assertions.assertEquals(1, user.getStrikes());
    }

    @Test
    void handelStrikeUser_UserInRepoAndWarnStrike_returnsInteger() throws MessagingException {
        user.setStrikes(4);
        Mockito.when(user.getId()).thenReturn(userId);
        Mockito.when(userService.getUserById(userId)).thenReturn(user);
        Mockito.doAnswer(invocation -> {
            user.setStrikes(user.getStrikes() + 1);
            return null;
        }).when(userService).strikeUser(user);
        securityService.handleStrikeUser(user);
        Assertions.assertEquals(NUM_STRIKES_FOR_WARN, user.getStrikes());
    }

    @Test
    void handelStrikeUser_UserInRepoAndBanStrike_returnsInteger() {
        user.setStrikes(NUM_STRIKES_FOR_WARN);
        Mockito.when(user.getId()).thenReturn(userId);
        Mockito.when(userService.getUserById(userId)).thenReturn(user);
        Mockito.doAnswer(invocation -> {
            user.setStrikes(user.getStrikes() + 1);
            return null;
        }).when(userService).strikeUser(user);
        securityService.handleStrikeUser(user);
        Assertions.assertEquals(NUM_STRIKES_FOR_BAN, user.getStrikes());

    }
}
