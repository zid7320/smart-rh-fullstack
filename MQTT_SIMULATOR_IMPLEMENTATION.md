# MQTT Device Simulator Implementation Plan

**Status:** Ready to implement  
**Technology:** Python 3.10+  
**Timeline:** 6-8 hours  
**Purpose:** Generate realistic attendance events for testing Module 10 & 19

---

## 🎯 Goals

✅ Simulate multiple IoT devices publishing attendance events  
✅ Test real-time attendance feed (Module 10)  
✅ Generate data for analytics (Module 19)  
✅ Test fraud detection scenarios  
✅ Load testing capability (10-200 devices)  

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────┐
│   MQTT Device Simulator (Python)            │
├─────────────────────────────────────────────┤
│                                             │
│  DeviceSimulator (Base Class)               │
│  ├── CameraDevice                           │
│  ├── DoorSensorDevice                       │
│  └── KioskDevice                            │
│                                             │
│  Event Generator                            │
│  ├── Normal Events (95%)                    │
│  │   └─ Confidence: 85-99%                  │
│  ├── Low Confidence (4%)                    │
│  │   └─ Confidence: 60-85%                  │
│  └── Fraud Events (1%)                      │
│      └─ Confidence: <60%, Fraud Reason      │
│                                             │
│  MQTT Publisher                             │
│  └─ Publishes to /smartrh/{deviceId}/*     │
│                                             │
└─────────────────────────────────────────────┘
     ↓
┌─────────────────────────────────────────────┐
│   Mosquitto MQTT Broker (localhost:1883)    │
└─────────────────────────────────────────────┘
     ↓
┌─────────────────────────────────────────────┐
│   Spring Boot Backend                       │
│   → Receives MQTT messages                  │
│   → Creates AttendanceEvent records         │
│   → Broadcasts to WebSocket                 │
└─────────────────────────────────────────────┘
     ↓
┌─────────────────────────────────────────────┐
│   Frontend Attendance Dashboard (Module 10) │
│   Frontend Analytics (Module 19)            │
└─────────────────────────────────────────────┘
```

---

## 📋 Features

### 1. Device Types

```python
# CameraDevice - Facial recognition camera
- Device Type: CAMERA
- Check-in/out events
- Base64 encoded photo
- Confidence score (85-99%)
- Optional fraud indicators (1%)

# DoorSensorDevice - Entry/exit sensors
- Device Type: DOOR_SENSOR
- Simple check-in/out
- Lower confidence (70-95%)
- Location-based

# KioskDevice - Self-service check-in kiosk
- Device Type: KIOSK
- User-initiated check-in
- High confidence (95-99%)
- Manual input
```

### 2. Event Types

```
CHECK_IN   - Employee checks in
CHECK_OUT  - Employee checks out
SUSPICIOUS - Low confidence or fraud detected
```

### 3. Fraud Scenarios (1% of events)

```
MASK_DETECTED       - Employee wearing mask
SPOOFING_ATTEMPT    - Possible face spoofing
LOW_CONFIDENCE      - Confidence < 70%
MULTIPLE_FACES      - Multiple faces detected
FACE_NOT_FOUND      - No face detected
```

### 4. Configuration

```python
# devices/mqtt_simulator_config.json
{
  "mosquitto": {
    "broker": "localhost",
    "port": 1883,
    "username": "simulator",
    "password": "simulator123"
  },
  "devices": {
    "count": 10,                    # Number of devices
    "types": {
      "CAMERA": 6,
      "DOOR_SENSOR": 3,
      "KIOSK": 1
    }
  },
  "events": {
    "employees_per_device": 50,     # Max employees per device
    "event_frequency": 60,          # Seconds between events
    "normal_probability": 0.95,     # 95% normal events
    "low_confidence_probability": 0.04,  # 4% low confidence
    "fraud_probability": 0.01       # 1% fraud events
  },
  "simulation": {
    "duration": 3600,               # Run for 1 hour
    "auto_increment": true,         # Auto-generate employees
    "verbose": true                 # Detailed logging
  }
}
```

---

## 💻 Implementation Files

### File 1: Device Base Class

**File:** `devices/mqtt_simulator.py`

```python
import json
import time
import random
import base64
import paho.mqtt.client as mqtt
from datetime import datetime, timedelta
from typing import List, Dict, Optional
from dataclasses import dataclass, asdict
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# ============ Data Models ============

@dataclass
class AttendanceEvent:
    """MQTT Attendance Event Payload"""
    deviceId: str
    employeeId: int
    employeeName: str
    eventType: str  # CHECK_IN, CHECK_OUT, SUSPICIOUS
    confidence: float  # 0.0-1.0
    fraudReason: Optional[str]  # MASK_DETECTED, SPOOFING_ATTEMPT, etc.
    timestamp: str  # ISO format
    photo: Optional[str]  # Base64 encoded photo data
    
    def to_json(self) -> str:
        return json.dumps(asdict(self))

# ============ MQTT Simulator ============

class MQTTSimulator:
    """Simulates multiple IoT devices publishing attendance events"""
    
    def __init__(self, config_path: str = 'devices/mqtt_simulator_config.json'):
        self.config = self._load_config(config_path)
        self.client = mqtt.Client()
        self.devices: List[SimulatedDevice] = []
        self._setup_mqtt()
        
    def _load_config(self, path: str) -> Dict:
        try:
            with open(path, 'r') as f:
                return json.load(f)
        except FileNotFoundError:
            logger.warning(f"Config not found: {path}, using defaults")
            return self._default_config()
    
    def _default_config(self) -> Dict:
        return {
            "mosquitto": {
                "broker": "localhost",
                "port": 1883,
                "username": "",
                "password": ""
            },
            "devices": {
                "count": 10,
                "types": {"CAMERA": 6, "DOOR_SENSOR": 3, "KIOSK": 1}
            },
            "events": {
                "employees_per_device": 50,
                "event_frequency": 60,
                "normal_probability": 0.95,
                "low_confidence_probability": 0.04,
                "fraud_probability": 0.01
            },
            "simulation": {
                "duration": 3600,
                "auto_increment": True,
                "verbose": True
            }
        }
    
    def _setup_mqtt(self):
        """Setup MQTT client and callbacks"""
        cfg = self.config['mosquitto']
        
        if cfg.get('username'):
            self.client.username_pw_set(cfg['username'], cfg['password'])
        
        self.client.on_connect = self._on_connect
        self.client.on_disconnect = self._on_disconnect
        self.client.on_publish = self._on_publish
        
        logger.info(f"Connecting to MQTT broker: {cfg['broker']}:{cfg['port']}")
        self.client.connect(cfg['broker'], cfg['port'], 60)
    
    def _on_connect(self, client, userdata, flags, rc):
        if rc == 0:
            logger.info("✅ Connected to MQTT broker")
        else:
            logger.error(f"❌ Connection failed with code {rc}")
    
    def _on_disconnect(self, client, userdata, rc):
        if rc != 0:
            logger.warning(f"Unexpected disconnection: {rc}")
    
    def _on_publish(self, client, userdata, mid):
        if self.config['simulation']['verbose']:
            logger.debug(f"Message published: {mid}")
    
    def create_devices(self):
        """Create simulated IoT devices"""
        cfg = self.config['devices']
        device_count = 0
        
        for device_type, count in cfg['types'].items():
            for i in range(count):
                device_id = f"{device_type.lower()}-{device_count:03d}"
                self.devices.append(
                    SimulatedDevice(device_id, device_type, self.client, self.config)
                )
                device_count += 1
        
        logger.info(f"✅ Created {len(self.devices)} simulated devices")
        for device in self.devices:
            logger.info(f"   - {device.device_id} ({device.device_type})")
    
    def start(self):
        """Start MQTT client and begin simulation"""
        self.client.loop_start()
        time.sleep(1)  # Wait for connection
        
        self.create_devices()
        
        logger.info(f"🚀 Starting simulation for {self.config['simulation']['duration']}s")
        start_time = time.time()
        event_count = 0
        
        try:
            while (time.time() - start_time) < self.config['simulation']['duration']:
                # Each device publishes events
                for device in self.devices:
                    if random.random() < 0.5:  # 50% chance to publish per interval
                        event = device.generate_event()
                        device.publish_event(event)
                        event_count += 1
                
                time.sleep(self.config['events']['event_frequency'])
        
        except KeyboardInterrupt:
            logger.info("⏹️  Simulation stopped by user")
        finally:
            logger.info(f"📊 Simulation complete: {event_count} events published")
            self.client.loop_stop()
            self.client.disconnect()

# ============ Simulated Device ============

class SimulatedDevice:
    """Represents a single IoT device"""
    
    # Sample employee names for realistic testing
    EMPLOYEE_NAMES = [
        "Ahmed Hassan", "Fatima Mohamed", "Ali Ibrahim", "Leila Ahmed",
        "Ibrahim Hassan", "Aisha Mohamed", "Omar Ali", "Noor Ibrahim",
        "Hassan Ahmed", "Sara Ibrahim", "Karim Mohamed", "Mariam Ahmed"
    ]
    
    FRAUD_REASONS = [
        "MASK_DETECTED", "SPOOFING_ATTEMPT", "MULTIPLE_FACES",
        "FACE_NOT_FOUND", "LOW_CONFIDENCE"
    ]
    
    def __init__(self, device_id: str, device_type: str, mqtt_client, config: Dict):
        self.device_id = device_id
        self.device_type = device_type
        self.client = mqtt_client
        self.config = config
        self.last_employee_id = 1000
        self.employee_states = {}  # Track check-in/out state
    
    def generate_event(self) -> AttendanceEvent:
        """Generate a realistic attendance event"""
        event_cfg = self.config['events']
        rand = random.random()
        
        # Determine event type
        if rand < event_cfg['normal_probability']:
            return self._generate_normal_event()
        elif rand < event_cfg['normal_probability'] + event_cfg['low_confidence_probability']:
            return self._generate_low_confidence_event()
        else:
            return self._generate_fraud_event()
    
    def _generate_normal_event(self) -> AttendanceEvent:
        """Generate normal (legitimate) event"""
        employee_id = self._get_employee_id()
        employee_name = self._get_employee_name()
        event_type = self._get_event_type(employee_id)
        
        return AttendanceEvent(
            deviceId=self.device_id,
            employeeId=employee_id,
            employeeName=employee_name,
            eventType=event_type,
            confidence=round(random.uniform(0.85, 0.99), 3),
            fraudReason=None,
            timestamp=datetime.now().isoformat(),
            photo=self._generate_dummy_photo()
        )
    
    def _generate_low_confidence_event(self) -> AttendanceEvent:
        """Generate low confidence event"""
        employee_id = self._get_employee_id()
        employee_name = self._get_employee_name()
        event_type = self._get_event_type(employee_id)
        
        return AttendanceEvent(
            deviceId=self.device_id,
            employeeId=employee_id,
            employeeName=employee_name,
            eventType="CHECK_IN",
            confidence=round(random.uniform(0.60, 0.85), 3),
            fraudReason=None,  # Low confidence but not fraud
            timestamp=datetime.now().isoformat(),
            photo=self._generate_dummy_photo()
        )
    
    def _generate_fraud_event(self) -> AttendanceEvent:
        """Generate fraud (suspicious) event"""
        employee_id = self._get_employee_id()
        employee_name = self._get_employee_name()
        fraud_reason = random.choice(self.FRAUD_REASONS)
        
        return AttendanceEvent(
            deviceId=self.device_id,
            employeeId=employee_id,
            employeeName=employee_name,
            eventType="SUSPICIOUS",
            confidence=round(random.uniform(0.30, 0.70), 3),
            fraudReason=fraud_reason,
            timestamp=datetime.now().isoformat(),
            photo=self._generate_dummy_photo()
        )
    
    def _get_employee_id(self) -> int:
        """Get random employee ID"""
        return random.randint(1, self.config['events']['employees_per_device'])
    
    def _get_employee_name(self) -> str:
        """Get random employee name"""
        return random.choice(self.EMPLOYEE_NAMES)
    
    def _get_event_type(self, employee_id: int) -> str:
        """Determine if CHECK_IN or CHECK_OUT"""
        # Toggle between check-in and check-out
        current_state = self.employee_states.get(employee_id, "OUT")
        new_state = "IN" if current_state == "OUT" else "OUT"
        self.employee_states[employee_id] = new_state
        
        return "CHECK_IN" if new_state == "IN" else "CHECK_OUT"
    
    def _generate_dummy_photo(self) -> str:
        """Generate a dummy Base64 photo (1x1 transparent PNG)"""
        # 1x1 transparent PNG
        png_data = (
            b'\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01'
            b'\x08\x06\x00\x00\x00\x1f\x15\xc4\x89\x00\x00\x00\nIDATx\x9cc\x00\x01'
            b'\x00\x00\x05\x00\x01\r\n-\xb4\x00\x00\x00\x00IEND\xaeB`\x82'
        )
        return base64.b64encode(png_data).decode()
    
    def publish_event(self, event: AttendanceEvent):
        """Publish event to MQTT broker"""
        topic = f"smartrh/{self.device_id}/attendance"
        payload = event.to_json()
        
        self.client.publish(topic, payload, qos=1)
        
        log_level = "⚠️ " if event.fraudReason else "✅"
        logger.info(
            f"{log_level} [{self.device_id}] {event.employeeName} "
            f"({event.eventType}, {event.confidence:.2f}) "
            f"{event.fraudReason or ''}"
        )

# ============ Main ============

if __name__ == "__main__":
    simulator = MQTTSimulator()
    simulator.start()
```

### File 2: Configuration File

**File:** `devices/mqtt_simulator_config.json`

```json
{
  "mosquitto": {
    "broker": "localhost",
    "port": 1883,
    "username": "",
    "password": ""
  },
  "devices": {
    "count": 10,
    "types": {
      "CAMERA": 6,
      "DOOR_SENSOR": 3,
      "KIOSK": 1
    }
  },
  "events": {
    "employees_per_device": 50,
    "event_frequency": 60,
    "normal_probability": 0.95,
    "low_confidence_probability": 0.04,
    "fraud_probability": 0.01
  },
  "simulation": {
    "duration": 3600,
    "auto_increment": true,
    "verbose": true
  }
}
```

### File 3: Requirements File

**File:** `devices/requirements.txt`

```
paho-mqtt==1.6.1
python-dotenv==0.21.0
```

### File 4: Docker Compose Update (Optional)

Add to `docker-compose.yml` for convenience:

```yaml
simulator:
  image: python:3.11-slim
  working_dir: /app
  volumes:
    - ./devices:/app/devices
  command: bash -c "pip install -r devices/requirements.txt && python devices/mqtt_simulator.py"
  depends_on:
    - mqtt
  environment:
    - PYTHONUNBUFFERED=1
  # Uncomment to run with Docker:
  # profiles: ["simulator"]
```

---

## 🚀 Usage

### Option 1: Run Standalone (Recommended for Testing)

```bash
# Install dependencies
cd devices
pip install -r requirements.txt

# Run simulator
python mqtt_simulator.py

# Expected output:
# ℹ️  Connecting to MQTT broker: localhost:1883
# ✅ Connected to MQTT broker
# ✅ Created 10 simulated devices
#    - camera-000 (CAMERA)
#    - camera-001 (CAMERA)
#    - ...
# 🚀 Starting simulation for 3600s
# ✅ [camera-000] Ahmed Hassan (CHECK_IN, 0.94)
# ✅ [door-sensor-006] Fatima Mohamed (CHECK_OUT, 0.88)
# ⚠️ [camera-002] Ali Ibrahim (SUSPICIOUS, 0.65) MASK_DETECTED
# ...
```

### Option 2: Run with Docker

```bash
docker compose --profile simulator up simulator
```

### Option 3: Run in Background (Production)

```bash
# Terminal 1: Start backend
docker compose up

# Terminal 2: Start simulator
cd devices && python mqtt_simulator.py &

# Now both are running. Verify:
# - Frontend: http://localhost:4200
#   → Attendance Dashboard shows live events
# - Analytics: http://localhost:4200/analytics
#   → Charts update with incoming data
```

---

## 🧪 Testing Scenarios

### Scenario 1: Normal Operations (5 min)
```bash
# Default config (95% legitimate events)
python mqtt_simulator.py
# Monitor: Frontend attendance feed fills with green events
```

### Scenario 2: Fraud Detection (10 min)
Edit `mqtt_simulator_config.json`:
```json
"events": {
  "normal_probability": 0.70,
  "fraud_probability": 0.30
}
```
Run and monitor fraud alerts in HR dashboard.

### Scenario 3: Load Testing (30 min)
```json
"devices": {
  "count": 50,
  "types": {
    "CAMERA": 30,
    "DOOR_SENSOR": 15,
    "KIOSK": 5
  }
}
```
Run and monitor backend performance.

---

## 📊 What Gets Generated

### MQTT Message Format

```json
{
  "deviceId": "camera-000",
  "employeeId": 1025,
  "employeeName": "Ahmed Hassan",
  "eventType": "CHECK_IN",
  "confidence": 0.945,
  "fraudReason": null,
  "timestamp": "2026-04-20T14:30:45.123456",
  "photo": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
}
```

### What Appears in Frontend

1. **Module 10 (Attendance Dashboard)**
   - Real-time feed updates every 60 seconds
   - Events show employee name, timestamp, confidence
   - Suspicious events highlighted in orange/red
   - Connection badge shows "CONNECTED"

2. **Module 19 (Analytics)**
   - Attendance trends line chart updates
   - Fraud metrics pie chart shows distribution
   - Department stats (if multiple employees)
   - Peak hours heatmap fills with data

---

## 🔧 Configuration Presets

### Low Load (Testing)
```json
{
  "devices": { "count": 3, "types": {"CAMERA": 2, "DOOR_SENSOR": 1} },
  "events": { "event_frequency": 30 },
  "simulation": { "duration": 600 }
}
```

### Normal Load (Demo)
```json
{
  "devices": { "count": 10 },
  "events": { "event_frequency": 60 },
  "simulation": { "duration": 3600 }
}
```

### Heavy Load (Stress Test)
```json
{
  "devices": { "count": 100 },
  "events": { "event_frequency": 5 },
  "simulation": { "duration": 3600 }
}
```

---

## ✅ Implementation Checklist

- [ ] Create `devices/mqtt_simulator.py`
- [ ] Create `devices/mqtt_simulator_config.json`
- [ ] Create `devices/requirements.txt`
- [ ] Test connection to Mosquitto broker
- [ ] Verify MQTT messages arrive (use MQTT client)
- [ ] Verify backend receives messages (check logs)
- [ ] Verify frontend updates (watch attendance dashboard)
- [ ] Test fraud scenario generation
- [ ] Test load scenarios (10, 50, 100 devices)
- [ ] Create `devices/README.md` with usage guide

---

## ⏱️ Timeline

| Task | Hours | Status |
|------|-------|--------|
| Setup & config | 1 | Ready |
| Core simulator | 2 | Ready |
| Device simulation | 2 | Ready |
| Testing & docs | 1 | Ready |
| **Total** | **6** | **Ready** |

---

**Ready? I'll create all files and integrate with existing backend next.**
