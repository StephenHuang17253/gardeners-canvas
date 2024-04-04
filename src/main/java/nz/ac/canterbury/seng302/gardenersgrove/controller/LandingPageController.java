package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;



/**
 * Redirects users to the landing page when they enter / (root) or / landing in their web search
 */
@Controller
public class LandingPageController {

    Logger logger = LoggerFactory.getLogger(LandingPageController.class);

    private final GardenService gardenService;

    public LandingPageController(GardenService gardenService) {
        this.gardenService = gardenService;
    }


    /**
     * sends users that get the /landing page to the landingPage.html
     * @return the html landing page
     */
    @GetMapping("/landing")
    public String getLanding(Model model)
    {
        logger.info("Get /landing");

        model.addAttribute("myGardens", gardenService.getGardens());

        return "landingPage";
    }


}
