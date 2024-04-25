package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.servlet.Filter;
import nz.ac.canterbury.seng302.gardenersgrove.config.SecurityConfig;
import nz.ac.canterbury.seng302.gardenersgrove.controller.ProfileController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SecurityConfig.class)
@WebAppConfiguration
@SpringBootTest
public class EditAUser {

    public MockMvc MOCK_MVC;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationManager authenticationManager;

    @Autowired
    public FileService fileService;
    public UserService userService;

    static String firstName = "John";
    static String lastName = "Doe";
    Boolean noLastName = false;
    static String emailAddress = "JohnDoe22@email.com";
    static LocalDate dateOfBirth = LocalDate.of(2001, 2, 2);
    MultipartFile validNameFile;

    @Before
    public void before_or_after_all() {
        userService = new UserService(passwordEncoder, userRepository);
        userRepository.deleteAll();
        userService.addUser(new User(firstName,
                        lastName,
                        emailAddress,
                        dateOfBirth),
                "1es1P@ssword");

        ProfileController profileController = new ProfileController(userService, authenticationManager, fileService);
        // Allows us to bypass spring security
        MOCK_MVC = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        String validFilename = "client_filename.png";
        int validFileSize = 10000000;
        byte[] validData = new byte[validFileSize];
        String contentType = "image/png";
        String filename = "local_filename.png";
        validNameFile = new MockMultipartFile(filename, validFilename, contentType, validData);
    }

    @BeforeEach
    public void setUpAuthentication() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(emailAddress, "1es1P@ssword");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


    @When("I click the \"Submit\" button")
    public void i_click_the_submit_button() throws Exception {
        MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .multipart("/profile/edit")
                        .file("profilePictureInput", validNameFile.getBytes())
                        .param("firstName", firstName)
                        .param("lastName", lastName)
                        .param("noLastName", String.valueOf(noLastName))
                        .param("dateOfBirth", String.valueOf(dateOfBirth))
                        .param("emailAddress", emailAddress)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(SecurityMockMvcRequestPostProcessors.user(emailAddress).password("1es1P@ssword"))
        );
    }

    @When("I enter valid values for first name {string}, last name {string}, email address {string}, and date of birth {string}")
    public void i_enter_valid_values(String fname, String lname, String email, String date) throws Exception {
        firstName = fname;
        lastName = lname;
        emailAddress = email;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dateOfBirth = LocalDate.parse(date, formatter);

    }
    @Then("No details are changed")
    public void no_details_are_changed() {
        Assertions.assertNotNull(userService.getUserByEmail(emailAddress));
        User u = userService.getUserByEmail(emailAddress);
        Assertions.assertEquals(firstName, u.getFirstName());
        Assertions.assertEquals(lastName, u.getLastName());
        Assertions.assertEquals(emailAddress, u.getEmailAddress());
        Assertions.assertEquals(dateOfBirth, u.getDateOfBirth());
    }

    @Then("My surname will become {string}")
    public void my_surname_will_become(String surname) {
        Assertions.assertNotNull(userService.getUserByEmail(emailAddress));
        User u = userService.getUserByEmail(emailAddress);
        Assertions.assertEquals(surname, u.getLastName());
    }

    @Then("I will be a user with first name {string}, last name {string}, email address {string}, and date of birth {string}")
    public void i_will_be_a_user_with_new_details(String fname, String lname, String email, String date) {
        Assertions.assertNotNull(userService.getUserByEmail(email));
        User u = userService.getUserByEmail(email);
        Assertions.assertEquals(fname, u.getFirstName());
        Assertions.assertEquals(lname, u.getLastName());
        Assertions.assertEquals(email, u.getEmailAddress());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate newDateOfBirth = LocalDate.parse(date, formatter);
        Assertions.assertEquals(newDateOfBirth, u.getDateOfBirth());
    }


    @When("I check the check box marked {string}")
    public void i_check_the_check_box_marked(String arg0) {
        noLastName = true;
    }

    @AfterAll
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }


}
