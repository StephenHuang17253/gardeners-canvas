package nz.ac.canterbury.seng302.gardenersgrove.controller;

import com.fasterxml.jackson.databind.JsonNode;
import nz.ac.canterbury.seng302.gardenersgrove.model.PlantInfoModel;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantInfoService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;


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

    /**
     * This method creates the get mapping for the plant wiki page where users can search for plant information.
     * @param search the query string
     * @param model the model
     * @return template for the plant wiki page
     * @throws IOException          If an I/O error occurs while making the
     *                              requesting
     * @throws InterruptedException If an interruption occurs while waiting for
     *                              response
     */
    @GetMapping("/plant-wiki")
    public String viewPlantWiki(@RequestParam(name = "search", required = false)
                                String search,
                                Model model) throws IOException, InterruptedException {
        logger.info("GET /plant-wiki with search: {}", search);

        if (search != null && !search.isEmpty()) {
            model.addAttribute("searchTerm", search);
            JsonNode plantList = plantInfoService.getPlantListJson(search, false);

            model.addAttribute("plantList", plantList.get("data"));

        }

        return "plantWikiPage";
    }

    /**
     * This method creates the get mapping for the plant details page which displays information for a certain plant.
     * @param plantId               id of the plant
     * @param model                 the model
     * @return                      template for the plant details pge
     * @throws IOException          If an error occurs while making request
     * @throws InterruptedException If request is interrupted
     */
    @GetMapping("/plant-wiki/{plantId}/details")
    public String viewPlantDetails(@PathVariable Long plantId,
                                   Model model) throws IOException, InterruptedException {
        logger.info("GET /plant-wiki/{}/details", plantId);


        JsonNode plantDetails = plantInfoService.getPlantDetailsJson(String.valueOf(plantId), false);

        PlantInfoModel plant = new PlantInfoModel(plantDetails);

        model.addAttribute("plant", plant);


        return "plantWikiDetailPage";
    }

}
