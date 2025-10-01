from flask import Flask, request, jsonify
import numpy as np
import joblib
from sklearn.ensemble import RandomForestRegressor
from sklearn.preprocessing import StandardScaler
import os
import logging
from datetime import datetime

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)

class WildfireRiskModel:
    def __init__(self):
        self.model = None
        self.scaler = StandardScaler()
        self.is_trained = False
        
    def create_synthetic_data(self, n_samples=10000):
        """Generate synthetic wildfire risk training data"""
        np.random.seed(42)
        
        # Features: temperature, humidity, wind_speed, vegetation_density, 
        # slope, elevation, days_since_rain, proximity_to_water
        features = np.random.rand(n_samples, 8)
        
        # Realistic feature scaling
        features[:, 0] = features[:, 0] * 40 + 10  # temp: 10-50°C
        features[:, 1] = features[:, 1] * 80 + 10  # humidity: 10-90%
        features[:, 2] = features[:, 2] * 30        # wind: 0-30 km/h
        features[:, 3] = features[:, 3] * 100       # vegetation: 0-100%
        features[:, 4] = features[:, 4] * 45        # slope: 0-45 degrees
        features[:, 5] = features[:, 5] * 3000      # elevation: 0-3000m
        features[:, 6] = features[:, 6] * 30        # days since rain: 0-30
        features[:, 7] = features[:, 7] * 10        # proximity to water: 0-10km
        
        # Risk calculation (higher temp, lower humidity, higher wind = higher risk)
        risk = (
            (features[:, 0] - 10) * 1.5 +  # temperature effect
            (100 - features[:, 1]) * 0.8 + # humidity effect (inverse)
            features[:, 2] * 1.2 +         # wind effect
            features[:, 3] * 0.5 +         # vegetation effect
            features[:, 4] * 0.3 +         # slope effect
            (features[:, 5] / 1000) * 0.4 + # elevation effect
            features[:, 6] * 1.8 +         # days since rain
            (10 - features[:, 7]) * 0.6    # proximity to water (inverse)
        )
        
        # Normalize to 0-100 scale with some noise
        risk = np.clip(risk / 2 + np.random.normal(0, 5, n_samples), 0, 100)
        
        return features, risk
    
    def train_model(self):
        """Train the wildfire risk prediction model"""
        logger.info("Training wildfire risk model...")
        
        # Generate training data
        X, y = self.create_synthetic_data()
        
        # Scale features
        X_scaled = self.scaler.fit_transform(X)
        
        # Train Random Forest model
        self.model = RandomForestRegressor(
            n_estimators=100,
            max_depth=15,
            random_state=42,
            n_jobs=-1
        )
        self.model.fit(X_scaled, y)
        self.is_trained = True
        
        logger.info("Model training completed")
        return self
    
    def predict_risk(self, features):
        """Predict wildfire risk for given features"""
        if not self.is_trained:
            raise ValueError("Model not trained")
        
        features_array = np.array(features).reshape(1, -1)
        features_scaled = self.scaler.transform(features_array)
        risk_score = self.model.predict(features_scaled)[0]
        
        return max(0, min(100, risk_score))  # Ensure 0-100 range
    
    def get_risk_category(self, risk_score):
        """Convert numeric risk score to category"""
        if risk_score < 20:
            return "Low"
        elif risk_score < 40:
            return "Moderate"
        elif risk_score < 60:
            return "High"
        elif risk_score < 80:
            return "Very High"
        else:
            return "Extreme"

# Initialize model
wildfire_model = WildfireRiskModel()

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        'status': 'healthy',
        'timestamp': datetime.now().isoformat(),
        'model_trained': wildfire_model.is_trained
    })

@app.route('/predict', methods=['POST'])
def predict_wildfire_risk():
    """Main prediction endpoint"""
    try:
        data = request.get_json()
        
        # Validate input
        required_fields = ['latitude', 'longitude', 'temperature', 'humidity', 
                          'wind_speed', 'vegetation_density']
        
        for field in required_fields:
            if field not in data:
                return jsonify({'error': f'Missing required field: {field}'}), 400
        
        # Extract features
        lat = float(data['latitude'])
        lon = float(data['longitude'])
        temp = float(data['temperature'])
        humidity = float(data['humidity'])
        wind_speed = float(data['wind_speed'])
        vegetation_density = float(data['vegetation_density'])
        
        # Optional features with defaults
        slope = float(data.get('slope', 15))
        elevation = float(data.get('elevation', 500))
        days_since_rain = float(data.get('days_since_rain', 7))
        proximity_to_water = float(data.get('proximity_to_water', 5))
        
        # Create feature vector
        features = [temp, humidity, wind_speed, vegetation_density, 
                   slope, elevation, days_since_rain, proximity_to_water]
        
        # Predict risk
        risk_score = wildfire_model.predict_risk(features)
        risk_category = wildfire_model.get_risk_category(risk_score)
        
        response = {
            'risk_score': round(risk_score, 2),
            'risk_category': risk_category,
            'coordinates': {'latitude': lat, 'longitude': lon},
            'timestamp': datetime.now().isoformat(),
            'model_confidence': 0.85,  # Placeholder - could calculate actual confidence
            'features_used': {
                'temperature': temp,
                'humidity': humidity,
                'wind_speed': wind_speed,
                'vegetation_density': vegetation_density,
                'slope': slope,
                'elevation': elevation,
                'days_since_rain': days_since_rain,
                'proximity_to_water': proximity_to_water
            }
        }
        
        logger.info(f"Prediction made: Risk={risk_score:.2f}, Category={risk_category}")
        return jsonify(response)
        
    except Exception as e:
        logger.error(f"Prediction error: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

@app.route('/batch_predict', methods=['POST'])
def batch_predict():
    """Batch prediction endpoint for multiple locations"""
    try:
        data = request.get_json()
        locations = data.get('locations', [])
        
        if not locations:
            return jsonify({'error': 'No locations provided'}), 400
        
        results = []
        for location in locations:
            # Use same logic as single prediction
            features = [
                location.get('temperature', 25),
                location.get('humidity', 50),
                location.get('wind_speed', 10),
                location.get('vegetation_density', 60),
                location.get('slope', 15),
                location.get('elevation', 500),
                location.get('days_since_rain', 7),
                location.get('proximity_to_water', 5)
            ]
            
            risk_score = wildfire_model.predict_risk(features)
            risk_category = wildfire_model.get_risk_category(risk_score)
            
            results.append({
                'latitude': location.get('latitude'),
                'longitude': location.get('longitude'),
                'risk_score': round(risk_score, 2),
                'risk_category': risk_category
            })
        
        return jsonify({
            'predictions': results,
            'total_locations': len(results),
            'timestamp': datetime.now().isoformat()
        })
        
    except Exception as e:
        logger.error(f"Batch prediction error: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

@app.route('/model/info', methods=['GET'])
def model_info():
    """Get model information"""
    return jsonify({
        'model_type': 'RandomForestRegressor',
        'features': [
            'temperature', 'humidity', 'wind_speed', 'vegetation_density',
            'slope', 'elevation', 'days_since_rain', 'proximity_to_water'
        ],
        'output_range': '0-100',
        'risk_categories': ['Low', 'Moderate', 'High', 'Very High', 'Extreme'],
        'trained': wildfire_model.is_trained,
        'version': '1.0.0'
    })

if __name__ == '__main__':
    # Train model on startup
    wildfire_model.train_model()
    
    # Save model for future use
    os.makedirs('model', exist_ok=True)
    joblib.dump(wildfire_model.model, 'model/wildfire_model.pkl')
    joblib.dump(wildfire_model.scaler, 'model/scaler.pkl')
    
    logger.info("EmberAlert ML Service starting...")
    app.run(host='0.0.0.0', port=5001, debug=True)