package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import nz.ac.canterbury.seng302.gardenersgrove.controller.MyGardensController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
@SpringBootTest
public class ViewGarden {
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

    @Before
    public void before_or_after_all() {
        userService = new UserService(passwordEncoder, userRepository);
        gardenService = new GardenService(gardenRepository, userService);
        securityService = new SecurityService(userService, authenticationManager);

        MyGardensController myGardensController = new MyGardensController(gardenService, securityService, plantService, fileService);
        MOCK_MVC = MockMvcBuilders.standaloneSetup(myGardensController).build();


    }

    @Given("I as user {string} is on my garden details page for {string}")
    public void iAsUserIsOnMyGardenDetailsPageFor(String userEmail, String gardenName) throws Exception {
        User user = userService.getUserByEmail(userEmail);
        String gardenId = String.valueOf(user.getGardens().get(0).getGardenId());
        MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .get("/my-gardens/{gardenId}", gardenId)
        ).andExpect(MockMvcResultMatchers.status().isOk());
        
    }
}
