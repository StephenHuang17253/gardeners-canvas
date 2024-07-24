package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;


/**
 * Controller for plant info endpoints
 */
@Controller
public class PlantWikiController {
    Logger logger = LoggerFactory.getLogger(PlantWikiController.class);
    private final SecurityService securityService;
    @Autowired
    public PlantWikiController(SecurityService securityService) {
        this.securityService = securityService;
    }

    @GetMapping("/plant-wiki")
    public String viewPlantWiki(@RequestParam(name = "search", required = false) String search, Model model) {
        logger.info("GET /plant-wiki with search: " + search);
        model.addAttribute("loggedIn", securityService.isLoggedIn());

        if (search != null && !search.isEmpty()) {
            model.addAttribute("searchTerm", search);
        }

        return "plantWikiPage";
    }

}
