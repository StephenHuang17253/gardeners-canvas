package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTag;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenTagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenTagService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class GardenTagServiceIntegrationTest {

    @Autowired
    private GardenTagRepository gardenTagRepository;
    private GardenTagService gardenTagService;

    @BeforeEach
    public void prepare_test()
    {
        gardenTagRepository.deleteAll();
        gardenTagService = new GardenTagService(gardenTagRepository);
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




}
