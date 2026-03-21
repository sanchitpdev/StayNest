package com.staynest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for monitoring application status
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    /**
     * Basic health check endpoint.
     */
    @GetMapping
    public ResponseEntity<Map<String,Object>> healthCheck(){
        Map<String,Object> health = new HashMap<>();
        health.put("status","UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service","StayNest API");
        health.put("version", "1.0.0");

        return ResponseEntity.ok(health);
    }

    /**
     * Detailed health check with database status
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String,Object>> detailedHealthCheck(){
        Map<String,Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "StayNest API");
        health.put("version","1.0.0");

        //check database connection
        try (Connection connection = dataSource.getConnection()){
            health.put("database","UP");
            health.put("databaseType",connection.getMetaData().getDatabaseProductName());
        }catch (Exception e){
            health.put("database","DOWN");
            health.put("databaseError",e.getMessage());
        }
        return ResponseEntity.ok(health);
    }

    /**
     * Simple ping endpoint
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String,String>> ping(){
        return ResponseEntity.ok(Map.of("message","pong"));
    }
}
