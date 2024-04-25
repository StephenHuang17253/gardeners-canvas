package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidation.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LostPasswordController {
    Logger logger = LoggerFactory.getLogger(ResetPasswordController.class);

    private final UserService userService;

    @Autowired
    public LostPasswordController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Gets the lost password page at /lost-password
     * @return the lost password form
     */
    @GetMapping("/lost-password")
    public String lostPassword() {

        logger.info("GET /lost-password");

        return "lostPasswordForm";

    }

    @PostMapping("/lost-password")
    public String emailChecker(@RequestParam("email") String email, Model model) {
        boolean isRegistered = userService.emailInUse(email);
        ValidationResult emailValidation = InputValidator.validateUniqueEmail(email);

        if (!isRegistered && !emailValidation.valid()) {
            model.addAttribute("emailError", emailValidation);
        } else {
            model.addAttribute("message", "An email was sent to the address if it was recognised");
        }

        return "lostPasswordForm";
    }
}
