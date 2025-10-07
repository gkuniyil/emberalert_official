package com.emberalert.service;

import com.emberalert.entity.FirePrediction;           // The database entity we created
import com.emberalert.model.FireRiskRequest;           // User input model
import com.emberalert.repository.FirePredictionRepository;  // Database interface
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.cache.annotation.Cacheable;  // ADD THIS LINE!

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ML Service Client - NOW WITH DATABASE STORAGE!
 * 
 * What this class does:
 * 1. Calls Python ML service to get fire risk prediction
 * 2. Saves the prediction to PostgreSQL database
 * 3. Can retrieve historical predictions
 */
@Service  // Tells Spring Boot: "I'm a service class, manage me!"
public class MLServiceClient {
    
    // Read ML service URL from application.yml (http://localhost:5000)
    @Value("${ml.service.url}")
    private String mlServiceUrl;
    
    // HTTP client to make calls to Python
    private final RestTemplate restTemplate;
    
    // DATABASE REPOSITORY - NEW!
    // This lets us save/retrieve from PostgreSQL
    @Autowired  // Spring Boot automatically injects the repository
    private FirePredictionRepository repository;
    
    // Constructor - creates RestTemplate
    public MLServiceClient() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Main method: Get prediction from ML service AND save to database
     * 
     * Flow:
     * 1. Call Python ML service
     * 2. Get prediction back
     * 3. Save to database
     * 4. Return prediction
     */
    /**
 * Main method: Get prediction from ML service AND save to database
 * NOW WITH CACHING!
 */
@Cacheable(value = "predictions", key = "#request.latitude + '-' + #request.longitude + '-' + #request.temperature")
public Map<String, Object> predictFireRisk(FireRiskRequest request) {
    try {
        System.out.println("🔥 CACHE MISS - Calling ML service at: " + mlServiceUrl + "/predict");
        
        // Build request payload for Python
        Map<String, Object> payload = buildMLPayload(request);
        
        // Call Python ML service
        String url = mlServiceUrl + "/predict";
        Map<String, Object> mlResponse = restTemplate.postForObject(
            url, 
            payload, 
            Map.class
        );
        
        System.out.println("✅ ML service responded successfully");
        
        // Save to database
        savePredictionToDatabase(request, mlResponse);
        
        return mlResponse;
        
    } catch (RestClientException e) {
        System.err.println("❌ ML service error: " + e.getMessage());
        throw new RuntimeException("Failed to get prediction from ML service: " + e.getMessage());
    }
}
    
    /**
     * NEW METHOD: Saves prediction to PostgreSQL database
     * 
     * Takes the user's request and ML's response, creates database record
     */
    private void savePredictionToDatabase(FireRiskRequest request, Map<String, Object> mlResponse) {
        try {
            // Create new database entity
            FirePrediction prediction = new FirePrediction();
            
            // Set input data (what user sent)
            prediction.setLatitude(request.getLatitude());
            prediction.setLongitude(request.getLongitude());
            prediction.setTemperature(request.getTemperature());
            prediction.setHumidity(request.getHumidity());
            prediction.setWindSpeed(request.getWindSpeed());
            prediction.setVegetationDensity(request.getVegetationDensity());
            prediction.setSlope(request.getSlope());
            prediction.setElevation(request.getElevation());
            prediction.setDaysSinceRain(request.getDaysSinceRain());
            prediction.setProximityToWater(request.getProximityToWater());
            
            // Set output data (what ML model predicted)
            Double riskScore = ((Number) mlResponse.get("risk_score")).doubleValue();
            String riskCategory = (String) mlResponse.get("risk_category");
            Double confidence = ((Number) mlResponse.get("model_confidence")).doubleValue();
            
            prediction.setRiskScore(riskScore);
            prediction.setRiskCategory(riskCategory);
            prediction.setModelConfidence(confidence);
            
            // SAVE TO DATABASE - This one line saves everything!
            repository.save(prediction);
            
            System.out.println("💾 Prediction saved to database with ID: " + prediction.getId());
            
        } catch (Exception e) {
            System.err.println("⚠️ Failed to save to database: " + e.getMessage());
            // Don't throw error - prediction still works even if save fails
        }
    }
    
    /**
     * NEW METHOD: Get all predictions from database
     */
    public List<FirePrediction> getAllPredictions() {
        return repository.findAll();
    }
    
    /**
     * NEW METHOD: Get predictions near a location
     */
    public List<FirePrediction> getPredictionsNearLocation(Double latitude, Double longitude) {
        return repository.findByLocationNear(latitude, longitude);
    }
    
    /**
     * NEW METHOD: Get high-risk predictions
     */
    public List<FirePrediction> getHighRiskPredictions(Double threshold) {
        return repository.findByRiskScoreGreaterThan(threshold);
    }
    
    /**
     * Checks if ML service is healthy
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
     * Converts Java request to Python format
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