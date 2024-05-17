package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

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
public class UserServiceTest {

    Logger logger = LoggerFactory.getLogger(UserServiceTest.class);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
    LocalDate date = LocalDate.parse("01/01/2001", formatter);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    private UserService userService;

    /**
     * Clear the repository and add a user to the repository. Then login the user
     */
    @BeforeEach
    void ClearRepository_AddUser_LoginUser() {
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

    /**
     * Test that the user is added to the repository
     */
    @Test
    public void AddedNewUser_UserInPersistence() {
        List<User> allUsers = userRepository.findAll();
        Assertions.assertEquals(allUsers.get(0).getFirstName(), "John");
        Assertions.assertEquals(allUsers.get(0).getLastName(), "Doe");
        Assertions.assertEquals(allUsers.get(0).getEmailAddress(), "johnDoe@email.com");
        Assertions.assertEquals(allUsers.get(0).getDateOfBirth(), date);
        Assertions.assertNotEquals(allUsers.get(0).getEncodedPassword(), "1es1P@ssword");
    }

    /**
     * Test that the user is updated in the repository and the number of users in
     * the repository is the same
     */
    @Test
    public void UpdateUserEmailAddress_SameNumberOfUsersInPersistence() {
        String fName = "John";
        String lName = "Doe";
        String email = "john@email.com";
        long id = userRepository.findByEmailAddressIgnoreCase("johnDoe@email.com")[0].getId();
        userService.updateUser(id, fName, lName, email, date);
        Assertions.assertEquals(userRepository.findAll().size(), 1);
    }

    /**
     * Test that the user is updated in the repository and the user's email address
     * and other values are updated
     */
    @Test
    public void UpdateAllUserDetails_AllDetailsUpdatedForUser() {
        String fName = "Jane";
        String lName = "Ode";
        String email = "janeOde@email.com";
        LocalDate newDate = LocalDate.parse("01/01/2000", formatter);
        long id = userRepository.findByEmailAddressIgnoreCase("johnDoe@email.com")[0].getId();
        userService.updateUser(id, fName, lName, email, newDate);
        List<User> allUsers = userRepository.findAll();
        Assertions.assertEquals(allUsers.get(0).getFirstName(), "Jane");
        Assertions.assertEquals(allUsers.get(0).getLastName(), "Ode");
        Assertions.assertEquals(allUsers.get(0).getEmailAddress(), "janeOde@email.com");
        Assertions.assertEquals(allUsers.get(0).getDateOfBirth(), newDate);

    }

}
