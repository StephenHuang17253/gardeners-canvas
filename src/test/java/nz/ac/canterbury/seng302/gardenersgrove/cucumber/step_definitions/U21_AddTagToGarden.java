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
import nz.ac.canterbury.seng302.gardenersgrove.controller.PublicGardensController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTag;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTagRelation;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.*;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Objects;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class U21_AddTagToGarden {

    Logger logger = LoggerFactory.getLogger(U21_AddTagToGarden.class);

    public static MockMvc mockMVCPublicGardens;

    public static MockMvc mockMVCGardens;

    @Autowired
    public GardenRepository gardenRepository;

    @Autowired
    public GardenTagRepository gardenTagRepository;

    @Autowired
    public GardenTagRelationRepository gardenTagRelationRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public FriendshipRepository friendshipRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationManager authenticationManager;

    @Autowired
    public SecurityService securityService;

    @Autowired
    public ObjectMapper objectMapper;

    public static GardenService gardenService;

    public static UserService userService;

    public static PlantService plantService;

    public static WeatherService weatherService;

    @Autowired
    public FriendshipService friendshipService;

    @Autowired
    public ProfanityService profanityService;

    public static GardenTagService gardenTagService;

    private MvcResult mvcResultPublicGardens;

    private MvcResult mvcResultGardens;
    private MvcResult tagResult;

    private Garden garden;

    @Before
    public void before_or_after_all() {

        userService = new UserService(passwordEncoder, userRepository);
        gardenService = new GardenService(gardenRepository, userService);
        friendshipService = new FriendshipService(friendshipRepository, userService);
        gardenTagService = new GardenTagService(gardenTagRepository, gardenTagRelationRepository);

        PublicGardensController publicGardensController = new PublicGardensController(gardenService, securityService,
                friendshipService, gardenTagService);

        mockMVCPublicGardens = MockMvcBuilders.standaloneSetup(publicGardensController).build();

        GardensController GardensController = new GardensController(gardenService, securityService,
                plantService, weatherService, objectMapper, gardenTagService, profanityService);

        mockMVCGardens = MockMvcBuilders.standaloneSetup(GardensController).build();

    }

    @Given("I as user {string} have a garden")
    public void i_have_a_garden(String userEmail) {
        User user = userService.getUserByEmail(userEmail);

        garden = gardenService.addGarden(new Garden("Garden to add Tag to", "Tag Acceptance Test",
                "", "", "Christchurch", "", "New Zealand", 0.0, false, "", "", user));
    }

    @Given("I access a garden details page for a public garden owned by {string}")
    public void i_access_a_garden_details_page_for_public_garden_owned_by(String userEmail) throws Exception {

        User user = userService.getUserByEmail(userEmail);

        Garden publicGarden = gardenService.addGarden(new Garden("A Public Garden with a Tag", "Tag Acceptance Test",
                "", "", "Christchurch", "", "New Zealand", 0.0, false, "", "", user));

        GardenTag testTag = gardenTagService.addGardenTag(new GardenTag("Veggies"));
        gardenTagService.addGardenTagRelation(new GardenTagRelation(publicGarden, testTag));

        mvcResultPublicGardens = mockMVCPublicGardens.perform(
                        MockMvcRequestBuilders
                                .get("/public-gardens/{gardenId}", publicGarden.getGardenId())
                                )
                        .andExpect(status().isOk()).andReturn();
    }

    @Then("I see a list of tags that the garden has been marked with by its owner")
    public void i_see_a_list_of_tags_that_the_garden_has_been_marked_with_by_its_owner(){
        List<String> tagsList = (List<String>) mvcResultPublicGardens.getModelAndView().getModelMap().getAttribute("tagsList");
        assert tagsList != null;
        String tag = tagsList.get(0);
        Assertions.assertTrue(Objects.equals(tag, "Veggies"));
        gardenTagRelationRepository.deleteAll();
        gardenTagRepository.deleteAll();
    }

    @When("I try to add an invalid tag {string} to my garden")
    public void i_try_to_add_a_tag_to_the_garden(String tag) throws Exception {

        mvcResultGardens = mockMVCGardens.perform(
                        MockMvcRequestBuilders
                                .post("/my-gardens/{gardenId}/tag", garden.getGardenId())
                                .param("tag", tag)
                        ).andExpect(status().isOk()).andReturn();
    }
//    AC5
    @When ("I enter a valid tag {string}")
    public void i_enter_a_valid_tag(String tag) throws Exception {
        mvcResultGardens = mockMVCGardens.perform(
                MockMvcRequestBuilders
                        .post("/my-gardens/{gardenId}/tag", garden.getGardenId())
                        .param("tag", tag)
        ).andExpect(status().is3xxRedirection()).andReturn();
    }

//    AC3
    @And("I begin typing the tag {string}")
    public void i_begin_typing_a_tag(String query) throws Exception {
        gardenTagService.addGardenTag(new GardenTag("Garden"));
        gardenTagService.addGardenTag(new GardenTag("Vegetable Garden"));
        gardenTagService.addGardenTag(new GardenTag("Rose Garden"));


        String fetchUrl = "/tag/suggestions";
        tagResult = mockMVCGardens.perform(
                MockMvcRequestBuilders
                        .get(fetchUrl)
                        .param("query", query)

        ).andReturn();
    }

    @Then("The following error message is displayed {string}")
    public void the_following_error_message_is_displayed(String errorMessage) {

        String tagErrorText = mvcResultGardens.getModelAndView().getModelMap().getAttribute("tagErrorText").toString();

        Assertions.assertEquals(errorMessage, tagErrorText);

    }

    @Then("The tag is not added to the garden")
    public void the_tag_is_not_added_to_the_garden() {

        List<GardenTagRelation> gardenTags = gardenTagService.getGardenTagRelationByGarden(garden);

        Assertions.assertTrue(gardenTags.isEmpty());

    }
// AC3
    @Then("I see autocomplete options for existing tags")
    public void i_see_autocomplete_options_for_existing_tags() throws JsonProcessingException, UnsupportedEncodingException {
        assert tagResult != null;
        String tagListResponse = tagResult.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(tagListResponse);

        logger.info(tagListResponse);

        Assertions.assertEquals("Garden", jsonNode.get(0).get("tagName").asText());
        Assertions.assertEquals("Vegetable Garden", jsonNode.get(1).get("tagName").asText());
        Assertions.assertEquals("Rose Garden", jsonNode.get(2).get("tagName").asText());

        gardenTagRepository.deleteAll();
    }
//    AC5
    @Then("the tag is added to my garden")
    public void the_tag_is_added_to_my_garden() {
        List<GardenTagRelation> gardenTags = gardenTagService.getGardenTagRelationByGarden(garden);
        Assertions.assertNotNull(gardenTags);

        String tag = String.valueOf(gardenTags.get(0).getTag().getTagName());
        Assertions.assertEquals("Cabbage Patch", tag);
        Assertions.assertEquals(garden.getGardenName(), gardenTags.get(0).getGarden().getGardenName());
    }

//    AC5
    @And("the tag shows up in future autocomplete suggestions")
    public void the_tag_shows_up_in_future_autocomplete_suggestions() throws Exception {
        String query = "Cabbage";
        String fetchUrl = "/tag/suggestions";
        tagResult = mockMVCGardens.perform(
                MockMvcRequestBuilders
                        .get(fetchUrl)
                        .param("query", query)

        ).andReturn();

        String tagListResponse = tagResult.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(tagListResponse);

        Assertions.assertEquals("Cabbage Patch", jsonNode.get(0).get("tagName").asText());

        gardenTagRelationRepository.deleteAll();
        gardenTagRepository.deleteAll();
    }


//    AC6, AC7
    @And("The tag {string} is not shown in future autocomplete suggestions")
    public void the_tag_is_not_shown_in_future_autocomplete_suggestions(String query) throws Exception {
        String fetchUrl = "/tag/suggestions";
        tagResult = mockMVCGardens.perform(
                MockMvcRequestBuilders
                        .get(fetchUrl)
                        .param("query", query)

        ).andReturn();

        String tagListResponse = tagResult.getResponse().getContentAsString();
        logger.info(tagListResponse);
        Assertions.assertEquals("[]", tagListResponse);
    }

}
