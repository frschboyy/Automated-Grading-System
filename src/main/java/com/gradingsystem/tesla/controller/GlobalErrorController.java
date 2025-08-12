package com.gradingsystem.tesla.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class GlobalErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAuthenticated = auth != null && auth.isAuthenticated()
                                  && !(auth.getPrincipal() instanceof String && auth.getPrincipal().equals("anonymousUser"));

        // Log the HTTP error status
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Integer statusCode = status != null ? Integer.valueOf(status.toString()) : null;
        log.info("Status Code: " + statusCode);

        if (isAuthenticated) {
            return "redirect:/dashboard";
        } else {
            return "redirect:/login";
        }
    }
}