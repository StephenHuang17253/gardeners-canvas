package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class ResetPasswordController {
    Logger logger = LoggerFactory.getLogger(ResetPasswordController.class);

    private final UserService userService;

    @Autowired
    public ResetPasswordController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/reset-password")
    public String resetPassword() {

        logger.info("GET /reset-password");

        return "resetPasswordForm";

    }

}
