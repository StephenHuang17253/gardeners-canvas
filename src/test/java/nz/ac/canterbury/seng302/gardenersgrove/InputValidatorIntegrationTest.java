package nz.ac.canterbury.seng302.gardenersgrove;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidator.InputValidator;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;

import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidator.InputValidator;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidator.ValidationResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@SpringJUnitConfig
public class InputValidatorIntegrationTest {

    @Autowired
    InputValidator inputValidator;

    @Autowired
    UserService userService;

    @Test
    public void InputValidator_validateUniqueEmail_uniqueEmail_return_OK()
    {
        assertEquals(ValidationResult.OK,InputValidator.validateUniqueEmail("jondoe@gmail.com"));
    }

    @Test
    public void InputValidator_validateUniqueEmail_duplicatedEmail_return_NON_UNIQUE_EMAIL()
    {
        LocalDate testDOB = LocalDate.now();
        User testUser = new User("John", "lastName", "jondoe2@gmail.com", testDOB);
        userService.addUser(testUser,"123Password1!");
        assertEquals(ValidationResult.NON_UNIQUE_EMAIL,InputValidator.validateUniqueEmail("jondoe2@gmail.com"));
    }


}
