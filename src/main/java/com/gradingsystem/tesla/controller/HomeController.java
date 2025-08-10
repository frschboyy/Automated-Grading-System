package com.gradingsystem.tesla.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication auth) {
        boolean isAuthenticated = auth != null 
                                  && auth.isAuthenticated() 
                                  && !(auth.getPrincipal() instanceof String 
                                       && auth.getPrincipal().equals("anonymousUser"));

        if (isAuthenticated) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }
}
