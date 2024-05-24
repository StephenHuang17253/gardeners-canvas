package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.MyGardensController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
public class PubliciseGarden {

    public static MockMvc MOCK_MVC;

    @Autowired
    public GardenRepository gardenRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationManager authenticationManager;

    public SecurityService securityService;

    private static GardenService gardenService;

    private static UserService userService;

    private static PlantService plantService;

    private static FileService fileService;

    private User user;

    private Garden userGarden;


    @Before
    public void before_or_after_all() {
        userService = new UserService(passwordEncoder, userRepository);
        gardenService = new GardenService(gardenRepository, userService);
        securityService = new SecurityService(userService, authenticationManager);

        MyGardensController myGardensController = new MyGardensController(gardenService, securityService, plantService, fileService);
        MOCK_MVC = MockMvcBuilders.standaloneSetup(myGardensController).build();

    }

    @Given("User {string} is on my garden details page for {string}")
    public void userIsOnMyGardenDetailsPageFor(String userEmail, String garden) throws Exception {
       user = userService.getUserByEmail(userEmail);
       userGarden = user.getGardens().get(0);
    }

    @When("I mark a checkbox labelled \"Make my garden public\"")
    public void iMarkACheckboxLabelledMakeMyGardenPublic() throws Exception {
        Boolean isPublic = true;
        String gardenId = String.valueOf(user.getGardens().get(0).getGardenId());
        MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .get("/my-gardens/{gardenId}", gardenId)
                        .param("isPublic", String.valueOf(isPublic)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Then("My garden will be visible in search results")
    public void myGardenWillBeVisibleInSearchResults() {
        Assertions.assertTrue(userGarden.getIsPublic());
    }
}
