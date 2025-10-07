package com.emberalert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main Spring Boot Application
 * CACHING
 */
@SpringBootApplication
@EnableCaching  // Enables Redis caching
public class EmberAlertApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(EmberAlertApplication.class, args);
        System.out.println("🔥 EmberAlert Backend API Started Successfully!");
        System.out.println("📍 Running on: http://localhost:8080");
        System.out.println("💾 Database: Connected to PostgreSQL");
        System.out.println("⚡ Cache: Redis enabled");
    }
}