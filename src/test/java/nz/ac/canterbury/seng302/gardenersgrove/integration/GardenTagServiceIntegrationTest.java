package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTag;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenTagRelationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenTagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenTagService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

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
    private GardenTagService gardenTagService;

    @BeforeEach
    public void prepare_test()
    {
        gardenTagRepository.deleteAll();
        gardenTagService = new GardenTagService(gardenTagRepository, gardenTagRelationRepository);
    }

    @Test
    void simpleFetchGardenTag_OnId()
    {
        GardenTag tag1 = new GardenTag("Test");
        Long saveGardenId = gardenTagService.addGardenTag(tag1).getId();

        try
        {
            Assertions.assertEquals("Test", gardenTagService.getById(saveGardenId).get().getTagName());
        }
        catch (NoSuchElementException exception)
        {
            fail(exception);
        }
    }

    @Test
    void doubleAddNewTag_throwsArgumentException()
    {
        GardenTag tag1 = new GardenTag("Test");
        GardenTag tag2 = new GardenTag("Test");
        gardenTagService.addGardenTag(tag1);
        Assertions.assertThrows(IllegalArgumentException.class,() -> {gardenTagService.addGardenTag(tag2);});

    }

    @Test
    void simpleFetchGardenTag_OnId_withOtherData()
    {
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

        try
        {
            Assertions.assertEquals("Test", gardenTagService.getById(saveGardenId).get().getTagName());
        }
        catch (NoSuchElementException exception)
        {
            fail(exception);
        }
    }

    @Test
    void simpleFetchGardenTag_OnName()
    {
        GardenTag tag1 = new GardenTag("Test2");
        gardenTagService.addGardenTag(tag1).getId();

        try
        {
            Assertions.assertEquals("Test2", gardenTagService.getByName("Test2").get().getTagName());
            System.out.println(gardenTagService.getAllSimilar("te"));
        }
        catch (NoSuchElementException exception)
        {
            fail(exception);
        }
    }

    @Test
    void simpleFetchGardenTag_OnName_withOtherData()
    {
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

        try
        {
            Assertions.assertEquals("Test", gardenTagService.getByName("Test").get().getTagName());
        }
        catch (NoSuchElementException exception)
        {
            fail(exception);
        }
    }


    @Test
    void simpleFetchGardenTag_OnSimilarName()
    {
        GardenTag tag1 = new GardenTag("Test2");
        gardenTagService.addGardenTag(tag1).getId();

        try
        {
            Assertions.assertEquals("Test2", gardenTagService.getAllSimilar("te").get(0).getTagName());
        }
        catch (NoSuchElementException exception)
        {
            fail(exception);
        }
    }

    @Test
    void simpleFetchGardenTag_OnSimilarName_withMultipleNames()
    {
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

        try
        {
            List<GardenTag> similarSearchResult = gardenTagService.getAllSimilar("test");
            Assertions.assertEquals(6, similarSearchResult.size());
            for (GardenTag tag: similarSearchResult)
            {
                Assertions.assertTrue(gardenTagHashSet.contains(tag.getTagName()));
                gardenTagHashSet.remove(tag.getTagName());
            }

        }
        catch (NoSuchElementException exception)
        {
            fail(exception);
        }
    }

    @Test
    void simpleFetchGardenTag_getAll_withMultipleNames()
    {
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

        try
        {
            List<GardenTag> SearchResults = gardenTagService.getAllGardenTags();
            Assertions.assertEquals(7, SearchResults.size());
            for (GardenTag tag: SearchResults)
            {
                Assertions.assertTrue(gardenTagHashSet.contains(tag.getTagName()));
                gardenTagHashSet.remove(tag.getTagName());
            }

        }
        catch (NoSuchElementException exception)
        {
            fail(exception);
        }
    }

    @Test
    void simpleFetchGardenTag_OnSimilarName_NoneMatch()
    {
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

        try
        {
            List<GardenTag> similarSearchResult = gardenTagService.getAllSimilar("test");
            Assertions.assertEquals(0, similarSearchResult.size());
        }
        catch (NoSuchElementException exception)
        {
            fail(exception);
        }
    }




}
