package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

/**
 * Controller for viewing all the created Gardens
 */
@Controller
public class MyGardensController {

    Logger logger = LoggerFactory.getLogger(MyGardensController.class);

    private final GardenService gardenService;

    @Autowired
    public MyGardensController(GardenService gardenService) {
        this.gardenService = gardenService;
    }

    /**
     * Maps the myGardensPage html file to /my-gardens url
     * @return thymeleaf createNewGardenForm
     */
    @GetMapping("/my-gardens")
    public String myGardens(Model model) {
        logger.info("GET /my-gardens");
        model.addAttribute("myGardens", gardenService.getGardens());
        model.addAttribute("gardenCount", gardenService.getGardens().size());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);

        return "myGardensPage";
    }

    /**
     * Gets all the users created gardens
     * and maps them all and there attributes to the gardenDetailsPage
     * but with the custom url of /my-gardens/{gardenId}={gardenName}
     * @return thymeleaf createNewGardenForm
     */
    @GetMapping("/my-gardens/{gardenId}={gardenName}")
    public String showGardenDetails(@PathVariable("gardenId") String gardenIdString,
                                    @PathVariable String gardenName,
                                    Model model) {
        logger.info("GET /my-gardens/{}-{}", gardenIdString, gardenName);

        long gardenId = Long.parseLong(gardenIdString);
        Optional<Garden> optionalGarden = gardenService.findById(gardenId);
        model.addAttribute("myGardens", gardenService.getGardens());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);

        if (optionalGarden.isPresent()) {
            Garden garden = optionalGarden.get();
            model.addAttribute("gardenName", garden.getGardenName());
            model.addAttribute("gardenLocation", garden.getGardenLocation());
            model.addAttribute("gardenSize", garden.getGardenSize());
            model.addAttribute("gardenId",gardenIdString);
            model.addAttribute("plants",garden.getPlants());
            model.addAttribute("totalPlants",garden.getPlants().size());
            return "gardenDetailsPage";
        } else {
            return "404";
        }
    }
}
