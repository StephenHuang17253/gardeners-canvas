package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.ProfileController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TokenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
public class addUserProfilePicture {


    Logger logger = LoggerFactory.getLogger(EditAUser.class);
    public MockMvc MOCK_MVC;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationManager authenticationManager;

    @Autowired
    public TokenRepository tokenRepository;

    public EmailService emailService;
    public FileService fileService;
    public UserService userService;

    String firstName = "John";
    String lastName = "Doe";
    Boolean noLastName = false;
    String emailAddress = "JohnDoe22@email.com";
    LocalDate dateOfBirth = LocalDate.of(2001, 2, 2);

    private static MockMultipartFile mockImage;


    @Before
    public void before_or_after_all() throws IOException {

    }

    @Given("I am logged in and on the profile page")
    public void i_am_logged_in_and_on_the_profile_page() throws IOException {
        emailService = Mockito.mock(EmailService.class);
        fileService = Mockito.mock(FileService.class);
        userService = Mockito.mock(UserService.class);

        Mockito.when(fileService.getAllFiles()).thenReturn(new String[] {});

        User mockUser = Mockito.mock(User.class);
        Mockito.when(mockUser.getId()).thenReturn(12L);
        Mockito.when(mockUser.getProfilePictureFilename()).thenReturn("oldImage.png");
        Mockito.when(mockUser.getFirstName()).thenReturn("Guy");
        Mockito.when(mockUser.getLastName()).thenReturn("Hes-lop");
        Mockito.when(mockUser.getEmailAddress()).thenReturn("guy@email.com");

        Mockito.when(userService.getUserByEmail(Mockito.any())).thenReturn(mockUser);


        ProfileController profileController = new ProfileController(authenticationManager,userService,
                fileService, emailService);

        // Allows us to bypass spring security
        MOCK_MVC = MockMvcBuilders
                .standaloneSetup(profileController)
                .build();
    }


    @Given("I choose a valid profile picture")
    public void i_choose_a_valid_profile_picture() {
        mockImage = new MockMultipartFile("profilePictureInput", "test.jpg",
                "image/jpg", "Not Actually a picture".getBytes());
    }

    @WithMockUser(username="johndoe.test@email.com")
    @When("I submit my profile picture")
    public void i_submit_my_profile_picture() throws Exception {
        MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .multipart("/profile").file(mockImage)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
        );
    }
    @Then("My profile picture is updated")
    public void my_profile_picture_is_updated() {
        Mockito.verify(userService, Mockito.atLeastOnce()).updateProfilePictureFilename(Mockito.anyString(),Mockito.anyLong());
    }
    @Given("I choose a non png nor jpg nor svg profile picture")
    public void i_choose_a_non_png_jpg_svg_profile_picture() {
        mockImage = new MockMultipartFile("profilePictureInput", "test.tif",
                "image/tif", "Not Actually a picture".getBytes());

    }
    @Then("My profile picture is not updated")
    public void my_profile_picture_is_not_updated() {
        Mockito.verify(userService, Mockito.never()).updateProfilePictureFilename(Mockito.anyString(),Mockito.anyLong());
    }
    @Given("I choose a profile picture larger than 10MB")
    public void i_choose_a_profile_picture_larger_than_10mb() {
        byte[] bytes = new byte[1024 * 1024 * 10];
        mockImage = new MockMultipartFile("profilePictureInput", "test.jpg",
                "image/jpg", bytes);

    }
}
