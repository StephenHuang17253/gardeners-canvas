package nz.ac.canterbury.seng302.gardenersgrove.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorHandleController implements ErrorController {

    Logger logger = LoggerFactory.getLogger(ErrorHandleController.class);

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        
        logger.error("An Error Ocurred");

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status == null) {
            return "500";
        }

        Integer statusCode = Integer.valueOf(status.toString());
        switch (statusCode) {
            case 403 -> {
                return "403";
            }
            case 404 -> {
                return "404";
            }
            default -> {
                return "500";
            }
        }

    }
}
