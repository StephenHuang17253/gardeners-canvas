package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import nz.ac.canterbury.seng302.gardenersgrove.controller.Garden2DController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.*;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class U5006_Garden_decorations {
    public static MockMvc mockMVC;
    private MvcResult mvcResult;
    private Map<String, Object> model;
    private Garden garden;

    private User user;

    @Autowired
    public GardenRepository gardenRepository;

    @Autowired
    public PlantRepository plantRepository;

    @Autowired
    public HomePageLayoutRepository homePageLayoutRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public SecurityService securityService;

    @Autowired
    public PlantService plantService;

    @Autowired
    public FileService fileService;

    @Autowired
    public GridItemLocationService gridItemLocationService;
    @Autowired
    public GridItemLocationRepository gridItemLocationRepository;

    public static GardenService gardenService;

    public static UserService userService;
    public static DecorationService decorationService;
    @Autowired
    public DecorationRepository decorationRepository;

    @Before
    public void before_or_after_all() {
        gardenService = new GardenService(gardenRepository, userService);
        userService = new UserService(passwordEncoder, userRepository, homePageLayoutRepository);
        gridItemLocationService = new GridItemLocationService(gridItemLocationRepository);
        fileService = new FileService();
        plantService = new PlantService(plantRepository, gardenService, fileService);
        decorationService = new DecorationService(decorationRepository);

        Garden2DController garden2DController = new Garden2DController(gardenService, securityService,
                gridItemLocationService, plantService, decorationService);
        mockMVC = MockMvcBuilders.standaloneSetup(garden2DController).build();
    }
    @Then("As user {string} I see a palette window with a tab labelled for decorations")
    public void asUserISeeAPaletteWindowWithATabLabelledForDecorations(String email) throws Exception {
        user = userService.getUserByEmail(email);
        garden = user.getGardens().get(0);
        mvcResult = mockMVC.perform(
                        MockMvcRequestBuilders
                                .get("/2D-garden/{gardenId}", garden.getGardenId()))
                .andExpect(status().isOk()).andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        Assertions.assertNotNull(modelAndView);
        model = modelAndView.getModel();

        // palette window page
        Assertions.assertTrue(model.containsKey("displayableItemsList"));

        // plants
        Assertions.assertTrue(model.containsKey("plants"));
    }
}
