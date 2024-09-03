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
import nz.ac.canterbury.seng302.gardenersgrove.util.TagStatus;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class U21_AddTagToGarden {

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
        public HomePageLayoutRepository homePageLayoutRepository;

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

        String tagSuggestions = "/tag/suggestions";

        @Before
        public void before_or_after_all() {
                userService = new UserService(passwordEncoder, userRepository, homePageLayoutRepository);
                gardenService = new GardenService(gardenRepository, userService);
                friendshipService = new FriendshipService(friendshipRepository, userService);
                gardenTagService = new GardenTagService(gardenTagRepository, gardenTagRelationRepository);

                PublicGardensController publicGardensController = new PublicGardensController(gardenService,
                                securityService,
                                friendshipService, gardenTagService);

                mockMVCPublicGardens = MockMvcBuilders.standaloneSetup(publicGardensController).build();

                GardensController gardensController = new GardensController(gardenService, securityService,
                                plantService, weatherService, objectMapper, gardenTagService, profanityService);

                mockMVCGardens = MockMvcBuilders.standaloneSetup(gardensController).build();
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

                Garden publicGarden = gardenService
                                .addGarden(new Garden("A Public Garden with a Tag", "Tag Acceptance Test",
                                                "", "", "Christchurch", "", "New Zealand", 0.0, false, "", "", user));

                GardenTag testTag = new GardenTag("Veggies");
                testTag.setTagStatus(TagStatus.APPROPRIATE);
                testTag = gardenTagService.addGardenTag(testTag);

                gardenTagService.addGardenTagRelation(new GardenTagRelation(publicGarden, testTag));

                mvcResultPublicGardens = mockMVCPublicGardens.perform(
                                MockMvcRequestBuilders
                                                .get("/public-gardens/{gardenId}", publicGarden.getGardenId()))
                                .andExpect(status().isOk()).andReturn();
        }

        @Then("I see a list of tags that the garden has been marked with by its owner")
        public void i_see_a_list_of_tags_that_the_garden_has_been_marked_with_by_its_owner() {
                ModelAndView model = mvcResultPublicGardens.getModelAndView();
                Assertions.assertNotNull(model);
                List<String> tagsList = (List<String>) model.getModelMap()
                                .getAttribute("tagsList");
                Assertions.assertNotNull(tagsList);
                String tag = tagsList.get(0);
                Assertions.assertEquals("Veggies", tag);
                gardenTagRelationRepository.deleteAll();
                gardenTagRepository.deleteAll();
        }

        @When("I try to add an invalid tag {string} to my garden")
        public void i_try_to_add_a_tag_to_the_garden(String tag) throws Exception {
                mvcResultGardens = mockMVCGardens.perform(
                                MockMvcRequestBuilders
                                                .post("/my-gardens/{gardenId}/tag", garden.getGardenId())
                                                .param("tag", tag))
                                .andExpect(status().isOk()).andReturn();
        }

        @When("I enter a valid tag {string}")
        public void i_enter_a_valid_tag(String tag) throws Exception {
                mvcResultGardens = mockMVCGardens.perform(
                                MockMvcRequestBuilders
                                                .post("/my-gardens/{gardenId}/tag", garden.getGardenId())
                                                .param("tag", tag))
                                .andReturn();
        }

        @When("I previously added a tag {string}")
        public void i_previously_added_a_tag(String tag) throws Exception {
                gardenTagService.addGardenTag(new GardenTag(tag));
        }

        @And("I begin typing the tag {string}")
        public void i_begin_typing_a_tag(String query) throws Exception {
                gardenTagService.addGardenTag(new GardenTag("Garden"));
                gardenTagService.addGardenTag(new GardenTag("Vegetable Garden"));
                gardenTagService.addGardenTag(new GardenTag("Rose Garden"));

                gardenTagService.updateGardenTagStatus("Garden", TagStatus.APPROPRIATE);
                gardenTagService.updateGardenTagStatus("Vegetable Garden", TagStatus.APPROPRIATE);
                gardenTagService.updateGardenTagStatus("Rose Garden", TagStatus.APPROPRIATE);

                tagResult = mockMVCGardens.perform(
                                MockMvcRequestBuilders
                                                .get(tagSuggestions)
                                                .param("query", query))
                                .andReturn();
        }

        @Then("The following error message is displayed {string}")
        public void the_following_error_message_is_displayed(String errorMessage) {
                String tagErrorText = mvcResultGardens.getModelAndView().getModelMap().getAttribute("tagErrorText")
                                .toString();
                Assertions.assertEquals(errorMessage, tagErrorText);
        }

        @Then("The tag is not added to the garden")
        public void the_tag_is_not_added_to_the_garden() {
                List<GardenTagRelation> gardenTags = gardenTagService.getGardenTagRelationByGarden(garden);
                Assertions.assertTrue(gardenTags.isEmpty());
        }

        @Then("I see autocomplete options for existing tags")
        public void i_see_autocomplete_options_for_existing_tags()
                        throws JsonProcessingException, UnsupportedEncodingException {
                Assertions.assertNotNull(tagResult);
                String tagListResponse = tagResult.getResponse().getContentAsString();
                JsonNode jsonNode = objectMapper.readTree(tagListResponse);
                Assertions.assertEquals("Garden", jsonNode.get(0).get("tagName").asText());
                Assertions.assertEquals("Vegetable Garden", jsonNode.get(1).get("tagName").asText());
                Assertions.assertEquals("Rose Garden", jsonNode.get(2).get("tagName").asText());
                gardenTagRepository.deleteAll();
        }

        @Then("The tag is {string} added to my garden")
        public void the_tag_is_added_to_my_garden(String tagName) {
                List<GardenTagRelation> gardenTags = gardenTagService.getGardenTagRelationByGarden(garden);
                Assertions.assertNotNull(gardenTags);
                gardenTagService.updateGardenTagStatus(tagName, TagStatus.APPROPRIATE);
                String tag = String.valueOf(gardenTags.get(0).getTag().getTagName());
                Assertions.assertEquals(tagName, tag);
                Assertions.assertEquals(garden.getGardenName(), gardenTags.get(0).getGarden().getGardenName());
        }

        @Then("The tag is {string} added to my garden as pending")
        public void theTagIsAddedToMyGardenAsPending(String tagName) {
                List<GardenTagRelation> gardenTags = gardenTagService.getGardenTagRelationByGarden(garden);
                Assertions.assertNotNull(gardenTags);
                GardenTag tag = gardenTags.get(0).getTag();
                Assertions.assertEquals(tagName, tag.getTagName());
                Assertions.assertSame(TagStatus.PENDING, tag.getTagStatus());
                Assertions.assertEquals(garden.getGardenName(), gardenTags.get(0).getGarden().getGardenName());
        }

        @And("The tag {string} shows up in future autocomplete suggestions")
        public void the_tag_shows_up_in_future_autocomplete_suggestions(String tagName) throws Exception {
                String query = tagName;
                tagResult = mockMVCGardens.perform(
                                MockMvcRequestBuilders
                                                .get(tagSuggestions)
                                                .param("query", query))
                                .andReturn();

                String tagListResponse = tagResult.getResponse().getContentAsString();
                JsonNode jsonNode = objectMapper.readTree(tagListResponse);

                Assertions.assertEquals(tagName, jsonNode.get(0).get("tagName").asText());

                gardenTagRelationRepository.deleteAll();
                gardenTagRepository.deleteAll();
        }

        @And("The tag {string} is not shown in future autocomplete suggestions")
        public void the_tag_is_not_shown_in_future_autocomplete_suggestions(String query) throws Exception {
                tagResult = mockMVCGardens.perform(
                                MockMvcRequestBuilders
                                                .get(tagSuggestions)
                                                .param("query", query))
                                .andReturn();

                String tagListResponse = tagResult.getResponse().getContentAsString();
                Assertions.assertEquals("[]", tagListResponse);
        }
}
