package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GridItemLocation;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.model.DisplayableItem;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.model.GardenDetailModel;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;

import nz.ac.canterbury.seng302.gardenersgrove.util.GridItemType;
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
import java.util.Objects;
import java.util.Optional;

@Controller
public class Garden3DController {

    Logger logger = LoggerFactory.getLogger(Garden3DController.class);

    private final GardenService gardenService;

    private final GridItemLocationService gridItemLocationService;
    private final SecurityService securityService;
    private final FriendshipService friendshipService;

    private final PlantService plantService;

    @Autowired
    public Garden3DController(GardenService gardenService, SecurityService securityService,
            FriendshipService friendshipService, GridItemLocationService gridItemLocationService,
            PlantService plantService) {
        this.gardenService = gardenService;
        this.gridItemLocationService = gridItemLocationService;
        this.securityService = securityService;
        this.friendshipService = friendshipService;
        this.plantService = plantService;
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

        Garden garden = optionalGarden.get();
        User currentUser = securityService.getCurrentUser();
        User gardenOwner = garden.getOwner();

        if (!(garden.getIsPublic() || Objects.equals(gardenOwner.getId(), currentUser.getId())
                || friendshipService.checkFriendshipStatus(gardenOwner, currentUser) == FriendshipStatus.ACCEPTED)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            model.addAttribute("message",
                    "This isn't your patch of soil. No peeking at the neighbor's garden without an invite!");
            return "403";
        }

        model.addAttribute("garden", new GardenDetailModel(garden));
        model.addAttribute("isOwner", securityService.isOwner(garden.getOwner().getId()));
        return "garden3DPage";
    }

    /**
     * returns a list of item locations as displayable items with coordinates and
     * names
     *
     * @param gardenId of the garden to get layout for
     * @param response object to be returned to caller
     * @return a list of displayable items
     */
    @ResponseBody
    @GetMapping("/3D-garden-layout/{gardenId}")
    public List<DisplayableItem> get3DGardenLayout(@PathVariable Long gardenId,
            HttpServletResponse response) {
        logger.info("GET /3D-garden-layout/{}", gardenId);

        List<DisplayableItem> displayableItems = new ArrayList<>();

        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);

        if (optionalGarden.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return displayableItems;
        }

        Garden garden = optionalGarden.get();
        User currentUser = securityService.getCurrentUser();
        User gardenOwner = garden.getOwner();

        if (!(garden.getIsPublic() || Objects.equals(gardenOwner.getId(), currentUser.getId())
                || friendshipService.checkFriendshipStatus(gardenOwner, currentUser) == FriendshipStatus.ACCEPTED)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return displayableItems;
        }

        List<GridItemLocation> plantLocations = gridItemLocationService.getGridItemLocationByGarden(garden);

        for (GridItemLocation plantLocation : plantLocations) {
            if (plantLocation.getItemType() == GridItemType.PLANT) {
                Optional<Plant> optionalPlant = plantService.getById(plantLocation.getObjectId());
                if (optionalPlant.isPresent()) {
                    Plant currentPlant = optionalPlant.get();
                    displayableItems.add(new DisplayableItem(plantLocation.getXCoordinate(),
                            plantLocation.getYCoordinate(),
                            currentPlant.getPlantName(),
                            currentPlant.getPlantCategory().toString(),
                            plantLocation.getObjectId(),
                            plantLocation.getItemType(),
                            currentPlant.getPlantCategory().getCategoryImage()));
                }
                else {
                    logger.warn("Plant/Decoration grid item could not be added to grid, missing item, id {}",plantLocation.getId());
                }
            }
            else {
                logger.warn("Not yet implemented adding decoration to grid {}",plantLocation.getId());
            }
        }

        return displayableItems;
    }

}