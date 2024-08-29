package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GridItemLocation;
import nz.ac.canterbury.seng302.gardenersgrove.model.DisplayableItem;
import nz.ac.canterbury.seng302.gardenersgrove.model.GardenDetailModel;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GridItemLocationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class Garden3DController {

    Logger logger = LoggerFactory.getLogger(Garden3DController.class);

    private final GardenService gardenService;

    private final GridItemLocationService gridItemLocationService;

    @Autowired
    public Garden3DController(GardenService gardenService, GridItemLocationService gridItemLocationService) {
        this.gardenService = gardenService;
        this.gridItemLocationService = gridItemLocationService;
    }

    @GetMapping("/3D-garden/{gardenId}")
    public String getGarden3DPage(@PathVariable Long gardenId,
            HttpServletResponse response,
            Model model) {
        logger.info("GET /3D-garden/{}", gardenId);
        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);

        if (optionalGarden.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }
        model.addAttribute("garden", new GardenDetailModel(optionalGarden.get()));
        return "garden3DPage";
    }

    /**
     * returns a list of item locations as displayable items with coordinates and names
     *
     * @param gardenId of the garden to get layout for
     * @param response object to be returned to caller
     * @param model
     * @return a list of displayable items
     */
    @ResponseBody
    @GetMapping("/3D-garden-layout/{gardenId}")
    public List<DisplayableItem> get3DGardenLayout(@PathVariable Long gardenId,
            HttpServletResponse response,
            Model model) {
        logger.info("GET /3D-garden-layout/{}", gardenId);

        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);

        if (optionalGarden.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ArrayList<>();
        }

        Garden garden = optionalGarden.get();

        List<GridItemLocation> plantLocations = gridItemLocationService.getGridItemLocationByGarden(garden);

        return plantLocations.stream().map(plantLocation -> new DisplayableItem(plantLocation.getXCoordinate(),plantLocation.getYCoordinate(),"testName","testModel")).toList();
    }

}
