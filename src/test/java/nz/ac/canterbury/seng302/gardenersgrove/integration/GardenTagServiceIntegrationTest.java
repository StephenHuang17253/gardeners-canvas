package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTag;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTagRelation;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenTagRelationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenTagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenTagService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.util.TagStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class GardenTagServiceIntegrationTest {

    @Autowired
    private GardenTagRepository gardenTagRepository;

    @Autowired
    private GardenTagRelationRepository gardenTagRelationRepository;

    @Autowired
    private GardenService gardenService;

    @Autowired
    private UserService userService;

    private GardenTagService gardenTagService;

    private Garden testGarden;
    private Garden testGarden2;
    private Garden testGarden3;
    private User owner;

    @BeforeEach
    void prepare_test() {
        gardenTagRelationRepository.deleteAll();
        gardenTagRepository.deleteAll();

        gardenTagService = new GardenTagService(gardenTagRepository, gardenTagRelationRepository);

        if (!userService.emailInUse("GardenServiceIntegrationTest@ProfileController.com")) {
            owner = new User("John", "Test", "GardenServiceIntegrationTest@ProfileController.com",
                    LocalDate.of(2003, 5, 2));
            userService.addUser(owner, "cheeseCake");
        }
        if (testGarden == null) {
            testGarden = new Garden(
                    "John's Garden",
                    "",
                    "114 Ilam Road",
                    "Ilam",
                    "Christchurch",
                    "8041",
                    "New Zealand",
                    15.0,
                    false,
                    "-43.5214643",
                    "172.5796159",
                    userService.getUserByEmail("GardenServiceIntegrationTest@ProfileController.com"));
            gardenService.addGarden(testGarden);
        }
        if (testGarden2 == null) {
            testGarden2 = new Garden(
                    "John's Garden",
                    "",
                    "114 Ilam Road",
                    "Ilam",
                    "Christchurch",
                    "8041",
                    "New Zealand",
                    15.0,
                    false,
                    "-43.5214643",
                    "172.5796159",
                    userService.getUserByEmail("GardenServiceIntegrationTest@ProfileController.com"));
            gardenService.addGarden(testGarden2);
        }
        if (testGarden3 == null) {
            testGarden3 = new Garden(
                    "John's Garden",
                    "",
                    "114 Ilam Road",
                    "Ilam",
                    "Christchurch",
                    "8041",
                    "New Zealand",
                    15.0,
                    false,
                    "-43.5214643",
                    "172.5796159",
                    userService.getUserByEmail("GardenServiceIntegrationTest@ProfileController.com"));
            gardenService.addGarden(testGarden3);
        }

    }

    @Test
    void simpleFetchGardenTag_OnId() {
        GardenTag tag1 = new GardenTag("Test");
        Long saveGardenId = gardenTagService.addGardenTag(tag1).getId();

        try {
            Assertions.assertEquals("Test", gardenTagService.getGardenTabById(saveGardenId).get().getTagName());
        } catch (NoSuchElementException exception) {
            fail(exception);
        }
    }

    @Test
    void doubleAddNewTag_throwsArgumentException() {
        GardenTag tag1 = new GardenTag("Test");
        GardenTag tag2 = new GardenTag("Test");
        gardenTagService.addGardenTag(tag1);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            gardenTagService.addGardenTag(tag2);
        });

    }

    @Test
    void simpleFetchGardenTag_OnId_withOtherData() {
        GardenTag tag1 = new GardenTag("Test");
        GardenTag tag2 = new GardenTag("Test2");
        GardenTag tag3 = new GardenTag("Test3");
        GardenTag tag4 = new GardenTag("Test4");
        GardenTag tag5 = new GardenTag("Test5");
        GardenTag tag6 = new GardenTag("Test6");

        Long saveGardenId = gardenTagService.addGardenTag(tag1).getId();
        gardenTagService.addGardenTag(tag2);
        gardenTagService.addGardenTag(tag3);
        gardenTagService.addGardenTag(tag4);
        gardenTagService.addGardenTag(tag5);
        gardenTagService.addGardenTag(tag6);

        try {
            Assertions.assertEquals("Test", gardenTagService.getGardenTabById(saveGardenId).get().getTagName());
        } catch (NoSuchElementException exception) {
            fail(exception);
        }
    }

    @Test
    void simpleFetchGardenTag_OnName() {
        GardenTag tag1 = new GardenTag("Test2");
        gardenTagService.addGardenTag(tag1).getId();

        try {
            Assertions.assertEquals("Test2", gardenTagService.getByName("Test2").get().getTagName());
        } catch (NoSuchElementException exception) {
            fail(exception);
        }
    }

    @Test
    void simpleFetchGardenTag_OnName_withOtherData() {
        GardenTag tag1 = new GardenTag("Test");
        GardenTag tag2 = new GardenTag("Test2");
        GardenTag tag3 = new GardenTag("Test3");
        GardenTag tag4 = new GardenTag("Test4");
        GardenTag tag5 = new GardenTag("Test5");
        GardenTag tag6 = new GardenTag("Test6");
        gardenTagService.addGardenTag(tag1);
        gardenTagService.addGardenTag(tag2);
        gardenTagService.addGardenTag(tag3);
        gardenTagService.addGardenTag(tag4);
        gardenTagService.addGardenTag(tag5);
        gardenTagService.addGardenTag(tag6);

        try {
            Assertions.assertEquals("Test", gardenTagService.getByName("Test").get().getTagName());
        } catch (NoSuchElementException exception) {
            fail(exception);
        }
    }

    @Test
    void simpleFetchGardenTag_OnSimilarName() {
        GardenTag tag1 = new GardenTag("Test2");
        gardenTagService.addGardenTag(tag1).getId();

        try {
            Assertions.assertEquals("Test2", gardenTagService.getAllSimilar("te").get(0).getTagName());
        } catch (NoSuchElementException exception) {
            fail(exception);
        }
    }

    @Test
    void simpleFetchGardenTag_OnSimilarName_withMultipleNames() {
        GardenTag tag1 = new GardenTag("Test");
        GardenTag tag2 = new GardenTag("Test2");
        GardenTag tag3 = new GardenTag("Test3");
        GardenTag tag4 = new GardenTag("Test4");
        GardenTag tag5 = new GardenTag("Test5");
        GardenTag tag6 = new GardenTag("Test6");
        GardenTag tag7 = new GardenTag("redHerring");
        HashSet<String> gardenTagHashSet = new HashSet<>();
        gardenTagHashSet.add(tag1.getTagName());
        gardenTagHashSet.add(tag2.getTagName());
        gardenTagHashSet.add(tag3.getTagName());
        gardenTagHashSet.add(tag4.getTagName());
        gardenTagHashSet.add(tag5.getTagName());
        gardenTagHashSet.add(tag6.getTagName());

        gardenTagService.addGardenTag(tag1);
        gardenTagService.addGardenTag(tag2);
        gardenTagService.addGardenTag(tag3);
        gardenTagService.addGardenTag(tag4);
        gardenTagService.addGardenTag(tag5);
        gardenTagService.addGardenTag(tag6);
        gardenTagService.addGardenTag(tag7);

        try {
            List<GardenTag> similarSearchResult = gardenTagService.getAllSimilar("test");
            Assertions.assertEquals(6, similarSearchResult.size());
            for (GardenTag tag : similarSearchResult) {
                Assertions.assertTrue(gardenTagHashSet.contains(tag.getTagName()));
                gardenTagHashSet.remove(tag.getTagName());
            }

        } catch (NoSuchElementException exception) {
            fail(exception);
        }
    }

    @Test
    void simpleFetchGardenTag_getAll_withMultipleNames() {
        GardenTag tag1 = new GardenTag("Test");
        GardenTag tag2 = new GardenTag("Test2");
        GardenTag tag3 = new GardenTag("Test3");
        GardenTag tag4 = new GardenTag("Test4");
        GardenTag tag5 = new GardenTag("Test5");
        GardenTag tag6 = new GardenTag("Test6");
        GardenTag tag7 = new GardenTag("redHerring");
        HashSet<String> gardenTagHashSet = new HashSet<>();
        gardenTagHashSet.add(tag1.getTagName());
        gardenTagHashSet.add(tag2.getTagName());
        gardenTagHashSet.add(tag3.getTagName());
        gardenTagHashSet.add(tag4.getTagName());
        gardenTagHashSet.add(tag5.getTagName());
        gardenTagHashSet.add(tag6.getTagName());
        gardenTagHashSet.add(tag7.getTagName());

        gardenTagService.addGardenTag(tag1);
        gardenTagService.addGardenTag(tag2);
        gardenTagService.addGardenTag(tag3);
        gardenTagService.addGardenTag(tag4);
        gardenTagService.addGardenTag(tag5);
        gardenTagService.addGardenTag(tag6);
        gardenTagService.addGardenTag(tag7);

        try {
            List<GardenTag> searchResults = gardenTagService.getAllGardenTags();
            Assertions.assertEquals(7, searchResults.size());
            for (GardenTag tag : searchResults) {
                Assertions.assertTrue(gardenTagHashSet.contains(tag.getTagName()));
                gardenTagHashSet.remove(tag.getTagName());
            }

        } catch (NoSuchElementException exception) {
            fail(exception);
        }
    }

    @Test
    void simpleFetchGardenTag_OnSimilarName_NoneMatch() {
        GardenTag tag1 = new GardenTag("redHerring1");
        GardenTag tag2 = new GardenTag("redHerring2");
        GardenTag tag3 = new GardenTag("redHerring3");
        GardenTag tag4 = new GardenTag("redHerring4");
        GardenTag tag5 = new GardenTag("redHerring5");
        GardenTag tag6 = new GardenTag("redHerring6");
        GardenTag tag7 = new GardenTag("redHerring7");

        gardenTagService.addGardenTag(tag1);
        gardenTagService.addGardenTag(tag2);
        gardenTagService.addGardenTag(tag3);
        gardenTagService.addGardenTag(tag4);
        gardenTagService.addGardenTag(tag5);
        gardenTagService.addGardenTag(tag6);
        gardenTagService.addGardenTag(tag7);

        try {
            List<GardenTag> similarSearchResult = gardenTagService.getAllSimilar("test");
            Assertions.assertEquals(0, similarSearchResult.size());
        } catch (NoSuchElementException exception) {
            fail(exception);
        }
    }

    @Test
    void addGardenTagRelation_basicAddition_findById() {
        GardenTag testTag = new GardenTag("Test");
        gardenTagService.addGardenTag(testTag);
        GardenTagRelation testGardenTagRelation = new GardenTagRelation(testGarden, testTag);
        Long testGardenTagRelationId = gardenTagService.addGardenTagRelation(testGardenTagRelation).getId();
        Assertions.assertEquals(testGardenTagRelation.toString(),
                gardenTagService.getGardenTagRelationById(testGardenTagRelationId).get().toString());

    }

    @Test
    void addGardenTagRelation_basicAdditionThenDelete_findById() {
        GardenTag testTag = new GardenTag("Test");
        gardenTagService.addGardenTag(testTag);
        GardenTagRelation testGardenTagRelation = new GardenTagRelation(testGarden, testTag);
        Long testGardenTagRelationId = gardenTagService.addGardenTagRelation(testGardenTagRelation).getId();
        gardenTagService.removeGardenTagRelation(testGardenTagRelation);
        Assertions.assertTrue(gardenTagService.getGardenTagRelationById(testGardenTagRelationId).isEmpty());
    }

    @Test
    void addGardenTagRelation_basicAddition_findByGarden() {
        GardenTag testTag = new GardenTag("Test");
        gardenTagService.addGardenTag(testTag);
        GardenTagRelation testGardenTagRelation = new GardenTagRelation(testGarden, testTag);
        gardenTagService.addGardenTagRelation(testGardenTagRelation);
        Assertions.assertEquals(testGardenTagRelation.toString(),
                gardenTagService.getGardenTagRelationByGarden(testGarden).get(0).toString());

    }

    @Test
    void addGardenTagRelation_basicAddition_findByTag() {
        GardenTag testTag = new GardenTag("Test");
        gardenTagService.addGardenTag(testTag);
        GardenTagRelation testGardenTagRelation = new GardenTagRelation(testGarden, testTag);
        gardenTagService.addGardenTagRelation(testGardenTagRelation);
        Assertions.assertEquals(testGardenTagRelation.toString(),
                gardenTagService.getGardenTagRelationByTag(testTag).get(0).toString());

    }

    @Test
    void addGardenTagRelation_addOneTagToManyGardens_findByTag() {

        GardenTag testTag = gardenTagService.addGardenTag(new GardenTag("Test"));
        GardenTag redHerring = gardenTagService.addGardenTag(new GardenTag("redHerring"));
        gardenTagService.addGardenTagRelation(new GardenTagRelation(testGarden, testTag));
        gardenTagService.addGardenTagRelation(new GardenTagRelation(testGarden2, testTag));
        gardenTagService.addGardenTagRelation(new GardenTagRelation(testGarden3, testTag));
        gardenTagService.addGardenTagRelation(new GardenTagRelation(testGarden3, redHerring));

        try {
            List<GardenTagRelation> similarSearchResult = gardenTagService.getGardenTagRelationByTag(testTag);
            Assertions.assertEquals(3, similarSearchResult.size());
        } catch (NoSuchElementException exception) {
            fail(exception);
        }
    }

    @Test
    void addGardenTagRelation_addManyTagToOneGarden_findByGarden() {

        GardenTag testTag1 = gardenTagService.addGardenTag(new GardenTag("test1"));
        GardenTag testTag2 = gardenTagService.addGardenTag(new GardenTag("test2"));
        GardenTag testTag3 = gardenTagService.addGardenTag(new GardenTag("test3"));
        GardenTag testTag4 = gardenTagService.addGardenTag(new GardenTag("test4"));
        gardenTagService.addGardenTagRelation(new GardenTagRelation(testGarden, testTag1));
        gardenTagService.addGardenTagRelation(new GardenTagRelation(testGarden, testTag2));
        gardenTagService.addGardenTagRelation(new GardenTagRelation(testGarden, testTag3));
        gardenTagService.addGardenTagRelation(new GardenTagRelation(testGarden2, testTag4));

        try {
            List<GardenTagRelation> similarSearchResult = gardenTagService.getGardenTagRelationByGarden(testGarden);
            Assertions.assertEquals(3, similarSearchResult.size());
        } catch (NoSuchElementException exception) {
            fail(exception);
        }
    }

    @Test
    void updateGardenTagStatus_changeToAPPROPRIATE_gardenStatusUpdated() {
        GardenTag testTag1 = gardenTagService.addGardenTag(new GardenTag("test1"));
        TagStatus originalTagStatus = testTag1.getTagStatus();
        gardenTagService.updateGardenTagStatus("test1", TagStatus.APPROPRIATE);
        GardenTag updatedTag1 = gardenTagRepository.findById(testTag1.getId()).get();
        Assertions.assertNotEquals(originalTagStatus, updatedTag1.getTagStatus());
        Assertions.assertEquals(TagStatus.APPROPRIATE, updatedTag1.getTagStatus());

    }

    @Test
    void updateGardenTagStatus_changeToAPPROPRIATE_allMatchingGardenTagsUpdated() {
        GardenTag testTag1 = gardenTagService.addGardenTag(new GardenTag("test1"));
        GardenTag testTagCapitalized = gardenTagService.addGardenTag(new GardenTag("TEsT1"));
        gardenTagService.updateGardenTagStatus("test1", TagStatus.APPROPRIATE);
        GardenTag updatedTag1 = gardenTagRepository.findById(testTag1.getId()).get();
        GardenTag updatedTagCapitalized = gardenTagRepository.findById(testTagCapitalized.getId()).get();
        Assertions.assertEquals(TagStatus.APPROPRIATE, updatedTag1.getTagStatus());
        Assertions.assertEquals(TagStatus.APPROPRIATE, updatedTagCapitalized.getTagStatus());
    }

    @Test
    void updateGardenTagStatus_changeToAPPROPRIATE_onlyMatchingGardenTagsUpdated() {
        GardenTag testTag1 = gardenTagService.addGardenTag(new GardenTag("test1"));
        GardenTag testTag2 = gardenTagService.addGardenTag(new GardenTag("test2"));
        gardenTagService.updateGardenTagStatus("test1", TagStatus.APPROPRIATE);
        GardenTag updatedTag1 = gardenTagRepository.findById(testTag1.getId()).get();
        GardenTag updatedTag2 = gardenTagRepository.findById(testTag2.getId()).get();
        Assertions.assertEquals(TagStatus.APPROPRIATE, updatedTag1.getTagStatus());
        Assertions.assertEquals(TagStatus.PENDING, updatedTag2.getTagStatus());
    }

}
