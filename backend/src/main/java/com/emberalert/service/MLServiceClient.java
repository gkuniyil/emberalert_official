package com.emberalert.service;

import com.emberalert.model.FireRiskRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for calling the Python ML service
 * This is the bridge between Java backend and Python ML model
 */
@Service
public class MLServiceClient {
    
    @Value("${ml.service.url}")
    private String mlServiceUrl;
    
    private final RestTemplate restTemplate;
    
    public MLServiceClient() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Calls Python ML service to get fire risk prediction
     */
    public Map<String, Object> predictFireRisk(FireRiskRequest request) {
        try {
            System.out.println("🔥 Calling ML service at: " + mlServiceUrl + "/predict");
            
            // Build request payload for Python service
            Map<String, Object> payload = buildMLPayload(request);
            
            // Call Python ML service
            String url = mlServiceUrl + "/predict";
            Map<String, Object> response = restTemplate.postForObject(
                url, 
                payload, 
                Map.class
            );
            
            System.out.println("✅ ML service responded successfully");
            return response;
            
        } catch (RestClientException e) {
            System.err.println("❌ ML service error: " + e.getMessage());
            throw new RuntimeException("Failed to get prediction from ML service: " + e.getMessage());
        }
    }
    
    /**
     * Checks if ML service is healthy and responding
     */
    public boolean isMLServiceHealthy() {
        try {
            String url = mlServiceUrl + "/health";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return response != null && "healthy".equals(response.get("status"));
        } catch (Exception e) {
            System.err.println("⚠️ ML service health check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Converts our Java request to Python ML service format
     */
    private Map<String, Object> buildMLPayload(FireRiskRequest request) {
        Map<String, Object> payload = new HashMap<>();
        
        // Required fields
        payload.put("latitude", request.getLatitude());
        payload.put("longitude", request.getLongitude());
        payload.put("temperature", request.getTemperature());
        payload.put("humidity", request.getHumidity());
        payload.put("wind_speed", request.getWindSpeed());
        payload.put("vegetation_density", request.getVegetationDensity());
        
        // Optional fields with defaults
        payload.put("slope", request.getSlope() != null ? request.getSlope() : 15.0);
        payload.put("elevation", request.getElevation() != null ? request.getElevation() : 500.0);
        payload.put("days_since_rain", request.getDaysSinceRain() != null ? request.getDaysSinceRain() : 7);
        payload.put("proximity_to_water", request.getProximityToWater() != null ? request.getProximityToWater() : 5.0);
        
        return payload;
    }
}