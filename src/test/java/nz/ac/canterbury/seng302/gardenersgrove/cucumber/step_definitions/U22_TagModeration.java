package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTag;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenTagService;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.util.TagStatus;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class U22_TagModeration {

    @Autowired
    private ProfanityService profanityService;

    @Autowired
    private GardenTagService gardenTagService;

    @Autowired
    private UserService userService;

    private int strikesBefore;

    @Given("My Tag {string} contained profanity")
    public void my_tag_contained_profanity(String tagName) {
        Mockito.when(profanityService.containsProfanity(Mockito.anyString(), Mockito.any())).thenReturn(true);
        gardenTagService.updateGardenTagStatus(tagName, TagStatus.INAPPROPRIATE);
    }

    @Given("My Tag {string} did not contain profanity")
    public void my_tag_did_not_contain_profanity(String tagName) {

        Mockito.when(profanityService.containsProfanity(Mockito.anyString(), Mockito.any())).thenReturn(false);
        gardenTagService.updateGardenTagStatus(tagName, TagStatus.APPROPRIATE);
    }

    @And("I have previously made a tag called {string}")
    public void iHavePreviouslyMadeATagCalled(String tagName) {

        if(gardenTagService.getByName(tagName).isEmpty())
        {
            gardenTagService.addGardenTag(new GardenTag(tagName));
        }
    }

    // U22 AC5
    @Given("I as user {string} currently have {int} strikes")
    public void i_as_user_currently_have_x_strikes(String userEmail, int strikes) {
        // This is just to check the number of strikes the user had
        // So it can be compared with later
        User user = userService.getUserByEmail(userEmail);
        for (int i = 0; i < strikes; i++) {
            userService.strikeUser(user);
        }
        strikesBefore = user.getStrikes();
        Assertions.assertEquals(strikes, user.getStrikes());

    }

    @And("My Tag {string} is currently pending moderation")
    public void myTagIsCurrentlyPendingModeration(String tagName) {
        Mockito.when(profanityService.containsProfanity(Mockito.anyString(), Mockito.any())).thenReturn(false);
        gardenTagService.updateGardenTagStatus(tagName, TagStatus.PENDING);
    }

    // U22 AC5
    @Then("I {string} get a strike")
    public void i_get_a_strike(String userEmail) {
        int strikes_now = userService.getUserByEmail(userEmail).getStrikes();
        Assertions.assertEquals(strikesBefore + 1, strikes_now);
    }


}
