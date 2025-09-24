package com.back.global.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Value("${custom.site.frontUrl}")
    private String frontUrl;

    @GetMapping("/")
    @Profile("dev")
    public String redirectToSwagger() {
        return "redirect:/swagger-ui/index.html";
    }

    @GetMapping("/")
    @Profile("prod")
    public String redirectToFrontend() {
        return "redirect:" + frontUrl;
    }
}