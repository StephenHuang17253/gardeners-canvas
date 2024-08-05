package nz.ac.canterbury.seng302.gardenersgrove.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.UnavailableException;
import nz.ac.canterbury.seng302.gardenersgrove.entity.PlantInfo;
import nz.ac.canterbury.seng302.gardenersgrove.model.PlantInfoModel;
import nz.ac.canterbury.seng302.gardenersgrove.model.PlantSearchModel;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantInfoService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


/**
 * Controller for plant info endpoints
 */
@Controller
public class PlantWikiController {
    Logger logger = LoggerFactory.getLogger(PlantWikiController.class);
    private final PlantInfoService plantInfoService;


    @Autowired
    public PlantWikiController(PlantInfoService plantInfoService) {
        this.plantInfoService = plantInfoService;
    }


    /**
     * Helper to handle adding api call results to model
     * and catching any potential exceptions that may occur
     * @param search term entered by user
     * @param model hashmap of endpoints attributes
     */
    private void addAPIResultsToModel(String search, Long plantId, Model model){
        try {
            if(Objects.nonNull(search)){
                JsonNode plantList = plantInfoService.getPlantListJson(search, false);

                List<PlantSearchModel> plants = StreamSupport.stream(plantList.get("data").spliterator(), false)
                        .map(PlantSearchModel::new)
                        .collect(Collectors.toList());

                model.addAttribute("plants", plants);
            } else {
                JsonNode plantDetails = plantInfoService.getPlantDetailsJson(String.valueOf(plantId), false);
                PlantInfoModel plantInfo = new PlantInfoModel(plantDetails);
                model.addAttribute("plant", plantInfo);
            }

        } catch (UnavailableException e) {
            logger.error("UnavailableException occurred: {}", e.getMessage());
            model.addAttribute("searchError", "Plant Wiki searching is a limited resource, which doesn't grow on trees. " +
                    "Please try again");

        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                logger.error("Rate limit exceeded: {}", e.getMessage());
                model.addAttribute("error", "Sorry we are unable to perform that action right now. " +
                        "Please try again tomorrow. In the meantime you may wish to check out our default plants");
            } else {
                logger.error("HTTP error occurred: {}", e.getMessage());
                model.addAttribute("error", "An error occurred while retrieving plant data. Please try again later.");
            }
        } catch (IOException e) {
            logger.error("IOException occurred while fetching plant data: {}", e.getMessage());
            model.addAttribute("searchError", "There was an error retrieving plant data. Please try again later.");

        } catch (InterruptedException e) {
            logger.error("InterruptedException occurred: {}", e.getMessage());
            Thread.currentThread().interrupt();
            model.addAttribute("searchError", "The request was interrupted. Please try again later.");

        } catch (Exception e) {
            logger.error("Unexpected error occurred: {}", e.getMessage());
            model.addAttribute("searchError", "An unexpected error occurred. Please try again later.");
        }
    }

    /**
     * Helper to add the default plant info objects to model
     * @param model hashmap of endpoint attributes
     */
    private void addDefaultPlantsToModel(Model model){
        List<PlantSearchModel> plants = plantInfoService.getAllDefaultPlants().stream()
                .map(PlantSearchModel::new)
                .collect(Collectors.toList());
        model.addAttribute("plants", plants);
    }

    /**
     * This method creates the get mapping for the plant wiki page where users can search for plant information.
     * @param search the query string
     * @param model the model
     * @return template for the plant wiki page
     */
    @GetMapping("/plant-wiki")
    public String viewPlantWiki(@RequestParam(name = "search", required = false)
                                String search,
                                Model model) {
        logger.info("GET /plant-wiki with search term: {}", search);

        if (search != null && !search.isEmpty()) {
            model.addAttribute("searchTerm", search);
            addAPIResultsToModel(search, null, model);
            return "plantWikiPage";
        }

        addDefaultPlantsToModel(model);

        return "plantWikiPage";
    }

    /**
     * This method creates the get mapping for the plant details page which displays information for a certain plant.
     * @param plantId               id of the plant
     * @param model                 the model
     * @return                      template for the plant details pge
     */
    @GetMapping("/plant-wiki/{plantId}/details")
    public String viewPlantDetails(@PathVariable Long plantId,
                                   Model model) {
        logger.info("GET /plant-wiki/{}/details", plantId);

        Optional<PlantInfo> plant = plantInfoService.getPlantById(plantId);

        if(plant.isPresent()){
            PlantInfoModel plantInfo = new PlantInfoModel(plant.get());

            model.addAttribute("plant", plantInfo);
            return "plantWikiDetailPage";
        }

        addAPIResultsToModel(null, plantId, model);

        return "plantWikiDetailPage";
    }

}
