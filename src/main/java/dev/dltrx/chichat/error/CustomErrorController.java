package dev.dltrx.chichat.error;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CustomErrorController  implements ErrorController {

    @GetMapping("/error")
    public ModelAndView handleError(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("error");
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            String errorMessage = "";
            String quickMessage = "An unknown error occurred.";

            errorMessage = switch (statusCode) {
                case 404 -> {
                    quickMessage = "Page not found!";
                    yield "The page you are looking for might have been removed, had its name changed, or is temporarily unavailable.";
                }
                case 403 -> {
                    quickMessage = "Access forbidden!";
                    yield "You don't have permission to access the requested resource.";
                }
                case 500 -> {
                    quickMessage = "Internal Server Error!";
                    yield "The server encountered an internal error or misconfiguration and was unable to complete your request.";
                    // Add more cases as needed
                }
                default -> errorMessage;
            };

            mav.addObject("errorCode", statusCode);
            mav.addObject("errorMessage", errorMessage);
            mav.addObject("quickMessage", quickMessage);
        }

        return mav;
    }

    public String getErrorPath() {
        return "/error";
    }
}
