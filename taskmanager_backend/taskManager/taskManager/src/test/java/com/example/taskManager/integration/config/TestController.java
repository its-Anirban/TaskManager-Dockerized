package com.example.taskManager.integration.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/auth/test")
    public String authPublic() {
        return "auth-ok";
    }

    @GetMapping("/private/test")
    public String privateEndpoint() {
        return "private";
    }
}
