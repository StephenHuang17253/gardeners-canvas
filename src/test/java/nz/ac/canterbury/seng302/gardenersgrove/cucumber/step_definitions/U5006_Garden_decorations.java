package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONArray;
import nz.ac.canterbury.seng302.gardenersgrove.controller.Garden2DController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Decoration;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GridItemLocation;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.*;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.DecorationCategory;
import nz.ac.canterbury.seng302.gardenersgrove.util.GridItemType;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

        Assertions.assertTrue(model.containsKey("displayableItemsList"));
    }

    @When("I As user {string} place a decoration on my grid and press save")
    public void iAsUserPlaceADecorationOnMyGridAndPressSave(String email) {
        user = userService.getUserByEmail(email);
        garden = user.getGardens().get(0);
        Decoration gnome = new Decoration(garden, DecorationCategory.GNOME);
        decorationService.addDecoration(gnome);
        Assertions.assertEquals(DecorationCategory.GNOME, decorationService.getDecorationsByGarden(garden).get(0).getDecorationCategory());
    }

    @Then("I see my placed decoration")
    public void iSeeMyPlacedDecoration() throws Exception {
        Long gardenId = garden.getGardenId();
        Decoration decoration = decorationService.getDecorations().getFirst();

        List<String> idList = new ArrayList<>();
        idList.add(decoration.getId().toString());

        List<String> typeList = new ArrayList<>();
        typeList.add("DECORATION");

        List<Double> xCoordList = new ArrayList<>();
        xCoordList.add(2.0);

        List<Double> yCoordList = new ArrayList<>();
        yCoordList.add(3.0);

        mockMVC.perform(MockMvcRequestBuilders.post("/2D-garden/" + gardenId + "/save").with(csrf())
                        .param("idList", JSONArray.toJSONString(idList))
                        .param("typeList", JSONArray.toJSONString(typeList))
                        .param("xCoordList", JSONArray.toJSONString(xCoordList))
                        .param("yCoordList", JSONArray.toJSONString(yCoordList)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andReturn();


        Optional<GridItemLocation> gridItemAddedToRepository = gridItemLocationRepository
                .findGridItemLocationByObjectIdAndItemTypeAndGarden(decoration.getId(),
                        GridItemType.DECORATION,
                        gardenService.getGardenById(gardenId).get());
        Assertions.assertTrue(gridItemAddedToRepository.isPresent());
    }
}
