# MQTT Device Integration Guide — SMART RH 4.0

## Overview

This guide explains how to integrate IoT devices (cameras, sensors, readers) with the SMART RH 4.0 backend via MQTT (Message Queuing Telemetry Transport).

## Architecture

```
┌─────────────┐
│   Devices   │ (Cameras, Sensors, Readers)
│  (ESP32)    │
└──────┬──────┘
       │ MQTT/TLS
       ▼
┌─────────────────────────────┐
│  Mosquitto MQTT Broker      │
│  (Port 8883 - TLS/SSL)      │
│  (Username/Password Auth)   │
└──────┬──────────────────────┘
       │ Spring Integration
       ▼
┌─────────────────────────────┐
│  Spring Boot Backend        │
│  (AttendanceEventService)   │
└──────┬──────────────────────┘
       │ WebSocket
       ▼
┌─────────────────────────────┐
│  Angular Dashboard          │
│  (Real-time updates)        │
└─────────────────────────────┘
```

## Prerequisites

Before connecting devices, ensure:

1. **Mosquitto broker is running**

   ```bash
   docker compose up mqtt
   ```

2. **SSL certificates are generated**

   ```bash
   cd infra/mosquitto
   bash generate-mqtt-certs.sh
   ```

3. **Backend server is running**
   ```bash
   docker compose up backend
   ```

## Device Registration

### Step 1: Register Device in SMART RH

First, register the device via REST API:

```bash
curl -X POST http://localhost:8081/api/devices \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -d '{
    "name": "Camera - Entrance",
    "deviceType": "CAMERA",
    "location": "Building A, Ground Floor",
    "mqttUsername": "device_camera_01",
    "mqttPassword": "{PASSWORD_FROM_PASSWD_FILE}"
  }'
```

### Step 2: Get MQTT Credentials

The device credentials are automatically generated in `/infra/mosquitto/passwd`.

For camera_01:

- **Username**: `device_camera_01`
- **Password**: Generate using the setup script (already in passwd file)

### Step 3: Configure Device

Update your device firmware with MQTT connection parameters:

```c
// Arduino/ESP32 code example
#include <PubSubClient.h>
#include <WiFi.h>

const char* ssid = "WIFI_SSID";
const char* password = "WIFI_PASSWORD";
const char* mqtt_broker = "mosquitto.smartrh.local";  // or IP address
const int mqtt_port = 8883;                           // TLS port
const char* mqtt_username = "device_camera_01";
const char* mqtt_password = "PASSWORD_HERE";
const char* client_id = "camera_01";

// For TLS/SSL, use rootCA certificate
const char* rootCA = R"(
-----BEGIN CERTIFICATE-----
MIIDXTCCAkWgAwIBAgIJAKZJSH5/QKJBMA0GCSqGSIb3DQEBCwUAMEUxCzAJBgNV
... (paste contents of certs/ca.crt here)
-----END CERTIFICATE-----
)";

WiFiClientSecure espClient;
PubSubClient client(espClient);

void setup() {
  Serial.begin(115200);
  WiFi.begin(ssid, password);

  // Set root CA certificate for TLS
  espClient.setCACert(rootCA);

  client.setServer(mqtt_broker, mqtt_port);
  client.setCallback(callback);
}

void loop() {
  if (!client.connected()) {
    reconnect();
  }
  client.loop();
}

void reconnect() {
  if (client.connect(client_id, mqtt_username, mqtt_password)) {
    Serial.println("MQTT connected");
    // Subscribe to commands
    client.subscribe("smartrh/commands/camera_01/+");
  }
}
```

## MQTT Topics & Message Format

### Facial Recognition Camera

**Publish Topics** (Device → Backend):

1. **Face Detection Event**

   ```
   Topic: smartrh/devices/camera_01/face/detection
   Payload: {
     "timestamp": "2026-04-20T10:30:45Z",
     "faceCount": 1,
     "confidence": 0.98,
     "location": "frame_center",
     "metadata": {
       "exposure": 100,
       "lighting": "good"
     }
   }
   ```

2. **Face Recognition Result**

   ```
   Topic: smartrh/devices/camera_01/face/recognition
   Payload: {
     "timestamp": "2026-04-20T10:30:46Z",
     "employeeId": 42,
     "confidence": 0.98,
     "eventType": "IN",
     "faceEmbedding": "[0.123, 0.456, ...]",
     "photoData": "BASE64_ENCODED_IMAGE_OR_PATH",
     "fraudFlags": [],
     "maskDetected": false,
     "livenessScore": 0.95,
     "metadata": {
       "processingTimeMs": 250,
       "model": "facenet_mobilenet",
       "modelVersion": "1.2.0"
     }
   }
   ```

3. **Health Heartbeat** (every 30 seconds)

   ```
   Topic: smartrh/devices/camera_01/health/heartbeat
   Payload: {
     "timestamp": "2026-04-20T10:30:45Z",
     "uptime": 86400,
     "cpuUsage": 45.2,
     "memoryUsage": 512,
     "framesProcessed": 12345,
     "errorCount": 0,
     "lastError": null
   }
   ```

4. **Error Event**
   ```
   Topic: smartrh/devices/camera_01/health/error
   Payload: {
     "timestamp": "2026-04-20T10:30:45Z",
     "errorCode": "FRAME_CAPTURE_FAILED",
     "errorMessage": "USB camera disconnected",
     "severity": "ERROR",
     "stackTrace": "..."
   }
   ```

**Subscribe Topics** (Backend → Device):

```
Topic: smartrh/commands/camera_01/control/{command}

Commands:
- restart         → Restart device
- update_settings → Update recognition parameters
- capture_frame   → Capture single frame (for testing)
- reset_errors    → Clear error logs
```

### Motion Sensor

**Publish Topics**:

```
smartrh/devices/motion_01/motion/detected
{
  "timestamp": "2026-04-20T10:30:45Z",
  "motionDetected": true,
  "sensitivity": 85,
  "location": "room_center",
  "motionDuration": 5  // seconds
}

smartrh/devices/motion_01/health/heartbeat
```

### Door Lock

**Publish Topics**:

```
smartrh/devices/lock_01/door/lock_status
{
  "timestamp": "2026-04-20T10:30:45Z",
  "locked": true,
  "batteryLevel": 85
}

smartrh/devices/lock_01/door/unlock_attempt
{
  "timestamp": "2026-04-20T10:30:45Z",
  "success": true,
  "method": "rfid",
  "employeeId": 42
}

smartrh/devices/lock_01/health/heartbeat
```

**Subscribe Topics**:

```
smartrh/commands/lock_01/door/lock
smartrh/commands/lock_01/door/unlock
```

### Access Control Reader

**Publish Topics**:

```
smartrh/devices/access_reader_01/access/swipe
{
  "timestamp": "2026-04-20T10:30:45Z",
  "cardId": "RFID_HEX_VALUE",
  "employeeId": 42,
  "accessGranted": true,
  "location": "entrance"
}

smartrh/devices/access_reader_01/health/heartbeat
```

## Connection Examples

### Python Client

```python
import paho.mqtt.client as mqtt
import json
import ssl
from datetime import datetime

class DeviceMqttClient:
    def __init__(self, broker, device_id, username, password):
        self.broker = broker
        self.device_id = device_id
        self.username = username
        self.password = password
        self.client = mqtt.Client(client_id=device_id)

    def connect(self, ca_cert_path):
        # Set authentication
        self.client.username_pw_set(self.username, self.password)

        # Enable TLS
        self.client.tls_set(
            ca_certs=ca_cert_path,
            certfile=None,
            keyfile=None,
            cert_reqs=ssl.CERT_REQUIRED,
            tls_version=ssl.PROTOCOL_TLSv1_2,
            ciphers=None
        )

        # Set callbacks
        self.client.on_connect = self.on_connect
        self.client.on_disconnect = self.on_disconnect
        self.client.on_message = self.on_message

        # Connect
        self.client.connect(self.broker, 8883, keepalive=60)
        self.client.loop_start()

    def on_connect(self, client, userdata, flags, rc):
        if rc == 0:
            print("Connected to MQTT broker")
            # Subscribe to commands
            client.subscribe(f"smartrh/commands/{self.device_id}/+")
        else:
            print(f"Connection failed with code {rc}")

    def on_disconnect(self, client, userdata, rc):
        if rc != 0:
            print(f"Unexpected disconnection: {rc}")

    def on_message(self, client, userdata, msg):
        print(f"Received command: {msg.topic}")
        payload = json.loads(msg.payload.decode())
        # Handle command

    def publish_recognition(self, employee_id, confidence, event_type):
        topic = f"smartrh/devices/{self.device_id}/face/recognition"
        payload = {
            "timestamp": datetime.utcnow().isoformat() + "Z",
            "employeeId": employee_id,
            "confidence": confidence,
            "eventType": event_type,
            "faceEmbedding": "[...]",
            "metadata": {
                "processingTimeMs": 250
            }
        }
        self.client.publish(topic, json.dumps(payload), qos=1)

    def publish_heartbeat(self):
        topic = f"smartrh/devices/{self.device_id}/health/heartbeat"
        payload = {
            "timestamp": datetime.utcnow().isoformat() + "Z",
            "uptime": 86400,
            "cpuUsage": 45.2,
            "memoryUsage": 512,
            "framesProcessed": 12345,
            "errorCount": 0
        }
        self.client.publish(topic, json.dumps(payload), qos=1)

# Usage
client = DeviceMqttClient(
    broker="mosquitto.smartrh.local",
    device_id="camera_01",
    username="device_camera_01",
    password="PASSWORD_HERE"
)
client.connect("/path/to/ca.crt")

# Publish recognition event
client.publish_recognition(
    employee_id=42,
    confidence=0.98,
    event_type="IN"
)

# Keep running
import time
while True:
    client.publish_heartbeat()
    time.sleep(30)
```

### Node.js Client

```javascript
const mqtt = require("mqtt");
const fs = require("fs");

const ca = fs.readFileSync("path/to/ca.crt", "utf8");

const client = mqtt.connect({
  protocol: "mqtts",
  host: "mosquitto.smartrh.local",
  port: 8883,
  username: "device_camera_01",
  password: "PASSWORD_HERE",
  ca: [ca],
  rejectUnauthorized: false, // for self-signed certs
});

client.on("connect", () => {
  console.log("Connected to MQTT broker");

  // Subscribe to commands
  client.subscribe("smartrh/commands/camera_01/+");

  // Publish recognition event
  const payload = {
    timestamp: new Date().toISOString(),
    employeeId: 42,
    confidence: 0.98,
    eventType: "IN",
    faceEmbedding: "[...]",
    metadata: {
      processingTimeMs: 250,
    },
  };

  client.publish(
    "smartrh/devices/camera_01/face/recognition",
    JSON.stringify(payload),
    { qos: 1 },
  );
});

client.on("message", (topic, message) => {
  console.log(`Received: ${topic}`);
  const payload = JSON.parse(message.toString());
  // Handle command
});
```

## Testing Connection

### Using MQTT CLI Tools

```bash
# Subscribe to camera events (monitor in real-time)
mosquitto_sub -h mosquitto.smartrh.local -p 8883 \
  -u device_camera_01 -P PASSWORD_HERE \
  -t "smartrh/devices/camera_01/+/+" \
  --cafile infra/mosquitto/certs/ca.crt

# Publish test message
mosquitto_pub -h mosquitto.smartrh.local -p 8883 \
  -u backend_admin -P PASSWORD_HERE \
  -t "smartrh/devices/camera_01/face/recognition" \
  -m '{"timestamp":"2026-04-20T10:30:46Z","employeeId":42,"confidence":0.98,"eventType":"IN"}' \
  --cafile infra/mosquitto/certs/ca.crt
```

## Troubleshooting

### Connection Issues

1. **"Connection refused"**
   - Verify Mosquitto is running: `docker compose ps mqtt`
   - Check port 8883 is exposed: `docker compose port mqtt 8883`
   - Verify ca.crt is accessible

2. **"Authentication failed"**
   - Verify username/password in passwd file
   - Check credentials match MQTT_USERNAME/MQTT_PASSWORD env vars
   - Verify ACL file allows device's topic

3. **"Certificate verification failed"**
   - Ensure ca.crt is in correct path
   - Verify certificate generation script ran successfully
   - Check certificate hasn't expired: `openssl x509 -in certs/ca.crt -noout -dates`

### Message Issues

1. **Messages not arriving at backend**
   - Check backend logs: `docker compose logs backend`
   - Verify Spring MQTT integration is enabled
   - Check topic names match exactly (case-sensitive)

2. **Recognition not showing in dashboard**
   - Verify WebSocket connection: check browser console
   - Check `findUnverifiedFraudAlerts()` query in backend
   - Monitor MQTT messages: use mosquitto_sub to verify messages

## Security Best Practices

1. **Rotate Credentials Regularly**

   ```bash
   # Generate new password for a device
   mosquitto_passwd /etc/mosquitto/passwd device_camera_01
   # Restart Mosquitto
   docker compose restart mqtt
   ```

2. **Restrict Network Access**
   - Firewall: Allow only internal IPs to port 8883
   - Docker: Use custom networks, don't expose on all interfaces
   - VPN: Consider VPN tunnel for remote devices

3. **Monitor MQTT Traffic**

   ```bash
   # Check active connections
   docker compose exec mqtt mosquitto_sub -h localhost -u backend_admin -t '$SYS/broker/clients/+' -W 1
   ```

4. **Certificate Rotation**
   - Certificates expire after 365 days (see generate-mqtt-certs.sh)
   - Set calendar reminder to regenerate before expiry
   - Keep backup of keys securely

## Production Checklist

- [ ] SSL certificates generated and validated
- [ ] All device credentials created and distributed securely
- [ ] Mosquitto ACL rules restrict access appropriately
- [ ] Backend MQTT integration enabled (APP_MQTT_ENABLED=true)
- [ ] Mosquitto persistence enabled (survives restarts)
- [ ] Monitoring/alerting set up for device disconnections
- [ ] Firewall rules restrict MQTT access
- [ ] Regular backups of passwd and ACL files
- [ ] Certificate expiry monitoring in place
- [ ] Load testing completed (50-200 devices)

## Support

For issues or questions:

- Check Mosquitto logs: `docker compose logs mqtt`
- Check backend logs: `docker compose logs backend`
- Review MQTT spec: https://mqtt.org/
- Mosquitto documentation: https://mosquitto.org/documentation/
