package ru.haritonenko.librarylendingservice.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/openapi.yaml")
    public String openApiYaml() {
        return "redirect:/v3/api-docs.yaml";
    }
}
