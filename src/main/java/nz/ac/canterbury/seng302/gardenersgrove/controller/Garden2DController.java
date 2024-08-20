package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.model.GardenDetailModel;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import nz.ac.canterbury.seng302.gardenersgrove.util.ItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class Garden2DController {

    Logger logger = LoggerFactory.getLogger(Garden2DController.class);

    private final GardenService gardenService;

    private final SecurityService securityService;

    @Autowired
    public Garden2DController(GardenService gardenService, SecurityService securityService){
        this.gardenService = gardenService;
        this.securityService = securityService;

    }

    @GetMapping("/2D-garden/{gardenId}")
    public String getGarden3DPage(@PathVariable Long gardenId,
                                  HttpServletResponse response,
                                  Model model){
        logger.info("GET /2D-garden/{}", gardenId);
        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);

        if (optionalGarden.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }

        Garden garden = optionalGarden.get();

        if (!securityService.isOwner(garden.getOwner().getId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            model.addAttribute("message",
                    "This isn't your patch of soil. No peeking at the neighbor's garden without an invite!");
            return "403";
        }

        securityService.addUserInteraction(gardenId, ItemType.GARDEN, LocalDateTime.now());

        model.addAttribute("isOwner", true);
        model.addAttribute("garden", new GardenDetailModel(optionalGarden.get()));
        return "garden2DPage";
    }

}
