package com.emberalert.controller;

import com.emberalert.entity.FirePrediction;
import com.emberalert.model.FireRiskRequest;
import com.emberalert.model.FireRiskResponse;
import com.emberalert.service.MLServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;  // ADD THIS IMPORT!
import java.util.Map;

/**
 * REST Controller for fire risk prediction endpoints
 * This handles all incoming HTTP requests from users
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FireRiskController {
    
    @Autowired
    private MLServiceClient mlServiceClient;
    
    /**
     * Health check endpoint
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "emberalert-backend");
        response.put("ml_service_healthy", mlServiceClient.isMLServiceHealthy());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Fire risk prediction endpoint
     * POST /api/predict
     */
    @PostMapping("/predict")
    public ResponseEntity<?> predictFireRisk(@RequestBody FireRiskRequest request) {
        try {
            System.out.println("🔥 Received fire risk prediction request");
            System.out.println("📍 Location: " + request.getLatitude() + ", " + request.getLongitude());
            
            // Validate request
            if (!request.isValid()) {
                String error = request.getValidationError();
                System.err.println("❌ Validation error: " + error);
                return ResponseEntity.badRequest().body(Map.of("error", error));
            }
            
            // Call ML service
            Map<String, Object> mlResponse = mlServiceClient.predictFireRisk(request);
            
            // Extract data from ML response
            Double riskScore = ((Number) mlResponse.get("risk_score")).doubleValue();
            String riskCategory = (String) mlResponse.get("risk_category");
            Double confidence = ((Number) mlResponse.get("model_confidence")).doubleValue();
            
            // Create response
            FireRiskResponse response = new FireRiskResponse(
                riskScore,
                riskCategory,
                confidence,
                request.getLatitude(),
                request.getLongitude()
            );
            
            System.out.println("✅ Prediction successful: " + riskScore + " - " + riskCategory);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error processing request: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get fire risk prediction: " + e.getMessage()));
        }
    }  // FIXED: Proper closing brace for predictFireRisk method

    /**
     * Get all prediction history
     * GET /api/predictions/history
     */
    @GetMapping("/predictions/history")
    public ResponseEntity<Map<String, Object>> getPredictionHistory() {
        try {
            List<FirePrediction> predictions = mlServiceClient.getAllPredictions();
            
            Map<String, Object> response = new HashMap<>();
            response.put("total_predictions", predictions.size());
            response.put("predictions", predictions);
            
            System.out.println("📊 Retrieved " + predictions.size() + " predictions from history");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch prediction history"));
        }
    }
    
    /**
     * Get predictions near a location
     * GET /api/predictions/location?lat=34.05&lon=-118.24
     */
    @GetMapping("/predictions/location")
    public ResponseEntity<?> getPredictionsByLocation(
            @RequestParam Double lat,
            @RequestParam Double lon) {
        try {
            List<FirePrediction> predictions = mlServiceClient.getPredictionsNearLocation(lat, lon);
            
            Map<String, Object> response = new HashMap<>();
            response.put("location", Map.of("latitude", lat, "longitude", lon));
            response.put("predictions_found", predictions.size());
            response.put("predictions", predictions);
            
            System.out.println("📍 Found " + predictions.size() + " predictions near " + lat + ", " + lon);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching location predictions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch predictions for location"));
        }
    }
    
  /**
     * Get high-risk predictions
     * GET /api/predictions/high-risk?threshold=70
     */
    @GetMapping("/predictions/high-risk")
    public ResponseEntity<?> getHighRiskPredictions(
            @RequestParam(defaultValue = "70.0") Double threshold) {
        try {
            List<FirePrediction> predictions = mlServiceClient.getHighRiskPredictions(threshold);
            
            Map<String, Object> response = new HashMap<>();
            response.put("threshold", threshold);
            response.put("high_risk_count", predictions.size());
            response.put("predictions", predictions);
            
            System.out.println("🚨 Found " + predictions.size() + " high-risk predictions (>" + threshold + ")");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching high-risk predictions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch high-risk predictions"));
        }
    }
    
    /**
     * Get prediction statistics
     * GET /api/predictions/stats
     */
    @GetMapping("/predictions/stats")
    public ResponseEntity<?> getPredictionStats() {
        try {
            List<FirePrediction> allPredictions = mlServiceClient.getAllPredictions();
            
            if (allPredictions.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "message", "No predictions yet",
                    "total_predictions", 0
                ));
            }
            
            // Calculate statistics
            double avgRiskScore = allPredictions.stream()
                    .mapToDouble(FirePrediction::getRiskScore)
                    .average()
                    .orElse(0.0);
            
            double maxRiskScore = allPredictions.stream()
                    .mapToDouble(FirePrediction::getRiskScore)
                    .max()
                    .orElse(0.0);
            
            double minRiskScore = allPredictions.stream()
                    .mapToDouble(FirePrediction::getRiskScore)
                    .min()
                    .orElse(0.0);
            
            // Count by category
            Map<String, Long> categoryCount = new HashMap<>();
            for (FirePrediction p : allPredictions) {
                categoryCount.put(
                    p.getRiskCategory(), 
                    categoryCount.getOrDefault(p.getRiskCategory(), 0L) + 1
                );
            }
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("total_predictions", allPredictions.size());
            stats.put("average_risk_score", Math.round(avgRiskScore * 100.0) / 100.0);
            stats.put("max_risk_score", maxRiskScore);
            stats.put("min_risk_score", minRiskScore);
            stats.put("predictions_by_category", categoryCount);
            
            System.out.println("📊 Statistics calculated for " + allPredictions.size() + " predictions");
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            System.err.println("❌ Error calculating stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to calculate statistics"));
        }
    }
    
    /**
     * Test endpoint to verify controller is working
     * GET /api/test
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "EmberAlert Backend API is working!");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
}  // Final closing brace for class