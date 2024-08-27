package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GridItemLocation;
import nz.ac.canterbury.seng302.gardenersgrove.model.GardenDetailModel;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GridItemLocationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import nz.ac.canterbury.seng302.gardenersgrove.util.GridItemType;
import nz.ac.canterbury.seng302.gardenersgrove.util.ItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class Garden2DController {

    Logger logger = LoggerFactory.getLogger(Garden2DController.class);

    private final GardenService gardenService;

    private final SecurityService securityService;

    private final GridItemLocationService gridItemLocationService;

    @Autowired
    public Garden2DController(GardenService gardenService, SecurityService securityService, GridItemLocationService gridItemLocationService) {
        this.gardenService = gardenService;
        this.securityService = securityService;
        this.gridItemLocationService = gridItemLocationService;

    }

    @GetMapping("/2D-garden/{gardenId}")
    public String getGarden2DPage(@PathVariable Long gardenId,
                                  HttpServletResponse response,
                                  Model model) {
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
        model.addAttribute("plants", garden.getPlants());
        return "garden2DPage";
    }

    /**
     * Helper function to update the 2D co-ordinates of one item in a garden
     *
     * @param gridItemType
     * @param itemId
     * @param xCoord
     * @param yCoord
     * @param garden
     */
    private void updateGardenGrid(GridItemType gridItemType, Long itemId, Integer xCoord, Integer yCoord, Garden garden) {
        Optional<GridItemLocation> matchingGridItem = gridItemLocationService.getMatchingGridItem(gridItemType, itemId, garden);
        if (matchingGridItem == null) {
            GridItemLocation newGridItemLocation = new GridItemLocation(itemId, gridItemType, garden, xCoord, yCoord);
            gridItemLocationService.addGridItemLocation(newGridItemLocation);
        } else {
            GridItemLocation updatedGridItem = matchingGridItem.get();
            updatedGridItem.setXCoordinates(xCoord);
            updatedGridItem.setYCoordinates(yCoord);
            gridItemLocationService.updateGridItemLocation(updatedGridItem);
        }
    }

    //post method <-- save method
    //save2DGarden
    // parameters are objects (plants) <-- their ids
    // object type
    // and their location
    // list of tuples with above three items
    // garden Id

    //loop through each item/id/thing and update GritItemLocation

    //redirect back to "/2D-garden/{gardenId}"
    @PostMapping("/2D-garden/{gardenId}/save")
    public String save2DGarden(@PathVariable Long gardenId, @RequestParam(value = "idList", required = false) String idList, @RequestParam(value = "xCoordList", required = false) String xCoordList, @RequestParam(value = "yCoordList", required = false) String yCoordList, HttpServletResponse response, Model model) {
        logger.info("POST /2D-garden/{}/save", gardenId);
        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);


        if (optionalGarden.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }

        Garden garden = optionalGarden.get();
        model.addAttribute("gardenId", garden.getGardenId());

        if (!securityService.isOwner(garden.getOwner().getId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            model.addAttribute("message",
                    "This isn't your patch of soil. No peeking at the neighbor's garden without an invite!");
            return "403";
        }

        logger.info((String) idList);

        if (idList == null || xCoordList == null || yCoordList == null) {
            return "redirect:/2D-garden/{gardenId}";
        }

        //all items on grid are plants at the moment
        //ToDo: garden2DLayout.forEach(item -> updateGardenGrid(item[0], item[1], item[2], item[3],garden));

        return "redirect:/2D-garden/{gardenId}";

    }


}
