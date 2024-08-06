package nz.ac.canterbury.seng302.gardenersgrove.cucumber;

import io.cucumber.junit.platform.engine.Constants;
import io.cucumber.spring.CucumberContextConfiguration;
import nz.ac.canterbury.seng302.gardenersgrove.GardenersGroveApplication;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantInfoService;
import nz.ac.canterbury.seng302.gardenersgrove.service.WeatherService;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityService;
import org.junit.platform.suite.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

// taken from ATDD workshop
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameters({
        @ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "nz.ac.canterbury.seng302.gardenersgrove.cucumber"),
        @ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-report/cucumber.html"),
        @ConfigurationParameter(key = Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true")
})
@ContextConfiguration(classes = GardenersGroveApplication.class)
@CucumberContextConfiguration
@SpringBootTest
@ActiveProfiles("cucumber")
public class RunCucumberTest {
    @MockBean
    public FileService fileservice;
    @MockBean
    public WeatherService weatherService;
    @MockBean
    public ProfanityService profanityService;
    @MockBean
    public EmailService emailService;
    @MockBean
    public PlantInfoService plantInfoService;
}