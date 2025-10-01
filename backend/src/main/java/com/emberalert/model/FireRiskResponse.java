package com.emberalert.model;

import java.time.LocalDateTime;

/**
 * Response model - what we send back to users
 */
public class FireRiskResponse {
    
    private Double riskScore;
    private String riskCategory;
    private Double modelConfidence;
    
    private Double latitude;
    private Double longitude;
    
    private LocalDateTime timestamp;
    private String source;
    
    public FireRiskResponse() {}
    
    public FireRiskResponse(Double riskScore, String riskCategory, 
                           Double modelConfidence, Double latitude, 
                           Double longitude) {
        this.riskScore = riskScore;
        this.riskCategory = riskCategory;
        this.modelConfidence = modelConfidence;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = LocalDateTime.now();
        this.source = "ml-model";
    }
    
    public Double getRiskScore() {
        return riskScore;
    }
    
    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }
    
    public String getRiskCategory() {
        return riskCategory;
    }
    
    public void setRiskCategory(String riskCategory) {
        this.riskCategory = riskCategory;
    }
    
    public Double getModelConfidence() {
        return modelConfidence;
    }
    
    public void setModelConfidence(Double modelConfidence) {
        this.modelConfidence = modelConfidence;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
}