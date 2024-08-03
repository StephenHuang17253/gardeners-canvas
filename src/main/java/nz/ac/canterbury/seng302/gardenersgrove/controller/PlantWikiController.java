package nz.ac.canterbury.seng302.gardenersgrove.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.UnavailableException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
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

    private static final int MAX_REQUESTS_PER_SECOND = 1;
    private static final Semaphore semaphore = new Semaphore(MAX_REQUESTS_PER_SECOND, true);
    private static long lastRequestTime = Instant.now().getEpochSecond();


    @Autowired
    public PlantWikiController(SecurityService securityService, PlantInfoService plantInfoService) {
        this.securityService = securityService;
        this.plantInfoService = plantInfoService;
    }

    /**
     * Helper function to give user a permit to make an api request
     * To ensure a rate limit of one call per second
     * @throws UnavailableException when the last permit granted was less than a second ago
     */
    public static synchronized void acquirePermit() throws UnavailableException {
        long currentTime = Instant.now().getEpochSecond();
        long timeElapsed = currentTime - lastRequestTime;

        if (timeElapsed >= 1) {
            semaphore.drainPermits();
            semaphore.release(MAX_REQUESTS_PER_SECOND);
            lastRequestTime = currentTime;
        }

        if (!semaphore.tryAcquire()) {
            throw new UnavailableException("429");
        }
    }

    /**
     * Helper to handle adding search results to model
     * and catching any potential exceptions that may ocur
     * @param search term entered by user
     * @param model hashmap of endpoints attributes
     */
    private void addSearchResultsToModel(String search, Model model){
        try {
            acquirePermit();
            JsonNode plantList = plantInfoService.getPlantListJson(search, true);

            List<PlantSearchModel> plants = StreamSupport.stream(plantList.get("data").spliterator(), false)
                    .map(PlantSearchModel::new)
                    .collect(Collectors.toList());

            model.addAttribute("plants", plants);

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
        logger.info("GET /plant-wiki with search term: {}", search);

        if (search != null && !search.isEmpty()) {
            model.addAttribute("searchTerm", search);
            addSearchResultsToModel(search, model);
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
