package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.model.GardenDetailModel;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class Garden3DController {

    Logger logger = LoggerFactory.getLogger(Garden3DController.class);

    private final GardenService gardenService;

    @Autowired
    public Garden3DController(GardenService gardenService){
        this.gardenService = gardenService;

    }

    @GetMapping("/3D-garden/{gardenId}")
    public String getGarden3DPage(@PathVariable Long gardenId,
                                  HttpServletResponse response,
                                  Model model){
        logger.info("GET /3D-garden/{}", gardenId);
        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);

        if (optionalGarden.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }
        model.addAttribute("garden", new GardenDetailModel(optionalGarden.get()));
        return "garden3DPage";
    }

}
