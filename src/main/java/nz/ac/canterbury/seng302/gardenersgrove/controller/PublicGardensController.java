package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for viewing all public gardens
 */
@Controller
public class PublicGardensController {

    Logger logger = LoggerFactory.getLogger(PublicGardensController.class);

    private final GardenService gardenService;

    private final SecurityService securityService;


    @Autowired
    public PublicGardensController(GardenService gardenService, SecurityService securityService) {
        this.gardenService = gardenService;
        this.securityService = securityService;

    }

    /**
     * returns a page with 10 most recent public gardens
     *
     * @return thymeleaf BrowsePublicGardens html element
     */
    @GetMapping("/public-gardens")
    public String publicGardens(Model model) {
        logger.info("GET /public-gardens");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);

        List<Garden> allGardens = gardenService.getGardens();

        List<Garden> tenSortedPublicGardens = allGardens.stream()
                .sorted(Comparator.comparing(Garden::getCreationDate).reversed())
                .limit(10L) // Temporary limit until pagination is implemented.
                .collect(Collectors.toList());

        model.addAttribute("publicGardens", tenSortedPublicGardens);

        return "BrowsePublicGardens";
    }


}

