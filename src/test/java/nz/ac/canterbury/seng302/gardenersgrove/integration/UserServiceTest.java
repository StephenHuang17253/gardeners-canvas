package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GridItemLocationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TokenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SpringBootTest being used to attempt to run integration tests between the
 * service class and the repository. {@link SpringBootTest} is being used
 * instead of @DataJpaTest
 */
@SpringBootTest
@Import(UserService.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private GridItemLocationRepository gridItemLocationRepository;

    @Autowired
    private UserService userService;

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);

    private static String email1 = "johnDoe@email.com";
    private static String password1 = "1es1P@ssword";
    private static String fName1 = "John";
    private static String lName1 = "Doe";
    private static LocalDate date1 = LocalDate.parse("01/01/2001", formatter);

    private static String email2 = "janeOde@email.com";
    private static String fName2 = "Jane";
    private static String lName2 = "Ode";
    private static LocalDate date2 = LocalDate.parse("01/01/2000", formatter);

    private static int banDuration = 2;

    @BeforeEach
    void clearRepository_AddUser_LoginUser() {
        tokenRepository.deleteAll();
        gridItemLocationRepository.deleteAll();
        userRepository.deleteAll();
        User user = new User(fName1, lName1, email1, date1);
        userService.addUser(user, password1);
    }

    @Test
    void addedNewUser_UserInPersistence() {
        List<User> allUsers = userRepository.findAll();
        assertEquals(1, allUsers.size());
        User foundUser = allUsers.get(0);

        assertEquals(fName1, foundUser.getFirstName());
        assertEquals(lName1, foundUser.getLastName());
        assertEquals(email1, foundUser.getEmailAddress());
        assertEquals(date1, foundUser.getDateOfBirth());
        assertNotEquals(password1, foundUser.getEncodedPassword());
    }

    @Test
    void updateUserEmailAddress_SameNumberOfUsersInPersistence() {
        User[] emailUsers = userRepository.findByEmailAddressIgnoreCase(email1);
        assertEquals(1, emailUsers.length);
        User user = emailUsers[0];
        long id = user.getId();
        userService.updateUser(id, fName1, lName1, email1, date1);
        assertEquals(1, userRepository.findAll().size());
    }

    @Test
    void updateAllUserDetails_AllDetailsUpdatedForUser() {

        User[] emailUsers = userRepository.findByEmailAddressIgnoreCase(email1);
        assertEquals(1, emailUsers.length);
        User user = emailUsers[0];
        long id = user.getId();

        User updatedUser = userService.updateUser(id, fName2, lName2, email2, date2);

        assertEquals(fName2, updatedUser.getFirstName());
        assertEquals(lName2, updatedUser.getLastName());
        assertEquals(email2, updatedUser.getEmailAddress());
        assertEquals(date2, updatedUser.getDateOfBirth());
    }

    @Test
    void createUser_UserNotBanned() {
        User[] emailUsers = userRepository.findByEmailAddressIgnoreCase(email1);
        assertEquals(1, emailUsers.length);
        User user = emailUsers[0];
        assertEquals(email1, user.getEmailAddress());
        assertFalse(user.isBanned());
    }

    @Test
    void createUser_BanThem_UserIsBanned() {
        User[] emailUsers = userRepository.findByEmailAddressIgnoreCase(email1);
        assertEquals(1, emailUsers.length);
        User user = emailUsers[0];
        assertEquals(email1, user.getEmailAddress());
        assertFalse(user.isBanned());
        userService.banUser(user, banDuration);
        assertTrue(user.isBanned());
        assertEquals(banDuration, user.daysUntilUnban());
    }

    @Test
    void createUser_GiveStrike_UserHasStrike() {
        User[] emailUsers = userRepository.findByEmailAddressIgnoreCase(email1);
        assertEquals(1, emailUsers.length);
        User user = emailUsers[0];
        assertEquals(0, user.getStrikes());
        userService.strikeUser(user);
        assertEquals(1, user.getStrikes());
    }

    @Test
    void userHadStrikes_GetsBanned_StrikesCleared() {
        User[] emailUsers = userRepository.findByEmailAddressIgnoreCase(email1);
        assertEquals(1, emailUsers.length);
        User user = emailUsers[0];
        assertEquals(email1, user.getEmailAddress());
        assertEquals(0, user.getStrikes());
        userService.strikeUser(user);
        assertEquals(1, user.getStrikes());
        userService.banUser(user, banDuration);
        assertTrue(user.isBanned());
        assertEquals(0, user.getStrikes());
    }

}
