package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
public class ValidateCucumber {
    WebApplicationContext webApplicationContext;

    @Autowired
    ValidateCucumber(WebApplicationContext webApplicationContext)
    {
        this.webApplicationContext = webApplicationContext;
    }
    @When("Spring Application context is loaded")
    public void an_empty_cucumber_test_is_run() {
        Assertions.assertNotNull(this.webApplicationContext);
    }
}
