package com.springboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        // http://localhost:8080 들어오면 /diary로 보내기
        return "redirect:/diary";
    }
}
