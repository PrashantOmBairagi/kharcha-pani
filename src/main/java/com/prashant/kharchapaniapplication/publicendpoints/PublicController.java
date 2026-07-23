package com.prashant.kharchapaniapplication.publicendpoints;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of(
                "application", "Smart Finance Tracker",
                "version", "2.0",
                "status", "ACTIVE",
                "timestamp", String.valueOf(new Date(System.currentTimeMillis())),
                "Author","Prashant Bairagi"
        );
    }
}
