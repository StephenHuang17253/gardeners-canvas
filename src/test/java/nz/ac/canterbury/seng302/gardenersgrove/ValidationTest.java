package nz.ac.canterbury.seng302.gardenersgrove;

import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.Validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

/**
 * Tests the Validation class
 */
public class ValidationTest {

    private static Validation validation;

    /**
     * Setup the mock for the UserService
     */
    @BeforeAll
    public static void setup() {
        UserService userServiceMock = Mockito.mock(UserService.class);
        Mockito.when(userServiceMock.emailInUse(Mockito.any())).thenReturn(false);
        validation = new Validation(userServiceMock);
    }

    /**
     * Test for valid names
     */ 
    @ParameterizedTest
    @ValueSource(strings = { "John Doe", "John-Doe", "John Doe's" })
    public void validateNameTest(String name) {
        Assertions.assertTrue(validation.validateName(name, true).isValid());
    }

    /**
     * Test for invalid names
     * @param name
     */
    @ParameterizedTest
    @ValueSource(strings = { "John1", "John>", "~John" })
    public void validateNameFalseTest(String name) {
        Assertions.assertFalse(validation.validateName(name, true).isValid());
    }

    /**
     * Test for valid emails
     * @param email
     */
    @ParameterizedTest
    @ValueSource(strings = { "test-test@example.com", "user_123@gmail.co.nz", "john.doe@hotmail.com" })
    public void validateEmailTest(String email) {
        Assertions.assertTrue(validation.validateEmail(email, false).isValid());
    }

    /**
     * Test for invalid emails
     * @param email
     */
    @ParameterizedTest
    @ValueSource(strings = { " ", "user_123gmail.co.nz", "john.doe@h." })
    public void validateEmailFalseTest(String email) {
        Assertions.assertFalse(validation.validateEmail(email, false).isValid());
    }

    /**
     * Test for valid passwords
     * @param password
     */
    @ParameterizedTest
    @ValueSource(strings = { "aB0!bbba", "##aBB0hhhhhhhhhh", "Passw0rd!" })
    public void validatePasswordTest(String password) {
        Assertions.assertTrue(validation.validatePassword(password).isValid());
    };

    /**
     * Test for invalid passwords
     * @param password
     */
    @ParameterizedTest
    @ValueSource(strings = { "aaa", "aaaaaaaa", "000!0000" })
    public void validatePasswordFalseTest(String password) {
        Assertions.assertFalse(validation.validatePassword(password).isValid());
    };

    /**
     * Test for valid DOB
     * @param dob
     */
    @ParameterizedTest
    @ValueSource(strings = { "01/01/2000", "01/12/1999", "31/12/2000" })
    public void isValidDOBTest(String dob) {
        Assertions.assertTrue(validation.validateDOB(dob).isValid());
    };
    
    /**
     * Test for invalid DOB
     * @param dob
     */
    @ParameterizedTest
    @ValueSource(strings = {  "-3/2/2023", "0/0/"})
    public void isValidDOBFalseTest(String dob) {
        Assertions.assertFalse(validation.validateDOB(dob).isValid());
    }
}
