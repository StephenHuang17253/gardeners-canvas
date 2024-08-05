package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardensController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.HomePageController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.PublicGardensController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTag;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTagRelation;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.*;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.TagStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class U24_BrowseGardensByTag {

    @Autowired
    public GardenRepository gardenRepository;

    @Autowired
    public GardenTagRepository gardenTagRepository;

    @Autowired
    public GardenTagRelationRepository gardenTagRelationRepository;

    @Autowired
    public PlantRepository plantRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public FriendshipRepository friendshipRepository;

    @Autowired
    public SecurityService securityService;

    @Autowired
    public UserInteractionService userInteractionService;

    public static MockMvc mockMVC;

    public static GardenService gardenService;

    public static UserService userService;

    public static PlantService plantService;

    public static FileService fileService;

    private static FriendshipService friendshipService;

    private static GardenTagService gardenTagService;


    private MvcResult mvcResult;


    private String currentPageUrl;
    private int lastPage;

    private String searchValue;
    private String appliedTagName;
    private Garden targetGarden;

    @Before
    public void before_or_after_all() {

        userService = new UserService(passwordEncoder, userRepository);
        gardenService = new GardenService(gardenRepository, userService);
        plantService = new PlantService(plantRepository, gardenService, fileService);
        friendshipService = new FriendshipService(friendshipRepository, userService);
        gardenTagService = new GardenTagService(gardenTagRepository, gardenTagRelationRepository);

        PublicGardensController publicGardensController = new PublicGardensController(gardenService, securityService,
                friendshipService, gardenTagService);

        // Allows us to bypass spring security
        mockMVC = MockMvcBuilders
                .standaloneSetup(publicGardensController)
                .build();
    }

    @And("The user {string} has a public garden called {string} that has the tag {string}")
    public void theUserHasAGardenCalledThatHasTheTag(String ownersEmail, String gardenName, String tag) {
        User user = userService.getUserByEmail(ownersEmail);
        Garden newGarden = new Garden(gardenName,
                "", "", "", "", "", "", 0.0, true, "", "",user);
        targetGarden = gardenService.addGarden(newGarden);
        GardenTag testTagInit = new GardenTag(tag);
        testTagInit.setTagStatus(TagStatus.APPROPRIATE);
        GardenTag testTag = gardenTagService.addGardenTag(testTagInit);
        GardenTagRelation relation = new GardenTagRelation(targetGarden, testTag);
        gardenTagService.addGardenTagRelation(relation);

        List<GardenTagRelation> savedRelations = gardenTagService.getGardenTagRelationByGarden(targetGarden);
        Assertions.assertFalse(savedRelations.isEmpty());
        Assertions.assertEquals(tag, savedRelations.get(0).getTag().getTagName());
    }

    @Given("I am on the browse garden page")
    public void iAmOnTheBrowseGardenPage() {
        currentPageUrl = "/public-gardens/search/1";
    }

    @And("I input search value {string}")
    public void iSearchInputSearchValue(String inputValue) {
        searchValue = inputValue;
    }

    @And("I apply the tag {string}")
    public void iApplyTheTag(String tag) {
        appliedTagName = tag;
    }

    @When("I submit the search with tag")
    public void iSubmitTheSearchWithTag() throws Exception {
        mvcResult = mockMVC.perform(
                        MockMvcRequestBuilders
                                .get(currentPageUrl)
                                .param("appliedTags", appliedTagName)
                                .param("searchInput", ""))
                .andExpect(status().isOk())
                .andReturn();
    }

    @When("I submit the search with both search and tag")
    public void iSubmitTheSearchWithBothSearchAndTag() throws Exception {
        mvcResult = mockMVC.perform(
                        MockMvcRequestBuilders
                                .get(currentPageUrl)
                                .param("appliedTags", appliedTagName)
                                .param("searchInput", searchValue))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("The search results contain the garden called {string}")
    public void theSearchResultsContainTheGardenCalled(String gardenName)  {
        Garden wantedGarden = gardenService.getMatchingGardens(gardenName).getFirst();
        ModelAndView model = mvcResult.getModelAndView();
        Assertions.assertNotNull(model);
        List<Garden> searchResults = (List<Garden>) model.getModelMap()
                .getAttribute("publicGardens");

        Assertions.assertFalse(searchResults.isEmpty());
        Assertions.assertEquals(searchResults.getFirst().toString(), wantedGarden.toString());
    }
}
