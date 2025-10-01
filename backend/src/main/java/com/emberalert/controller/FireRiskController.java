package com.emberalert.controller;

import com.emberalert.model.FireRiskRequest;
import com.emberalert.model.FireRiskResponse;
import com.emberalert.service.MLServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
}