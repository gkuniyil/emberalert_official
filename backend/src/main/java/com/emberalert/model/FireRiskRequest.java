package com.emberalert.model;

/**
 * Request model - what users send to our API
 */
public class FireRiskRequest {
    
    // Location
    private Double latitude;
    private Double longitude;
    
    // Weather conditions
    private Double temperature;
    private Double humidity;
    private Double windSpeed;
    
    // Environment
    private Double vegetationDensity;
    private Double slope;
    private Double elevation;
    private Integer daysSinceRain;
    private Double proximityToWater;
    
    // Empty constructor
    public FireRiskRequest() {}
    
    // Getters and Setters
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
    
    // Validation method
    public boolean isValid() {
        return latitude != null && longitude != null &&
               temperature != null && humidity != null &&
               windSpeed != null && vegetationDensity != null;
    }
    
    public String getValidationError() {
        if (latitude == null || longitude == null) {
            return "Latitude and longitude are required";
        }
        if (temperature == null) {
            return "Temperature is required";
        }
        if (humidity == null) {
            return "Humidity is required";
        }
        if (windSpeed == null) {
            return "Wind speed is required";
        }
        if (vegetationDensity == null) {
            return "Vegetation density is required";
        }
        return null;
    }
}