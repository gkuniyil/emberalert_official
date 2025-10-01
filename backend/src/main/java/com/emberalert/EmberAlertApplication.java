package com.emberalert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EmberAlertApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(EmberAlertApplication.class, args);
        System.out.println("🔥 EmberAlert Backend API Started Successfully!");
        System.out.println("📍 Running on: http://localhost:8080");
        System.out.println("📊 Health Check: http://localhost:8080/api/health");
    }
}