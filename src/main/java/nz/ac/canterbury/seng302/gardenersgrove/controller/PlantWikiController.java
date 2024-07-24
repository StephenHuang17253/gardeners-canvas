package nz.ac.canterbury.seng302.gardenersgrove.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


/**
 * Controller for plant info endpoints
 */
@Controller
public class PlantWikiController {
    Logger logger = LoggerFactory.getLogger(PlantWikiController.class);
    private final SecurityService securityService;

    @Value("${perenual.plant.api.key}")
    private String perenualPlantAPIKey;


    @Autowired
    public PlantWikiController(SecurityService securityService) {
        this.securityService = securityService;
    }


    /**
     * Gets the response body of the Perenual API plant details call.
     * @param plantId ID of plant species
     * @return plantInfo - a JsonNode containing the plant information such as common name, scientific name, image, etc.
     * @throws JsonProcessingException if the JSON string has an invalid format
     */
    public JsonNode getPlantDetailsJson(String plantId) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://perenual.com/api/species/details/" + plantId + "?key=" + perenualPlantAPIKey
                ))
                .header("accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        logger.info("Call to Perenual Plant API:" + request);
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        String plantDataString = "{\"id\":1,\"common_name\":\"European Silver Fir\",\"scientific_name\":[\"Abies alba\"],\"other_name\":[\"Common Silver Fir\"],\"family\":null,\"origin\":[\"Austria\",\"Germany\",\"Switzerland\",\"France\",\"Italy\",\"Slovenia\",\"Croatia\",\"Bosnia and Herzegovina\",\"Serbia\",\"Montenegro\",\"Albania\",\"Bulgaria\",\"Romania\",\"Ukraine\",\"Poland\",\"Czech Republic\",\"Slovakia\",\"Hungary\"],\"type\":\"tree\",\"dimension\":\"Height:  60 feet\",\"dimensions\":{\"type\":\"Height\",\"min_value\":60,\"max_value\":60,\"unit\":\"feet\"},\"cycle\":\"Perennial\",\"attracts\":[],\"propagation\":[\"Cutting\",\"Grafting Propagation\",\"Layering Propagation\",\"Seed Propagation\",\"Air Layering Propagation\",\"Tissue Culture\"],\"hardiness\":{\"min\":\"7\",\"max\":\"7\"},\"hardiness_location\":{\"full_url\":\"https:\\/\\/perenual.com\\/api\\/hardiness-map?species_id=1&size=og&key=sk-zhTS66a07c67e32476317\",\"full_iframe\":\"<iframe frameborder=0 scrolling=yes seamless=seamless width=1000 height=550 style='margin:auto;' src='https:\\/\\/perenual.com\\/api\\/hardiness-map?species_id=1&size=og&key=sk-zhTS66a07c67e32476317'><\\/iframe>\"},\"watering\":\"Frequent\",\"depth_water_requirement\":[],\"volume_water_requirement\":[],\"watering_period\":null,\"watering_general_benchmark\":{\"value\":\"7-10\",\"unit\":\"days\"},\"plant_anatomy\":[],\"sunlight\":[\"full sun\"],\"pruning_month\":[\"February\",\"March\",\"April\"],\"pruning_count\":[],\"seeds\":0,\"maintenance\":null,\"care-guides\":\"http:\\/\\/perenual.com\\/api\\/species-care-guide-list?species_id=1&key=sk-zhTS66a07c67e32476317\",\"soil\":[],\"growth_rate\":\"High\",\"drought_tolerant\":false,\"salt_tolerant\":false,\"thorny\":false,\"invasive\":false,\"tropical\":false,\"indoor\":false,\"care_level\":\"Medium\",\"pest_susceptibility\":[],\"pest_susceptibility_api\":\"Coming Soon\",\"flowers\":false,\"flowering_season\":null,\"flower_color\":\"\",\"cones\":true,\"fruits\":false,\"edible_fruit\":false,\"edible_fruit_taste_profile\":\"Coming Soon\",\"fruit_nutritional_value\":\"Coming Soon\",\"fruit_color\":[],\"harvest_season\":null,\"leaf\":true,\"leaf_color\":[\"green\"],\"edible_leaf\":false,\"cuisine\":false,\"medicinal\":true,\"poisonous_to_humans\":0,\"poisonous_to_pets\":0,\"description\":\"European Silver Fir (Abies alba) is an amazing coniferous species native to mountainous regions of central Europe and the Balkans. It is an evergreen tree with a narrow, pyramidal shape and long, soft needles. Its bark is scaly grey-brown and its branches are highly ornamental due to its conical-shaped silver-tinged needles. It is pruned for use as an ornamental evergreen hedging and screening plant, and is also popular for use as a Christmas tree. Young trees grow quickly and have strong, flexible branches which makes them perfect for use as windbreaks. The European Silver Fir is an impressive species, making it ideal for gardens and public spaces.\",\"default_image\":{\"license\":45,\"license_name\":\"Attribution-ShareAlike 3.0 Unported (CC BY-SA 3.0)\",\"license_url\":\"https:\\/\\/creativecommons.org\\/licenses\\/by-sa\\/3.0\\/deed.en\",\"original_url\":\"https:\\/\\/perenual.com\\/storage\\/species_image\\/1_abies_alba\\/og\\/1536px-Abies_alba_SkalitC3A9.jpg\",\"regular_url\":\"https:\\/\\/perenual.com\\/storage\\/species_image\\/1_abies_alba\\/regular\\/1536px-Abies_alba_SkalitC3A9.jpg\",\"medium_url\":\"https:\\/\\/perenual.com\\/storage\\/species_image\\/1_abies_alba\\/medium\\/1536px-Abies_alba_SkalitC3A9.jpg\",\"small_url\":\"https:\\/\\/perenual.com\\/storage\\/species_image\\/1_abies_alba\\/small\\/1536px-Abies_alba_SkalitC3A9.jpg\",\"thumbnail\":\"https:\\/\\/perenual.com\\/storage\\/species_image\\/1_abies_alba\\/thumbnail\\/1536px-Abies_alba_SkalitC3A9.jpg\"},\"other_images\":\"Upgrade Plan To Supreme For Access https:\\/\\/perenual.com\\/subscription-api-pricing. Im sorry\"}";

        ObjectMapper mapper = new ObjectMapper();

        JsonNode plantInfo = mapper.readTree(response.body());

        return plantInfo;
    }

    @GetMapping("/plant-wiki")
    public String viewPlantWiki(@RequestParam(name = "search", required = false) String search, Model model) throws IOException, InterruptedException {
        logger.info("GET /plant-wiki with search: " + search);
        model.addAttribute("loggedIn", securityService.isLoggedIn());

        if (search != null && !search.isEmpty()) {
            model.addAttribute("searchTerm", search);
            JsonNode plantData = getPlantDetailsJson(search);
            String plantName = plantData.get("common_name").asText();
            String plantImageUrl = plantData.get("default_image").get("original_url").asText();
            model.addAttribute("plantName", plantName);
            model.addAttribute("plantImage", plantImageUrl);
            logger.info(plantName);
            logger.info(plantImageUrl);
        }





        return "plantWikiPage";
    }

}
