package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service("securityService")
public class SecurityService {

    @Autowired
    UserService userService;
    @Autowired
    GardenService gardenService;
    Logger logger = LoggerFactory.getLogger(SecurityService.class);
    Authentication authentication;

    public boolean isOwner(Long gardenId){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User owner = this.userService.getUserByEmail(authentication.getName());
        Optional<Garden> garden = this.gardenService.findById(gardenId);
        return owner.getId() == garden.get().getOwner().getId();
    }
}
