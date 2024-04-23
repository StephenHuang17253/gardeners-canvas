package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final UserService userService;

    @Autowired
    public MyGardensController(GardenService gardenService, UserService userService) {
        this.gardenService = gardenService;
        this.userService = userService;
    }

    /**
     * Maps the myGardensPage html file to /my-gardens url
     * @return thymeleaf createNewGardenForm
     */
    @GetMapping("/my-gardens")
    public String myGardens(Model model) {
        logger.info("GET /my-gardens");

        return "myGardensPage";
    }

    /**
     * Gets all the users created gardens
     * and maps them all and there attributes to the gardenDetailsPage
     * but with the custom url of /my-gardens/{gardenId}={gardenName}
     * @return thymeleaf createNewGardenForm
     */
    @PreAuthorize("@securityService.isOwner(#gardenId)")
    @GetMapping("/my-gardens/{gardenId}={gardenName}")
    public String showGardenDetails(@PathVariable Long gardenId,
                                    @PathVariable String gardenName,
                                    Model model) {
        logger.info("GET /my-gardens/{}-{}", gardenId, gardenName);

        Optional<Garden> optionalGarden = gardenService.findById(gardenId);

        if (optionalGarden.isPresent()) {
            Garden garden = optionalGarden.get();
            model.addAttribute("gardenName", garden.getGardenName());
            model.addAttribute("gardenLocation", garden.getGardenLocation());
            model.addAttribute("gardenSize", garden.getGardenSize());
            model.addAttribute("gardenId",gardenId);
            model.addAttribute("plants", garden.getPlants());
            model.addAttribute("totalPlants", garden.getPlants().size());
            return "gardenDetailsPage";
        } else {
            return "404";
        }
    }

}
