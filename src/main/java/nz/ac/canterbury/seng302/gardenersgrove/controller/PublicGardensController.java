package nz.ac.canterbury.seng302.gardenersgrove.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;

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
     * returns a page with the 10 most recent public gardens based on current page in pagination
     * Page number index starts at 1, so page 1 gets gardens 1-10 latest gardens, page 2 gets 11-20 and so on
     *
     * @return thymeleaf BrowsePublicGardens html element
     */
    @GetMapping("/public-gardens/page/{pageNumber}")
    public String publicGardensPagination(@PathVariable Long pageNumber, Model model) {
        logger.info("GET /public-gardens");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && !Objects.equals(authentication.getName(), "anonymousUser");
        model.addAttribute("loggedIn", loggedIn);

        List<Garden> allGardens = gardenService.getAllPublicGardens();
        int totalGardens = allGardens.size();
        int pageSize = 10;
        int startIndex = Math.toIntExact((pageNumber - 1) * pageSize);
        int endIndex = Math.min(startIndex + pageSize, totalGardens);
        int lastPage = (int) Math.ceil((double) totalGardens / pageSize);

        if (lastPage == 0) {
            return "redirect:/home";
        }

        if (pageNumber > lastPage) {
            return "redirect:/public-gardens/page/" + lastPage;
        }
        if (pageNumber < 1) {
            return "redirect:/public-gardens/page/1";
        }


        List<Garden> tenSortedPublicGardens = allGardens.stream()
                .sorted(Comparator.comparing(Garden::getCreationDate).reversed())
                .skip((pageNumber - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());

        model.addAttribute("publicGardens", tenSortedPublicGardens);
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalGardens", totalGardens);
        model.addAttribute("startIndex", startIndex + 1);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("lastPage", lastPage);
        model.addAttribute("SearchErrorText", "");
        model.addAttribute("searchValue", "");
        return "browsePublicGardens";
    }

    /**
     * Redirects to pagination page one
     *
     * @return redirect to page/1
     */
    @GetMapping("/public-gardens")
    public String publicGardens(Model model) {
        logger.info("GET /public-gardens");
        return "redirect:/public-gardens/page/1";
    }


    /**
     * returns a page with the 10 most recent public gardens based on search and on current page in pagination
     * Page number index starts at 1, so page 1 gets gardens 1-10 latest gardens, page 2 gets 11-20 and so on
     *
     * @return thymeleaf BrowsePublicGardens html element
     */
    @GetMapping("/public-gardens/search/{pageNumber}")
    public String publicGardens(@RequestParam(name = "searchInput", defaultValue = "", required = false) String searchInput,
                                @PathVariable Long pageNumber,
                                Model model) {
        logger.info("GET /public-gardens/search");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && !Objects.equals(authentication.getName(), "anonymousUser");
        model.addAttribute("loggedIn", loggedIn);

        if (Objects.equals(searchInput, "")) {
            return "redirect:/public-gardens/page/1";
        }

        List<Garden> matchingGardens = gardenService.getMatchingGardens(searchInput);

        if (!matchingGardens.isEmpty()) {
            int totalGardens = matchingGardens.size();
            int pageSize = 10;
            int startIndex = Math.toIntExact((pageNumber - 1) * pageSize);
            int endIndex = Math.min(startIndex + pageSize, totalGardens);
            int lastPage = (int) Math.ceil((double) totalGardens / pageSize);

            if (pageNumber > lastPage) {
                return "redirect:/public-gardens/search/" + lastPage;
            }
            if (pageNumber < 1) {
                return "redirect:/public-gardens/search/1";
            }

            List<Garden> tenSortedPublicGardens = matchingGardens.stream()
                    .sorted(Comparator.comparing(Garden::getCreationDate).reversed())
                    .skip((pageNumber - 1) * pageSize)
                    .limit(pageSize)
                    .collect(Collectors.toList());

            model.addAttribute("publicGardens", tenSortedPublicGardens);
            model.addAttribute("currentPage", pageNumber);
            model.addAttribute("totalGardens", totalGardens);
            model.addAttribute("startIndex", startIndex + 1);
            model.addAttribute("endIndex", endIndex);
            model.addAttribute("lastPage", lastPage);
            model.addAttribute("SearchErrorText", "");
            model.addAttribute("searchValue", searchInput);
        } else {
            model = resetModel(model);
            model.addAttribute("searchValue", searchInput);
            model.addAttribute("SearchErrorText", "No gardens match your search");
        }
        return "browsePublicGardens";
    }


    /**
     * Resets model send to Browse gardens page
     *
     * @param model model to reset
     * @return model with default values
     */
    Model resetModel(Model model) {
        List<Garden> emptyGardensList = new ArrayList<>();
        model.addAttribute("publicGardens", emptyGardensList);
        model.addAttribute("currentPage", 1);
        model.addAttribute("totalGardens", 0);
        model.addAttribute("startIndex", 0);
        model.addAttribute("endIndex", 0);
        model.addAttribute("lastPage", 1);
        model.addAttribute("SearchErrorText", "");
        model.addAttribute("searchValue", "");
        return model;
    }


}

