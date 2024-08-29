package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.model.GardenDetailModel;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendshipService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Objects;
import java.util.Optional;

@Controller
public class Garden3DController {

    Logger logger = LoggerFactory.getLogger(Garden3DController.class);

    private final GardenService gardenService;
    private final SecurityService securityService;
    private final FriendshipService friendshipService;

    @Autowired
    public Garden3DController(GardenService gardenService, SecurityService securityService, FriendshipService friendshipService){
        this.gardenService = gardenService;
        this.securityService = securityService;
        this.friendshipService = friendshipService;
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

        Garden garden = optionalGarden.get();
        User currentUser = securityService.getCurrentUser();
        User gardenOwner = garden.getOwner();

        if (!garden.getIsPublic() ) {
            FriendshipStatus userOwnerRelationship;
            if(Objects.equals(gardenOwner.getId(), currentUser.getId()))
            {
                userOwnerRelationship = FriendshipStatus.ACCEPTED;
            }
            else
            {
                userOwnerRelationship = friendshipService.checkFriendshipStatus(gardenOwner,currentUser);
            }

            if (userOwnerRelationship != FriendshipStatus.ACCEPTED)
            {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                model.addAttribute("message", "This isn't your patch of soil. No peeking at the neighbor's garden without an invite!");
                return "403";
            }

        }

        model.addAttribute("garden", new GardenDetailModel(optionalGarden.get()));
        model.addAttribute("isOwner", securityService.isOwner(garden.getOwner().getId()));
        return "garden3DPage";
    }

}
