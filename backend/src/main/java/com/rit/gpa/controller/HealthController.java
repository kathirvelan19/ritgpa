package com.rit.gpa.controller;

import com.rit.gpa.config.FirebaseConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final FirebaseConfig firebaseConfig;

    public HealthController(FirebaseConfig firebaseConfig) {
        this.firebaseConfig = firebaseConfig;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "RIT GPA Calculator Backend");
        status.put("javaVersion", System.getProperty("java.version"));
        status.put("firebaseAdminInitialized", firebaseConfig.isInitialized());
        status.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(status);
    }
}
