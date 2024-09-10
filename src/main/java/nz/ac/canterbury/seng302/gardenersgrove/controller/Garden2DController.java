package nz.ac.canterbury.seng302.gardenersgrove.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GridItemLocation;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class Garden2DController {

    Logger logger = LoggerFactory.getLogger(Garden2DController.class);

    private final GardenService gardenService;

    private final SecurityService securityService;

    private final GridItemLocationService gridItemLocationService;

    private static final int COUNT_PER_PAGE = 6;

    private static final String ERROR_MESSAGE_ATTRIBUTE = "message";

    @Autowired
    public Garden2DController(GardenService gardenService, SecurityService securityService, GridItemLocationService gridItemLocationService) {
        this.gardenService = gardenService;
        this.securityService = securityService;
        this.gridItemLocationService = gridItemLocationService;

    }

    @GetMapping("/2D-garden/{gardenId}")
    public String getGarden2DPage(@PathVariable Long gardenId,
                                  @RequestParam(defaultValue = "1") int page,
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
            model.addAttribute(ERROR_MESSAGE_ATTRIBUTE,
                    "This isn't your patch of soil. No peeking at the neighbor's garden without an invite!");
            return "403";
        }

        securityService.addUserInteraction(gardenId, ItemType.GARDEN, LocalDateTime.now());
        handlePagniation(page, garden.getPlants().size(), garden.getPlants(), model);

        Map<Long, Plant> plantsById = garden.getPlants().stream()
                .collect(Collectors.toMap(Plant::getPlantId, Function.identity()));

        model.addAttribute("isOwner", true);
        model.addAttribute("garden", new GardenDetailModel(optionalGarden.get()));
        model.addAttribute("gridItemLocations", gridItemLocationService.getGridItemLocationByGarden(garden));
        model.addAttribute("plantsById", plantsById);
        return "garden2DPage";
    }

    private void handlePagniation(int page, int listLength, List<Plant> plants, Model model) {
        int totalPages = (int) Math.ceil((double) listLength / COUNT_PER_PAGE);
        int startIndex = (page - 1) * COUNT_PER_PAGE;
        int endIndex = Math.min(startIndex + COUNT_PER_PAGE, listLength);

        plants.sort(Comparator.comparing(Plant::getPlantName));

        model.addAttribute("currentPage", page);
        model.addAttribute("lastPage", totalPages);
        model.addAttribute("startIndex", startIndex + 1);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("plants", plants.subList(startIndex, endIndex));
        model.addAttribute("plantCount", plants.size());
    }

    /**
     * Helper function to update the 2D co-ordinates of one item in a garden
     *
     * @param gridItemType type of item (plant or decoration)
     * @param itemId       id of item
     * @param xCoord       x co-ordinate of item on 2D grid
     * @param yCoord       y co-ordinate of item on 2D grid
     * @param garden       garden that contains all the items
     */
    private void updateGardenGrid(GridItemType gridItemType, Long itemId, Integer xCoord, Integer yCoord, Garden garden) {
        Optional<GridItemLocation> matchingGridItem = gridItemLocationService.getMatchingGridItem(gridItemType, itemId, garden);
        if (matchingGridItem.isEmpty()) {
            GridItemLocation newGridItemLocation = new GridItemLocation(itemId, gridItemType, garden, xCoord, yCoord);
            gridItemLocationService.addGridItemLocation(newGridItemLocation);
        } else {
            GridItemLocation updatedGridItem = matchingGridItem.get();
            updatedGridItem.setXCoordinates(xCoord);
            updatedGridItem.setYCoordinates(yCoord);
            gridItemLocationService.updateGridItemLocation(updatedGridItem);
        }
    }

    /**
     * Endpoint to save the locations of elements on a 2D garden grid.
     * NOTE: CURRENTLY ASSUMES ALL ELEMENTS ARE PLANTS
     *
     * @param gardenId   id of the garden whose grid has to be saved
     * @param idList     ids of all elements on the grid
     * @param xCoordList x coordinates of all elements on the grid
     * @param yCoordList y coordinates of all elements on the grid
     * @param response   http response to use to return error
     * @param model      model to use to return error
     * @return redirect back to 2D garden grid
     */
    @PostMapping("/2D-garden/{gardenId}/save")
    public String save2DGarden(@PathVariable Long gardenId,
                               @RequestParam(value = "idList", required = false) String idList,
                               @RequestParam(value = "xCoordList", required = false) String xCoordList,
                               @RequestParam(value = "yCoordList", required = false) String yCoordList,
                               HttpServletResponse response,
                               Model model) {
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
            model.addAttribute(ERROR_MESSAGE_ATTRIBUTE,
                    "This isn't your patch of soil. No peeking at the neighbor's garden without an invite!");
            return "403";
        }

        if (idList == null || xCoordList == null || yCoordList == null) {
            return "redirect:/2D-garden/{gardenId}";
        }

        ObjectMapper objectMapper = new ObjectMapper();
        List<String> idListAsList = new ArrayList<>();
        List<Double> xCoordListAsList = new ArrayList<>();
        List<Double> yCoordListAsList = new ArrayList<>();
        try {
            idListAsList = objectMapper.readValue(idList, new TypeReference<List<String>>() {
            });
            xCoordListAsList = objectMapper.readValue(xCoordList, new TypeReference<List<Double>>() {
            });
            yCoordListAsList = objectMapper.readValue(yCoordList, new TypeReference<List<Double>>() {
            });
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        try {
            for (int i = 0; i < idListAsList.size(); i++) {
                // all items on grid are plants at the moment
                updateGardenGrid(GridItemType.PLANT, Long.parseLong(idListAsList.get(i)), xCoordListAsList.get(i).intValue(), yCoordListAsList.get(i).intValue(), garden);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("errorTitle", "400 Bad Request");
            model.addAttribute(ERROR_MESSAGE_ATTRIBUTE,
                    "Something went wrong. We could not save the changes");
            return "error";
        }

        return "redirect:/2D-garden/{gardenId}";
    }

    /**
     * Endpoint for clearing all elements from the 2D garden grid.
     * NOTE: Currently all gridItems are plants, decorations feature hasn't been added.
     *
     * @param gardenId   id of the garden whose grid has to be saved
     * @param response   http response to use to return error
     * @param model      model to use to return error
     * @return redirect back to 2D garden grid
     */
    @PostMapping("/2D-garden/{gardenId}/clear")
    public String clear2DGarden(@PathVariable Long gardenId,
                               HttpServletResponse response,
                               Model model) {
        logger.info("POST /2D-garden/{}/clear", gardenId);
        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);


        if (optionalGarden.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }

        Garden garden = optionalGarden.get();
        model.addAttribute("gardenId", garden.getGardenId());

        if (!securityService.isOwner(garden.getOwner().getId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            model.addAttribute(ERROR_MESSAGE_ATTRIBUTE,
                    "This isn't your patch of soil. No peeking at the neighbor's garden without an invite!");
            return "403";
        }

        logger.info("Clearing grid of garden with id: {}", gardenId);

        List<GridItemLocation> gridItems = gridItemLocationService.getGridItemLocationByGarden(garden);

        for (GridItemLocation gridItem : gridItems) {
            try {
                gridItemLocationService.removeGridItemLocation(gridItem);
            } catch (Exception exception) {
                logger.error("Error removing grid item with id {}: {}", gridItem.getId(), exception.getMessage());
            }
        }

        return "redirect:/2D-garden/{gardenId}";
    }

    /**
     * This endpoint handles deleting a single item from the grid.
     * @param gardenId id of the garden the grid belongs to
     * @param xCoord the x coordinate of the grid item
     * @param yCoord the y coordinate of the grid item
     * @param response http response to use to return error
     * @param model model to use to return error
     * @return redirect back to the 2d garden page
     */
    @GetMapping("/2D-garden/{gardenId}/delete")
    @ResponseBody
    public String deleteGridItem(@PathVariable Long gardenId,
                                 @RequestParam(value = "x_coord_delete") int xCoord,
                                 @RequestParam(value = "y_coord_delete") int yCoord,
                                 HttpServletResponse response,
                                 Model model) {


    logger.info("POST /2D-garden/{}/delete", gardenId);
    Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);


    if (optionalGarden.isEmpty()) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return "404";
    }

    Garden garden = optionalGarden.get();
    model.addAttribute("gardenId", garden.getGardenId());

    if (!securityService.isOwner(garden.getOwner().getId())) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        model.addAttribute(ERROR_MESSAGE_ATTRIBUTE,
                "This isn't your patch of soil. No peeking at the neighbor's garden without an invite!");
        return "403";
    }

    logger.info("Removing item at {}, {} on grid of garden with id: {}", xCoord, yCoord, gardenId);

        List<GridItemLocation> gridItems = gridItemLocationService.getGridItemLocationByGarden(garden);

        for (GridItemLocation gridItem : gridItems) {
            if (gridItem.getXCoordinate() == xCoord && gridItem.getYCoordinate() == yCoord) {
                gridItemLocationService.removeGridItemLocation(gridItem);
            }
        }

        return "200";
    }


}
