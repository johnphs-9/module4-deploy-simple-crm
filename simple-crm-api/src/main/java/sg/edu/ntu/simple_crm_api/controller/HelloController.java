package sg.edu.ntu.simple_crm_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Protected endpoint demonstrating JWT enforcement (Assignment PT2 §2, L46-47).
 * Requires a valid Bearer token (SecurityConfig: /api/** authenticated).
 */
@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello! JWT authentication successful.";
    }
}
