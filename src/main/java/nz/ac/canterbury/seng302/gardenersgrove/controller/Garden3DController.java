package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.gardenersgrove.component.DailyWeather;
import nz.ac.canterbury.seng302.gardenersgrove.component.WeatherResponseData;
import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.model.DisplayableItem;
import nz.ac.canterbury.seng302.gardenersgrove.model.GardenDetailModel;
import nz.ac.canterbury.seng302.gardenersgrove.model.TileModel;
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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
public class Garden3DController {

    Logger logger = LoggerFactory.getLogger(Garden3DController.class);

    private final GardenService gardenService;
    private final DecorationService decorationService;
    private final GridItemLocationService gridItemLocationService;
    private final SecurityService securityService;
    private final FriendshipService friendshipService;
    private final WeatherService weatherService;
    private final PlantService plantService;

    private final GardenTileService gardenTileService;

    /**
     * Gets services used in this controller
     *
     * @param gardenService           handles interactions with garden repository
     * @param securityService         handles security
     * @param friendshipService       handles interactions with friendship repository
     * @param gridItemLocationService handles interactions with gridItemLocations repository
     * @param weatherService handles interaction with weather API and weather objects
     * @param plantService            handles interactions with plant repository
     * @param decorationService       handles interactions with decoration repository
     */
    @Autowired
    public Garden3DController(GardenService gardenService, SecurityService securityService,
                              FriendshipService friendshipService, GridItemLocationService gridItemLocationService,
                              WeatherService weatherService,
                              PlantService plantService, DecorationService decorationService, GardenTileService gardenTileService) {
        this.gardenService = gardenService;
        this.gridItemLocationService = gridItemLocationService;
        this.securityService = securityService;
        this.friendshipService = friendshipService;
        this.plantService = plantService;
        this.weatherService = weatherService;
        this.decorationService = decorationService;
        this.gardenTileService = gardenTileService;
    }

    /**
     * Returns page for viewing 3D garden
     *
     * @param gardenId id of garden to view
     * @param response HttpServeletResponse for any errors (404, 403)
     * @param model    model to contain html page inputs
     * @return garden3Dpage html page
     */
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

        WeatherResponseData weatherData = weatherService.getWeather(garden.getGardenLatitude(),
                garden.getGardenLongitude());

        if (weatherData != null) {
            String timezone = weatherData.getTimeZone();
            Instant currentTime = Instant.now();
            ZoneId zone = ZoneId.of(timezone);
            ZonedDateTime zonedTime = currentTime.atZone(zone);
            int hourAtLocation = zonedTime.getHour();

            List<DailyWeather> weeksWeather = weatherData.getRetrievedWeatherData();
            DailyWeather todaysWeather = weeksWeather.get(2);
            String todaysWeatherType = todaysWeather.getDescription();

            model.addAttribute("currentHour", hourAtLocation);
            model.addAttribute("weather", todaysWeatherType);
        }

        model.addAttribute("garden", new GardenDetailModel(garden));
        model.addAttribute("isOwner", securityService.isOwner(garden.getOwner().getId()));
        return "garden3DPage";
    }


    /**
     * Turns all gridItemLocations of a garden into a usable model for actual display on client side
     *
     * @param gridItemLocations all items in grid
     * @return displayableItems which are gridItemLocations formatted such that they can be displayed
     */
    private List<DisplayableItem> extractDisplayableItems(List<GridItemLocation> gridItemLocations) {

        List<DisplayableItem> displayableItems = new ArrayList<>();

        for (GridItemLocation gridLocation : gridItemLocations) {
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
                } else {
                    logger.warn("Plant grid item could not be added to grid, missing item, id {}", gridLocation.getId());
                }
            } else if (gridLocation.getItemType() == GridItemType.DECORATION) {
                Optional<Decoration> optionalDecoration = decorationService.getById(gridLocation.getObjectId());
                if (optionalDecoration.isPresent()) {
                    Decoration currentDecoration = optionalDecoration.get();
                    displayableItems.add(new DisplayableItem(
                            gridLocation.getXCoordinate(),
                            gridLocation.getYCoordinate(),
                            currentDecoration.getDecorationCategory().getCategoryName(), // using category name as item name
                            currentDecoration.getDecorationCategory().toString(),
                            currentDecoration.getId(),
                            gridLocation.getItemType(),
                            currentDecoration.getDecorationCategory().getCategoryImage()));
                } else {
                    logger.warn("Decoration grid item could not be added to grid, missing item, id {}", gridLocation.getId());
                }
            } else {
                logger.warn("GridItemType is invalid. It is not DECORATION, nor PLANT");
            }
        }
        return displayableItems;
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

        List<GridItemLocation> gridItemLocations = gridItemLocationService.getGridItemLocationByGarden(garden);

        displayableItems = extractDisplayableItems(gridItemLocations);
        return displayableItems;
    }

    @ResponseBody
    @GetMapping("/3D-tile-textures-grid/{gardenId}")
    public List<TileModel> getGardenTileGrid(@PathVariable Long gardenId,
                                             HttpServletResponse response) {
        logger.info("GET /3D-tile-textures-grid/{}", gardenId);
        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);

        if (optionalGarden.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ArrayList<>();
        }

        Garden garden = optionalGarden.get();
        User currentUser = securityService.getCurrentUser();
        User gardenOwner = garden.getOwner();

        if (!(garden.getIsPublic() || Objects.equals(gardenOwner.getId(), currentUser.getId())
                || friendshipService.checkFriendshipStatus(gardenOwner, currentUser) == FriendshipStatus.ACCEPTED)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return new ArrayList<>();
        }

        List<TileModel> textures = gardenTileService.getGardenTilesByGarden(garden).stream().map(tile -> new TileModel(tile.getTileTexture(), tile.getXCoordinate(), tile.getYCoordinate())).toList();
        return textures;
    }

}