package com.emberalert.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity - represents a fire_predictions table in database
 * Each object = one row in the table
 */
@Entity
@Table(name = "fire_predictions")
public class FirePrediction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Location
    @Column(nullable = false)
    private Double latitude;
    
    @Column(nullable = false)
    private Double longitude;
    
    // Weather conditions (input)
    @Column(nullable = false)
    private Double temperature;
    
    @Column(nullable = false)
    private Double humidity;
    
    @Column(name = "wind_speed", nullable = false)
    private Double windSpeed;
    
    @Column(name = "vegetation_density", nullable = false)
    private Double vegetationDensity;
    
    private Double slope;
    private Double elevation;
    
    @Column(name = "days_since_rain")
    private Integer daysSinceRain;
    
    @Column(name = "proximity_to_water")
    private Double proximityToWater;
    
    // Prediction results (output)
    @Column(name = "risk_score", nullable = false)
    private Double riskScore;
    
    @Column(name = "risk_category", nullable = false)
    private String riskCategory;
    
    @Column(name = "model_confidence")
    private Double modelConfidence;
    
    // Metadata
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    private String source;
    
    // Constructors
    public FirePrediction() {
        this.createdAt = LocalDateTime.now();
        this.source = "ml-model";
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public Double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    
    public Double getHumidity() {
        return humidity;
    }
    
    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }
    
    public Double getWindSpeed() {
        return windSpeed;
    }
    
    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }
    
    public Double getVegetationDensity() {
        return vegetationDensity;
    }
    
    public void setVegetationDensity(Double vegetationDensity) {
        this.vegetationDensity = vegetationDensity;
    }
    
    public Double getSlope() {
        return slope;
    }
    
    public void setSlope(Double slope) {
        this.slope = slope;
    }
    
    public Double getElevation() {
        return elevation;
    }
    
    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }
    
    public Integer getDaysSinceRain() {
        return daysSinceRain;
    }
    
    public void setDaysSinceRain(Integer daysSinceRain) {
        this.daysSinceRain = daysSinceRain;
    }
    
    public Double getProximityToWater() {
        return proximityToWater;
    }
    
    public void setProximityToWater(Double proximityToWater) {
        this.proximityToWater = proximityToWater;
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
}