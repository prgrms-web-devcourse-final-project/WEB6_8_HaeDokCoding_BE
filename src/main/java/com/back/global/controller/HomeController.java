package com.back.global.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Value("${custom.site.frontUrl}")
    private String frontUrl;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @GetMapping("/")
    public String redirect() {
        if("prod".equals(activeProfile)){
            return "redirect:" + frontUrl;
        }
        return "redirect:/swagger-ui/index.html";
    }
}