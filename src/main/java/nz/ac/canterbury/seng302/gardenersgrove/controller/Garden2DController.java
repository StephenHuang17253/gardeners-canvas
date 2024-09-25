package nz.ac.canterbury.seng302.gardenersgrove.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Decoration;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTile;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GridItemLocation;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.model.DisplayableItem;
import nz.ac.canterbury.seng302.gardenersgrove.model.Plant2DModel;
import nz.ac.canterbury.seng302.gardenersgrove.model.GardenDetailModel;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.GridItemType;
import nz.ac.canterbury.seng302.gardenersgrove.util.ItemType;
import nz.ac.canterbury.seng302.gardenersgrove.util.TileType;
import nz.ac.canterbury.seng302.gardenersgrove.util.TileTexture;

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

    private final PlantService plantService;

    private final DecorationService decorationService;

    private final GridItemLocationService gridItemLocationService;

    private final GardenTileService gardenTileService;

    private static final int COUNT_PER_PAGE = 6;

    private static final String ERROR_MESSAGE_ATTRIBUTE = "message";

    private static final int GRID_COLUMNS = 7;

    private static final int GRID_ROWS = 7;

    @Autowired
    public Garden2DController(GardenService gardenService, SecurityService securityService,
            GridItemLocationService gridItemLocationService, PlantService plantService,
            DecorationService decorationService, GardenTileService gardenTileService) {
        this.gardenService = gardenService;
        this.securityService = securityService;
        this.gridItemLocationService = gridItemLocationService;
        this.plantService = plantService;
        this.decorationService = decorationService;
        this.gardenTileService = gardenTileService;
    }

    /**
     * Returns the 2D Garden Page where the user can edit the layout of their
     * garden.
     */
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

        List<Plant2DModel> plants = garden.getPlants().stream()
                .sorted(Comparator.comparing(Plant::getPlantName))
                .map(Plant2DModel::new)
                .toList();
        model.addAttribute("plants", plants);
        model.addAttribute("countPerPage", COUNT_PER_PAGE);

        Map<Long, Plant> plantsById = garden.getPlants().stream()
                .collect(Collectors.toMap(Plant::getPlantId, Function.identity()));

        List<GridItemLocation> gridLocations = gridItemLocationService.getGridItemLocationByGarden(garden);

        List<DisplayableItem> displayableItems = new ArrayList<>();

        for (GridItemLocation gridLocation : gridLocations) {
            if (gridLocation.getItemType() == GridItemType.PLANT) {
                Optional<Plant> optionalPlant = plantService.getById(gridLocation.getObjectId());
                if (optionalPlant.isPresent()) {
                    Plant currentPlant = optionalPlant.get();
                    displayableItems.add(new DisplayableItem(
                            gridLocation.getXCoordinate(),
                            gridLocation.getYCoordinate(),
                            currentPlant.getPlantName(),
                            currentPlant.getPlantCategory().toString(),
                            gridLocation.getObjectId(),
                            gridLocation.getItemType(),
                            currentPlant.getPlantCategory().getCategoryImage()));
                }
            } else if (gridLocation.getItemType() == GridItemType.DECORATION) {
                Optional<Decoration> optionalDecoration = decorationService.getById(gridLocation.getObjectId());
                if (optionalDecoration.isPresent()) {
                    Decoration currentDecoration = optionalDecoration.get();
                    displayableItems.add(new DisplayableItem(
                            gridLocation.getXCoordinate(),
                            gridLocation.getYCoordinate(),
                            currentDecoration.getDecorationCategory().getCategoryName(), // using category name as item
                                                                                         // name
                            currentDecoration.getDecorationCategory().toString(),
                            currentDecoration.getId(),
                            gridLocation.getItemType(),
                            currentDecoration.getDecorationCategory().getCategoryImage()));
                }
            }

        }

        model.addAttribute("isOwner", true);
        model.addAttribute("garden", new GardenDetailModel(optionalGarden.get()));
        model.addAttribute("displayableItemsList", displayableItems);
        model.addAttribute("plantsById", plantsById);
        model.addAttribute("tileTextures", TileTexture.values());
        return "garden2DPage";
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
    private void updateGardenGrid(GridItemType gridItemType, Long itemId, Integer xCoord, Integer yCoord,
            Garden garden) {
        GridItemLocation newGridItemLocation = new GridItemLocation(itemId, gridItemType, garden, xCoord, yCoord);
        gridItemLocationService.addGridItemLocation(newGridItemLocation);
    }

    /**
     * Helper function to delete grid items of given garden.
     *
     * @param garden garden whose grid items have to be deleted
     */
    private void deleteOldGridLocationItems(Garden garden) {
        List<GridItemLocation> gridItems = gridItemLocationService.getGridItemLocationByGarden(garden);

        for (GridItemLocation gridItem : gridItems) {
            try {
                gridItemLocationService.removeGridItemLocation(gridItem);
            } catch (Exception exception) {
                logger.error("Error removing grid item with id {}: {}", gridItem.getId(), exception.getMessage());
            }
        }
    }

    /**
     * Endpoint to save the locations of elements on a 2D garden grid.
     *
     * @param gardenId   id of the garden whose grid has to be saved
     * @param idList     ids of all elements on the grid
     * @param typeList   gridItemType of all elements on the grid
     * @param xCoordList x coordinates of all elements on the grid
     * @param yCoordList y coordinates of all elements on the grid
     * @param response   http response to use to return error
     * @param model      model to use to return error
     * @return redirect back to 2D garden grid
     */
    @PostMapping("/2D-garden/{gardenId}/save")
    public String save2DGarden(@PathVariable Long gardenId,
            @RequestParam(value = "idList", required = false) String idList,
            @RequestParam(value = "typeList", required = false) String typeList,
            @RequestParam(value = "xCoordList", required = false) String xCoordList,
            @RequestParam(value = "yCoordList", required = false) String yCoordList,
            @RequestParam(value = "tileTextureList", required = false) String tileTextureList,
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

        if (idList == null || xCoordList == null || yCoordList == null || tileTextureList == null) {
            return "redirect:/2D-garden/{gardenId}";
        }

        // converting json input to arrays
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> idListAsList = new ArrayList<>();
        List<String> typeListAsList = new ArrayList<>();
        List<Double> xCoordListAsList = new ArrayList<>();
        List<Double> yCoordListAsList = new ArrayList<>();
        List<String> tileTextureListAsList = new ArrayList<>();

        try {
            idListAsList = objectMapper.readValue(idList, new TypeReference<>() {
            });
            typeListAsList = objectMapper.readValue(typeList, new TypeReference<>() {
            });
            xCoordListAsList = objectMapper.readValue(xCoordList, new TypeReference<>() {
            });
            yCoordListAsList = objectMapper.readValue(yCoordList, new TypeReference<>() {
            });
            tileTextureListAsList = objectMapper.readValue(tileTextureList, new TypeReference<>() {
            });
        } catch (Exception error) {
            logger.error("Could not read 2D layout {}", error.getMessage());
        }

        if (idListAsList.size() != xCoordListAsList.size() ||
                idListAsList.size() != yCoordListAsList.size() ||
                idListAsList.size() != typeListAsList.size() ||
                tileTextureListAsList.size() != GRID_COLUMNS * GRID_ROWS) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("errorTitle", "400 Bad Request");
            model.addAttribute(ERROR_MESSAGE_ATTRIBUTE,
                    "Something went wrong. We could not save the changes");
            return "error";
        }

        for (int i = 0; i < GRID_COLUMNS; i++) {
            for (int j = 0; j < GRID_ROWS; j++) {
                String tileTextureString = tileTextureListAsList.get(i * GRID_COLUMNS + j);
                TileType tileType = TileType.valueOf(tileTextureString);
                GardenTile gardenTile = new GardenTile(garden, tileType, j, i);
                gardenTileService.addGardenTile(gardenTile);
            }
        }

        // updating the repository
        deleteOldGridLocationItems(garden);
        for (int i = 0; i < idListAsList.size(); i++) {
            updateGardenGrid(GridItemType.valueOf(typeListAsList.get(i)), Long.parseLong(idListAsList.get(i)),
                    xCoordListAsList.get(i).intValue(), yCoordListAsList.get(i).intValue(), garden);
        }
        return "redirect:/2D-garden/{gardenId}";
    }
}
