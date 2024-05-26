package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for viewing all public gardens
 */
@Controller
public class PublicGardensController {

    Logger logger = LoggerFactory.getLogger(PublicGardensController.class);

    private final GardenService gardenService;


    @Autowired
    public PublicGardensController(GardenService gardenService) {
        this.gardenService = gardenService;


    }

    /**
     * returns a page with the 10 most recent public gardens
     *
     * @return thymeleaf BrowsePublicGardens html element
     */
    @GetMapping("/public-gardens/page/{pageNumber}")
    public String publicGardensPagination(@PathVariable Long pageNumber, Model model) {
        logger.info("GET /public-gardens");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);

        List<Garden> allGardens = gardenService.getGardens();
        int totalGardens = allGardens.size();
        int pageSize = 10;
        int startIndex = Math.toIntExact((pageNumber - 1) * pageSize);
        int endIndex = Math.min(startIndex + pageSize, totalGardens);
        int lastPage = (int) Math.ceil((double) totalGardens / pageSize);

        List<Garden> tenSortedPublicGardens = allGardens.stream()
                .sorted(Comparator.comparing(Garden::getCreationDate))
                .skip((pageNumber - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());

        model.addAttribute("publicGardens", tenSortedPublicGardens);
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalGardens", totalGardens);
        model.addAttribute("startIndex", startIndex+1);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("lastPage", lastPage);

        return "BrowsePublicGardens";
    }

    @GetMapping("/public-gardens")
    public String publicGardens(Model model) {
        logger.info("GET /public-gardens");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);

        return "redirect:/public-gardens/page/1";
    }


}

