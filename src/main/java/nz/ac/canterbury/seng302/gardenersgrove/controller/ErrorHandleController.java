package nz.ac.canterbury.seng302.gardenersgrove.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Spring boot controller class for handling errors. note the {@link Controller}
 * annotation which defines this. There must not exist an error.html page in the
 * resources/templates folder otherwise that will be used instead of this
 * controller.
 */
@Controller
public class ErrorHandleController implements ErrorController {

    Logger logger = LoggerFactory.getLogger(ErrorHandleController.class);

    /**
     * This method is called when an error occurs in the application. It returns the
     * relevant error page
     * 
     * @param request the request object
     * @return the error page to display
     */
    @GetMapping("/error")
    public String handleError(HttpServletRequest request) {

        logger.error("An Error Occurred");

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
