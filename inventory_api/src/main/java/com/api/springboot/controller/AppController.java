package com.api.springboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class AppController {
    /**
     * Get api reference
     * @return api reference page
     */
    @RequestMapping("/api")
    public String getApiRef() {
        return "apiref";
    }
   

}