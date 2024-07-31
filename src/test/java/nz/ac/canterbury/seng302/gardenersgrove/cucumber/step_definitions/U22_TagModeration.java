package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardensController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTag;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenTagService;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityService;
import nz.ac.canterbury.seng302.gardenersgrove.util.TagStatus;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class U22_TagModeration {

    @Autowired
    private ProfanityService profanityService;


    @Autowired
    private GardenTagService gardenTagService;

    @Given("My Tag {string} contained profanity")
    public void my_tag_contained_profanity(String tagName) {
        Mockito.when(profanityService.containsProfanityLowPriority(Mockito.anyString())).thenReturn(true);
        gardenTagService.updateGardenTagStatus(tagName, TagStatus.INAPPROPRIATE);
    }

    @Given("My Tag {string} did not contain profanity")
    public void my_tag_did_not_contain_profanity(String tagName) {

        Mockito.when(profanityService.containsProfanityLowPriority(Mockito.anyString())).thenReturn(false);
        gardenTagService.updateGardenTagStatus(tagName, TagStatus.APPROPRIATE);
    }

    @And("I have previously made a tag called {string}")
    public void iHavePreviouslyMadeATagCalled(String tagName) {

        if(gardenTagService.getByName(tagName).isEmpty())
        {
            gardenTagService.addGardenTag(new GardenTag(tagName));
        }

    }
}
