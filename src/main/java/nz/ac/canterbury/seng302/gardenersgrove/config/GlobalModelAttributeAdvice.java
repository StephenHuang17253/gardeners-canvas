package nz.ac.canterbury.seng302.gardenersgrove.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Objects;

/**
 * Class to add model attributes across all controllers of the application
 */

@ControllerAdvice
public class GlobalModelAttributeAdvice {

    /**
     * Add loggedIn attribute to all controllers
     * @param model hashmap of variables that are injected into the view
     */
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && !Objects.equals(authentication.getName(), "anonymousUser");
        model.addAttribute("loggedIn", loggedIn);

    }
}
