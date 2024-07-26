package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * SpringBootTest being used to attempt to run integration tests between the
 * service class and the repository. {@link SpringBootTest} is being used
 * instead of @DataJpaTest
 */
@SpringBootTest
@Import(UserService.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserServiceTest {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
    LocalDate date = LocalDate.parse("01/01/2001", formatter);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    private UserService userService;

    @BeforeEach
    void clearRepository_AddUser_LoginUser() {
        userService = new UserService(passwordEncoder, userRepository);
        userRepository.deleteAll();
        userService.addUser(new User("John",
                "Doe",
                "johnDoe@email.com",
                date),
                "1es1P@ssword");
        // set up an authenticated user
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("johnDoe@email.com",
                "1es1P@ssword");

        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void addedNewUser_UserInPersistence() {
        List<User> allUsers = userRepository.findAll();
        assertEquals(allUsers.get(0).getFirstName(), "John");
        assertEquals(allUsers.get(0).getLastName(), "Doe");
        assertEquals(allUsers.get(0).getEmailAddress(), "johnDoe@email.com");
        assertEquals(allUsers.get(0).getDateOfBirth(), date);
        assertNotEquals(allUsers.get(0).getEncodedPassword(), "1es1P@ssword");
    }

    @Test
    void updateUserEmailAddress_SameNumberOfUsersInPersistence() {
        String fName = "John";
        String lName = "Doe";
        String email = "john@email.com";
        long id = userRepository.findByEmailAddressIgnoreCase("johnDoe@email.com")[0].getId();
        userService.updateUser(id, fName, lName, email, date);
        assertEquals(userRepository.findAll().size(), 1);
    }

    @Test
    void updateAllUserDetails_AllDetailsUpdatedForUser() {
        String fName = "Jane";
        String lName = "Ode";
        String email = "janeOde@email.com";
        LocalDate newDate = LocalDate.parse("01/01/2000", formatter);
        long id = userRepository.findByEmailAddressIgnoreCase("johnDoe@email.com")[0].getId();
        userService.updateUser(id, fName, lName, email, newDate);
        List<User> allUsers = userRepository.findAll();
        assertEquals(allUsers.get(0).getFirstName(), "Jane");
        assertEquals(allUsers.get(0).getLastName(), "Ode");
        assertEquals(allUsers.get(0).getEmailAddress(), "janeOde@email.com");
        assertEquals(allUsers.get(0).getDateOfBirth(), newDate);
    }

    @Test
    void createUser_UserNotBanned() {
        String fName = "Jane";
        String lName = "Ode";
        String email = "janeOde@email.com";
        LocalDate newDate = LocalDate.parse("01/01/2000", formatter);
        long id = userRepository.findByEmailAddressIgnoreCase("johnDoe@email.com")[0].getId();
        userService.updateUser(id, fName, lName, email, newDate);
        List<User> allUsers = userRepository.findAll();
        assertEquals(allUsers.get(0).getEmailAddress(), "janeOde@email.com");
        assertFalse(allUsers.get(0).isBanned());
    }

    @Test
    void createUser_BanThem_UserIsBanned() {
        String fName = "Jane";
        String lName = "Ode";
        String email = "janeOde@email.com";
        LocalDate newDate = LocalDate.parse("01/01/2000", formatter);
        long id = userRepository.findByEmailAddressIgnoreCase("johnDoe@email.com")[0].getId();
        User bannedUser = userService.updateUser(id, fName, lName, email, newDate);
        userService.banUser(bannedUser, 2);
        List<User> allUsers = userRepository.findAll();
        assertEquals(allUsers.get(0).getEmailAddress(), "janeOde@email.com");
        assertTrue(allUsers.get(0).isBanned());
    }

}
