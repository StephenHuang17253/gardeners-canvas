package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("securityService")
public class SecurityService {


    private final UserService userService;

    private final GardenService gardenService;
    Logger logger = LoggerFactory.getLogger(SecurityService.class);

    @Autowired
    public SecurityService(UserService userService, GardenService gardenService){
        this.userService = userService;
        this.gardenService = gardenService;

    }

    public boolean isOwner(Long gardenId){
        logger.info("Security check");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User owner = userService.getUserByEmail(authentication.getName());
        Garden garden = gardenService.findById(gardenId).get();
        return owner.getId() == garden.getOwner().getId();
    }
}
