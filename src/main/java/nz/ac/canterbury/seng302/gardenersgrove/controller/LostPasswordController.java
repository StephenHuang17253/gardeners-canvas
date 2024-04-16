package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
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

    @GetMapping("/lost-password")
    public String lostPassword() {

        logger.info("GET /lost-password");

        return "lostPasswordForm";

    }

    @PostMapping("/lost-password")
    public String checkEmail(@RequestParam("email") String email, Model model) {
        boolean isRegistered = userService.emailInUse(email);
        if (isRegistered) {
            model.addAttribute("message", "Okay");
        } else {
            model.addAttribute("message", "Error: Email not registered.");
        }
        return "lostPasswordForm";
    }
}
