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
            String detailedMessage;
            String quickMessage;

           switch (statusCode) {
                case 400 -> {
                    quickMessage = "Bad Request!";
                    detailedMessage = "The server could not understand the request due to invalid syntax.";
                }
                case 401 -> {
                    quickMessage = "Unauthorized!";
                    detailedMessage = "You must be authenticated to access this resource.";
                }
                case 403 -> {
                    quickMessage = "Forbidden!";
                    detailedMessage = "You do not have permission to access this resource.";
                }
                case 404 -> {
                    quickMessage = "Not Found!";
                    detailedMessage = "The requested resource could not be found on this server.";
                }
                case 405 -> {
                    quickMessage = "Method Not Allowed!";
                    detailedMessage = "The request method is not supported for the requested resource.";
                }
               case 408 -> {
                   quickMessage = "Request Timeout!";
                   detailedMessage = "The server timed out waiting for the request.";
               }
                case 500 -> {
                    quickMessage = "Internal Server Error!";
                    detailedMessage = "The server encountered an internal error or misconfiguration and was unable to complete your request.";
                }
               case 501 -> {
                   quickMessage = "Not Implemented!";
                   detailedMessage = "The server either does not recognize the request method, or it lacks the ability to fulfill the request.";
               }
               case 502 -> {
                   quickMessage = "Bad Gateway!";
                   detailedMessage = "The server was acting as a gateway or proxy and received an invalid response from the upstream server.";
               }
                case 503 -> {
                    quickMessage = "Service Unavailable!";
                    detailedMessage = "The server is currently unavailable (because it is overloaded or down for maintenance).";
                }
               case 504 -> {
                   quickMessage = "Gateway Timeout!";
                   detailedMessage = "\tThe server was acting as a gateway or proxy and did not receive a timely response from the upstream server.";
               }
                default -> {
                    quickMessage = "Unknown Error!";
                    detailedMessage = "An unexpected error occurred. Please try again later.";
                }
            };

            mav.addObject("errorCode", statusCode);
            mav.addObject("detailedMessage", detailedMessage);
            mav.addObject("quickMessage", quickMessage);
        }

        return mav;
    }

    public String getErrorPath() {
        return "/error";
    }
}
