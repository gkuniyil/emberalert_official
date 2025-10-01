import requests
import json
import time

# Configuration
BASE_URL = "http://localhost:5001"

def test_health_check():
    """Test the health endpoint"""
    print("🏥 Testing health check...")
    response = requests.get(f"{BASE_URL}/health")
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
    print("-" * 50)

def test_single_prediction():
    """Test single wildfire risk prediction"""
    print("🔥 Testing single prediction...")
    
    # High risk scenario: Hot, dry, windy conditions
    high_risk_data = {
        "latitude": 34.0522,
        "longitude": -118.2437,
        "temperature": 42,      # Very hot
        "humidity": 15,         # Very dry
        "wind_speed": 25,       # High wind
        "vegetation_density": 80, # Dense vegetation
        "slope": 30,            # Steep slope
        "elevation": 1200,      # Higher elevation
        "days_since_rain": 21,  # Long drought
        "proximity_to_water": 8  # Far from water
    }
    
    response = requests.post(f"{BASE_URL}/predict", json=high_risk_data)
    print(f"🚨 High Risk Scenario - Status: {response.status_code}")
    if response.status_code == 200:
        result = response.json()
        print(f"🔥 Risk Score: {result['risk_score']}/100")
        print(f"📊 Category: {result['risk_category']}")
        print(f"📍 Location: {result['coordinates']['latitude']}, {result['coordinates']['longitude']}")
    else:
        print(f"❌ Error: {response.text}")
    print("-" * 50)
    
    # Low risk scenario: Cool, humid, calm conditions
    low_risk_data = {
        "latitude": 47.6062,
        "longitude": -122.3321,
        "temperature": 18,      # Cool
        "humidity": 75,         # Humid
        "wind_speed": 3,        # Light wind
        "vegetation_density": 30, # Sparse vegetation
        "slope": 5,             # Flat
        "elevation": 200,       # Low elevation
        "days_since_rain": 1,   # Recent rain
        "proximity_to_water": 1  # Near water
    }
    
    response = requests.post(f"{BASE_URL}/predict", json=low_risk_data)
    print(f"🌧️ Low Risk Scenario - Status: {response.status_code}")
    if response.status_code == 200:
        result = response.json()
        print(f"💧 Risk Score: {result['risk_score']}/100")
        print(f"📊 Category: {result['risk_category']}")
        print(f"📍 Location: {result['coordinates']['latitude']}, {result['coordinates']['longitude']}")
    else:
        print(f"❌ Error: {response.text}")
    print("-" * 50)

def test_batch_prediction():
    """Test batch prediction endpoint"""
    print("🌍 Testing batch prediction...")
    
    batch_data = {
        "locations": [
            {
                "latitude": 34.0522, "longitude": -118.2437,
                "temperature": 38, "humidity": 20, "wind_speed": 20,
                "vegetation_density": 70
            },
            {
                "latitude": 37.7749, "longitude": -122.4194,
                "temperature": 22, "humidity": 65, "wind_speed": 8,
                "vegetation_density": 40
            },
            {
                "latitude": 40.7128, "longitude": -74.0060,
                "temperature": 28, "humidity": 55, "wind_speed": 12,
                "vegetation_density": 25
            }
        ]
    }
    
    response = requests.post(f"{BASE_URL}/batch_predict", json=batch_data)
    print(f"Batch Prediction - Status: {response.status_code}")
    if response.status_code == 200:
        result = response.json()
        print(f"📍 Processed {result['total_locations']} locations:")
        for prediction in result['predictions']:
            print(f"  🔥 {prediction['latitude']}, {prediction['longitude']}: {prediction['risk_score']}/100 ({prediction['risk_category']})")
    else:
        print(f"❌ Error: {response.text}")
    print("-" * 50)

def test_model_info():
    """Test model info endpoint"""
    print("🧠 Testing model info...")
    response = requests.get(f"{BASE_URL}/model/info")
    print(f"Status: {response.status_code}")
    if response.status_code == 200:
        info = response.json()
        print(f"Model Type: {info['model_type']}")
        print(f"Features: {', '.join(info['features'])}")
        print(f"Risk Categories: {', '.join(info['risk_categories'])}")
        print(f"Model Trained: {info['trained']}")
    else:
        print(f"❌ Error: {response.text}")
    print("-" * 50)

def test_performance():
    """Test API performance"""
    print("⚡ Testing API performance...")
    
    test_data = {
        "latitude": 34.0522,
        "longitude": -118.2437,
        "temperature": 35,
        "humidity": 25,
        "wind_speed": 15,
        "vegetation_density": 60
    }
    
    # Test response time
    num_requests = 10
    total_time = 0
    successful_requests = 0
    
    for i in range(num_requests):
        start_time = time.time()
        try:
            response = requests.post(f"{BASE_URL}/predict", json=test_data, timeout=5)
            end_time = time.time()
            
            if response.status_code == 200:
                total_time += (end_time - start_time)
                successful_requests += 1
            else:
                print(f"Request {i+1} failed: {response.status_code}")
        except requests.exceptions.RequestException as e:
            print(f"Request {i+1} failed: {str(e)}")
    
    if successful_requests > 0:
        avg_time = (total_time / successful_requests) * 1000  # Convert to ms
        print(f"⚡ Average response time: {avg_time:.2f}ms")
        print(f"🎯 Target: < 200ms {'✅' if avg_time < 200 else '❌'}")
        print(f"📊 Success rate: {successful_requests}/{num_requests}")
    else:
        print("❌ All requests failed!")
    print("-" * 50)

if __name__ == "__main__":
    print("🔥 EmberAlert ML Service - API Tests")
    print("=" * 50)
    
    try:
        test_health_check()
        test_single_prediction()
        test_batch_prediction()
        test_model_info()
        test_performance()
        print("🎉 All tests completed successfully!")
        
    except requests.exceptions.ConnectionError:
        print("❌ Could not connect to ML service.")
        print("💡 Make sure the service is running: python app.py")
    except Exception as e:
        print(f"❌ Test failed: {str(e)}")