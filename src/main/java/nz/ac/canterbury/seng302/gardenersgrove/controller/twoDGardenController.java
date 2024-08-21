package nz.ac.canterbury.seng302.gardenersgrove.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class twoDGardenController {

    @GetMapping("/2d")
    public String twoDGardenPlan() {
        return "2dGardenPlan";
    }
}
