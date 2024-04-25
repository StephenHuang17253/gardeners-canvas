package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.InputValidator.InputValidator;
import nz.ac.canterbury.seng302.gardenersgrove.validation.InputValidator.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Objects;


@Controller
public class ResetPasswordController {
    Logger logger = LoggerFactory.getLogger(ResetPasswordController.class);

    private final UserService userService;

    @Autowired
    public ResetPasswordController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Gets the reset password page at /reset-password
     * @return the reset password form
     */
    @GetMapping("/reset-password")
    public String resetPassword() {
        logger.info("GET /reset-password");

        return "resetPasswordForm";

    }
    @PostMapping("/reset-password")
    public String passwordChecker(@RequestParam("password") String password, @RequestParam("retypePassword") String retypePassword, Model model) {
        ValidationResult passwordValidation = InputValidator.validatePassword(password);

        ValidationResult rePasswordValidation = InputValidator.validatePassword(password);
        if (!passwordValidation.valid()) {
            model.addAttribute("passwordError", passwordValidation);
        } else if (!Objects.equals(password, retypePassword)) {
            model.addAttribute("passwordError", "The passwords do not match");
        }
        return "resetPasswordForm";
    }

}
