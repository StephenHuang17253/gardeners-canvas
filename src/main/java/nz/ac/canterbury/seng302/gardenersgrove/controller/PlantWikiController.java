package nz.ac.canterbury.seng302.gardenersgrove.controller;

import com.fasterxml.jackson.databind.JsonNode;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantInfoService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


/**
 * Controller for plant info endpoints
 */
@Controller
public class PlantWikiController {
    Logger logger = LoggerFactory.getLogger(PlantWikiController.class);
    private final SecurityService securityService;
    private final PlantInfoService plantInfoService;


    @Autowired
    public PlantWikiController(SecurityService securityService, PlantInfoService plantInfoService) {
        this.securityService = securityService;
        this.plantInfoService = plantInfoService;
    }



    private void displayPlantDetails(Model model, JsonNode plantData) {
            String plantName = plantData.get("common_name").asText();
            String plantImageUrl = plantData.get("default_image").get("original_url").asText();
            model.addAttribute("plantName", plantName);
            model.addAttribute("plantImage", plantImageUrl);
            logger.info(plantName);
            logger.info(plantImageUrl);
    }

    private void addSearchSuggestions(Model model, JsonNode plantList) {

        JsonNode dataArray = plantList.path("data");

        // Ensure dataArray is an array node
        if (dataArray.isArray()) {
            // Convert JsonNode to a stream and extract common names
            List<String> commonNames = StreamSupport.stream(dataArray.spliterator(), false)
                    .map(node -> node.path("common_name").asText())
                    .collect(Collectors.toList());

            // Add the common names to the model
            model.addAttribute("searchSuggestions", commonNames);
        }
    }

    @GetMapping("/plant-wiki")
    public String viewPlantWiki(@RequestParam(name = "search", required = false) String search, Model model) throws IOException, InterruptedException {
        logger.info("GET /plant-wiki with search: " + search);
        model.addAttribute("loggedIn", securityService.isLoggedIn());

        if (search != null && !search.isEmpty()) {
            model.addAttribute("searchTerm", search);
            JsonNode plantList = plantInfoService.getPlantListJson(search);

            model.addAttribute("plantList", plantList.get("data"));

        }

        return "plantWikiPage";
    }

}
