# SMART RH 4.0 - Complete API Testing Guide

## Overview

This guide provides comprehensive instructions for testing all completed modules:

- **Module 10**: Attendance Dashboard (Real-time facial recognition)
- **Module 19**: BI & Analytics Reporting (with CSV/PDF export)
- **Module 2**: Mosquitto MQTT Setup (with TLS/SSL security)

---

## Prerequisites

1. **Database**: MySQL running with SMART RH schema
2. **Backend**: Spring Boot application running on `http://localhost:8081`
3. **MQTT Broker**: Mosquitto running on `localhost:8883` (TLS) and `localhost:1883` (unencrypted testing)
4. **Frontend**: Angular application running on `http://localhost:4200` (optional for UI testing)

### Start Docker Services

```bash
cd d:/RH/smart-rh-fullstack
docker compose up mysql backend mqtt
```

### Verify Services Are Running

```bash
# Check MySQL
curl http://localhost:3307

# Check Backend Health
curl http://localhost:8081/api/health

# Check MQTT
mosquitto_sub -h localhost -p 1883 -u backend_admin -P testing -t "$SYS/broker/info" -W 1
```

---

## Authentication

All endpoints require a valid JWT token. To obtain a token:

```bash
# Login to get JWT token
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'

# Response:
# {
#   "token": "eyJhbGc...",
#   "refreshToken": "...",
#   "user": {...}
# }
```

Save the token:

```bash
export JWT_TOKEN="<your-token-here>"
```

Use in requests:

```bash
-H "Authorization: Bearer $JWT_TOKEN"
```

---

## Module 10: Attendance Dashboard

### Test Real-Time Attendance Events

#### 1. Get Recent Attendance Events

```bash
curl -X GET "http://localhost:8081/api/attendance/events/recent?page=0&size=10" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected Response**: Array of recent attendance events with fraud indicators

#### 2. Get Unverified Fraud Alerts

```bash
curl -X GET "http://localhost:8081/api/attendance/events/fraud-alerts/unverified" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected Response**: List of suspicious attendance events awaiting HR verification

#### 3. Verify Attendance Event (HR Action)

```bash
curl -X PUT "http://localhost:8081/api/attendance/events/123/verify" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "manuallyVerified": true,
    "verificationNotes": "Legitimate access - visitor badge visible in photo"
  }'
```

**Expected Response**: Updated attendance event with verification status

#### 4. Get Today's Attendance Summary

```bash
curl -X GET "http://localhost:8081/api/attendance/events/today/summary" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected Response**:

```json
{
  "totalCheckins": 245,
  "totalCheckouts": 198,
  "presentToday": 245,
  "suspiciousCount": 3,
  "avgConfidence": 98.5
}
```

---

## Module 19: BI & Analytics Reporting

### Analytics Endpoints

#### 1. Get Daily Attendance Trends

```bash
# Last 30 days
curl -X GET "http://localhost:8081/api/attendance/bi/trends/daily?days=30" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected Response**:

```json
[
  {
    "date": "2026-04-20",
    "checkIns": 245,
    "checkOuts": 198,
    "present": 245,
    "absent": 5,
    "suspicious": 2
  },
  ...
]
```

#### 2. Get Department Statistics

```bash
curl -X GET "http://localhost:8081/api/attendance/bi/stats/departments?startDate=2026-04-01&endDate=2026-04-20" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected Response**:

```json
[
  {
    "departmentId": 1,
    "departmentName": "Engineering",
    "totalEmployees": 50,
    "presentToday": 48,
    "absentToday": 2,
    "attendanceRate": 96.0,
    "suspiciousCount": 1
  },
  ...
]
```

#### 3. Get Peak Hours Analysis

```bash
curl -X GET "http://localhost:8081/api/attendance/bi/analytics/peak-hours?date=2026-04-20" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected Response**:

```json
[
  {
    "hour": 8,
    "checkInCount": 125,
    "checkOutCount": 5,
    "suspiciousCount": 1
  },
  {
    "hour": 9,
    "checkInCount": 98,
    "checkOutCount": 8,
    "suspiciousCount": 0
  },
  ...
]
```

#### 4. Get Fraud Metrics

```bash
curl -X GET "http://localhost:8081/api/attendance/bi/metrics/fraud?startDate=2026-04-01&endDate=2026-04-20" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected Response**:

```json
{
  "totalEvents": 5000,
  "suspiciousEvents": 45,
  "fraudRate": 0.9,
  "unverifiedAlerts": 12,
  "verifiedFraudCount": 33,
  "topFraudReasons": [
    {
      "reason": "MASK_DETECTED",
      "count": 20,
      "percentage": 44.44
    },
    {
      "reason": "SPOOFING_DETECTED",
      "count": 15,
      "percentage": 33.33
    },
    ...
  ]
}
```

#### 5. Get Employee Reliability Scores

```bash
curl -X GET "http://localhost:8081/api/attendance/bi/analytics/employee-reliability?limit=20" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected Response**:

```json
[
  {
    "employeeId": 42,
    "employeeName": "John Doe",
    "departmentName": "Engineering",
    "attendanceRate": 92.5,
    "inconsistencyScore": 25.3,
    "lastWeekAbsences": 1,
    "lastMonthFraudAlerts": 2
  },
  ...
]
```

#### 6. Get Attendance Summary

```bash
curl -X GET "http://localhost:8081/api/attendance/bi/summary?startDate=2026-04-01&endDate=2026-04-20" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

**Expected Response**:

```json
{
  "period": {
    "startDate": "2026-04-01",
    "endDate": "2026-04-20",
    "daysCount": 20
  },
  "totalCheckIns": 4900,
  "totalCheckOuts": 3900,
  "totalPresent": 4900,
  "totalAbsent": 100,
  "totalSuspicious": 45,
  "averageDailyAttendance": 97.5,
  "fraudRate": 0.92,
  "averageConfidenceScore": 97.8
}
```

---

## Export Endpoints (CSV & PDF)

### CSV Exports

#### 1. Export Attendance Summary as CSV

```bash
curl -X GET "http://localhost:8081/api/attendance/bi/export/csv?startDate=2026-04-01&endDate=2026-04-20" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -o attendance_report.csv
```

**Output**: CSV file with attendance metrics

#### 2. Export Fraud Metrics as CSV

```bash
curl -X GET "http://localhost:8081/api/attendance/bi/export/fraud-csv?startDate=2026-04-01&endDate=2026-04-20" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -o fraud_report.csv
```

**Output**: CSV file with fraud analysis data

#### 3. Export Daily Trends as CSV

```bash
curl -X GET "http://localhost:8081/api/attendance/bi/export/trends-csv?days=30" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -o daily_trends.csv
```

**Output**: CSV file with 30-day attendance trends

### PDF Exports

#### 1. Export Attendance Summary as PDF

```bash
curl -X GET "http://localhost:8081/api/attendance/bi/export/pdf?startDate=2026-04-01&endDate=2026-04-20" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -o attendance_report.pdf
```

**Output**: Professional PDF report with formatted tables

#### 2. Export Fraud Metrics as PDF

```bash
curl -X GET "http://localhost:8081/api/attendance/bi/export/fraud-pdf?startDate=2026-04-01&endDate=2026-04-20" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -o fraud_report.pdf
```

**Output**: PDF with fraud analysis and top reasons breakdown

#### 3. Export Daily Trends as PDF

```bash
curl -X GET "http://localhost:8081/api/attendance/bi/export/trends-pdf?days=30" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -o daily_trends.pdf
```

**Output**: PDF with 30-day attendance trend analysis

---

## Module 2: MQTT Integration Testing

### Prerequisites

1. Generate MQTT certificates:

```bash
cd infra/mosquitto
bash generate-mqtt-certs.sh
```

2. Verify certificates were created:

```bash
ls -la certs/
# Should show: ca.crt, server.crt, server.key
```

### Test MQTT Connection

#### 1. Test Unencrypted Connection (Development)

```bash
# Subscribe to all device messages
mosquitto_sub -h localhost -p 1883 \
  -u backend_admin -P testing \
  -t "smartrh/devices/+/+/+" \
  -v

# In another terminal, publish a test message
mosquitto_pub -h localhost -p 1883 \
  -u device_camera_01 -P <password> \
  -t "smartrh/devices/camera_01/face/recognition" \
  -m '{
    "timestamp": "2026-04-20T10:30:46Z",
    "employeeId": 42,
    "confidence": 0.98,
    "eventType": "IN",
    "faceEmbedding": "[0.123, 0.456]",
    "metadata": {
      "processingTimeMs": 250,
      "model": "facenet_mobilenet"
    }
  }'
```

**Expected**: Message appears in subscriber terminal

#### 2. Test TLS Connection (Production)

```bash
# Subscribe with TLS
mosquitto_sub -h localhost -p 8883 \
  --cafile infra/mosquitto/certs/ca.crt \
  -u backend_admin -P testing \
  -t "smartrh/devices/+/+/+" \
  -v

# Publish with TLS
mosquitto_pub -h localhost -p 8883 \
  --cafile infra/mosquitto/certs/ca.crt \
  -u device_camera_01 -P <password> \
  -t "smartrh/devices/camera_01/health/heartbeat" \
  -m '{
    "timestamp": "2026-04-20T10:30:45Z",
    "uptime": 86400,
    "cpuUsage": 45.2,
    "memoryUsage": 512,
    "framesProcessed": 12345,
    "errorCount": 0
  }'
```

**Expected**: TLS handshake succeeds, message delivered

#### 3. Test Authentication Failure

```bash
# Try wrong password
mosquitto_sub -h localhost -p 1883 \
  -u device_camera_01 -P wrongpassword \
  -t "smartrh/devices/camera_01/+/+" \
  -W 1

# Expected error: "Connection Refused – not authorised"
```

#### 4. Test Topic ACL Enforcement

```bash
# Device can only publish to its own topic
mosquitto_pub -h localhost -p 1883 \
  -u device_camera_01 -P <password> \
  -t "smartrh/devices/camera_02/face/recognition" \
  -m '{"test": "message"}'

# Expected error: "Access Denied"

# But this should work:
mosquitto_pub -h localhost -p 1883 \
  -u device_camera_01 -P <password> \
  -t "smartrh/devices/camera_01/face/recognition" \
  -m '{"test": "message"}'

# Expected: Success
```

#### 5. Test Backend Integration

```bash
# Subscribe to backend commands
mosquitto_sub -h localhost -p 1883 \
  -u device_camera_01 -P <password> \
  -t "smartrh/commands/camera_01/+" \
  -v

# In backend, publish a command (you can add this endpoint if needed)
# The backend should be able to send commands to devices
```

---

## Integration Testing Workflow

### End-to-End Test Scenario

**Scenario**: A facial recognition event occurs, gets processed, displayed in real-time, and included in analytics.

1. **Device sends MQTT message**:

```bash
mosquitto_pub -h localhost -p 1883 \
  -u device_camera_01 -P <password> \
  -t "smartrh/devices/camera_01/face/recognition" \
  -m '{
    "timestamp": "2026-04-20T14:30:00Z",
    "employeeId": 42,
    "confidence": 0.98,
    "eventType": "IN",
    "fraudFlags": [],
    "metadata": {
      "processingTimeMs": 180,
      "model": "facenet_mobilenet",
      "modelVersion": "1.2.0"
    }
  }'
```

2. **Verify message received in backend**:

```bash
# Check backend logs
docker compose logs backend | grep "MQTT" | tail -20

# Should see message processing and storage
```

3. **Verify data in database**:

```bash
# Connect to MySQL
mysql -h localhost -u smart_rh -p smart_rh_pass smart_rh_db

# Check attendance events
SELECT * FROM attendance_event WHERE employee_id = 42 ORDER BY event_timestamp DESC LIMIT 1;
```

4. **Check real-time dashboard**:

- Open http://localhost:4200/attendance
- New event should appear in feed within 1 second

5. **Verify in analytics**:

```bash
curl -X GET "http://localhost:8081/api/attendance/bi/analytics/peak-hours?date=2026-04-20" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq '.[] | select(.hour == 14)'

# Should show the new check-in count in 14:00 hour
```

6. **Export report**:

```bash
curl -X GET "http://localhost:8081/api/attendance/bi/export/pdf?startDate=2026-04-20&endDate=2026-04-20" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -o daily_report.pdf

# Open daily_report.pdf and verify event is included
```

---

## Troubleshooting

### MQTT Connection Issues

| Issue                             | Solution                                                                               |
| --------------------------------- | -------------------------------------------------------------------------------------- |
| "Connection refused"              | Ensure Mosquitto is running: `docker compose ps mqtt`                                  |
| "Authentication failed"           | Check credentials in passwd file: `docker compose exec mqtt cat /etc/mosquitto/passwd` |
| "Certificate verification failed" | Regenerate certs: `cd infra/mosquitto && bash generate-mqtt-certs.sh`                  |
| "Topic access denied"             | Verify ACL file: `cat infra/mosquitto/acl`                                             |

### Backend Integration Issues

| Issue                            | Solution                                                                |
| -------------------------------- | ----------------------------------------------------------------------- |
| MQTT events not appearing in DB  | Check Spring MQTT config: `APP_MQTT_ENABLED=true` in docker-compose.yml |
| Export endpoints return 500      | Verify iText and Commons CSV dependencies in pom.xml                    |
| Employee reliability shows empty | Ensure attendance data exists for test employees                        |

### Frontend Real-Time Issues

| Issue                            | Solution                                                |
| -------------------------------- | ------------------------------------------------------- |
| WebSocket not connecting         | Check browser console for errors; verify backend health |
| Events not updating in real-time | Check WebSocket connection in browser DevTools          |
| Charts not rendering             | Verify BiService returns data with proper structure     |

---

## Performance Testing

### Load Test MQTT Broker

Simulate 50 devices publishing every 30 seconds:

```bash
#!/bin/bash
# test_mqtt_load.sh

for device_id in {01..50}; do
  (
    while true; do
      mosquitto_pub -h localhost -p 1883 \
        -u device_camera_$device_id \
        -P test_password_$device_id \
        -t "smartrh/devices/camera_$device_id/health/heartbeat" \
        -m "{\"timestamp\":\"$(date -u +%Y-%m-%dT%H:%M:%SZ)\",\"uptime\":86400}"
      sleep 30
    done
  ) &
done
wait
```

Monitor broker stats:

```bash
mosquitto_sub -h localhost -p 1883 \
  -u backend_admin -P testing \
  -t '$SYS/broker/clients/#' \
  -W 1
```

### Load Test Analytics Queries

```bash
# Test with date ranges
for i in {1..100}; do
  curl -X GET "http://localhost:8081/api/attendance/bi/trends/daily?days=30" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    > /dev/null &
done
wait

# Should complete in <5 seconds for 100 concurrent requests
```

---

## Verification Checklist

- [ ] All MQTT connections succeed with valid credentials
- [ ] Authentication fails with invalid credentials
- [ ] TLS handshake works (port 8883)
- [ ] Unencrypted connection works (port 1883, dev only)
- [ ] Topic ACL prevents cross-device access
- [ ] Attendance events appear in real-time dashboard
- [ ] Analytics queries return correct counts
- [ ] CSV exports contain proper data
- [ ] PDF exports are properly formatted
- [ ] Employee reliability scoring shows realistic scores
- [ ] Fraud detection is working and verified
- [ ] WebSocket updates are real-time (<1 second)
- [ ] Exports work for all three report types (attendance, fraud, trends)
- [ ] Backend handles concurrent MQTT messages
- [ ] Dashboard responsiveness is good (all queries <2 seconds)

---

## API Summary

| Feature              | Endpoint                                            | Method | Status |
| -------------------- | --------------------------------------------------- | ------ | ------ |
| Get Recent Events    | `/api/attendance/events/recent`                     | GET    | ✅     |
| Get Fraud Alerts     | `/api/attendance/events/fraud-alerts/unverified`    | GET    | ✅     |
| Verify Event         | `/api/attendance/events/{id}/verify`                | PUT    | ✅     |
| Today Summary        | `/api/attendance/events/today/summary`              | GET    | ✅     |
| Daily Trends         | `/api/attendance/bi/trends/daily`                   | GET    | ✅     |
| Department Stats     | `/api/attendance/bi/stats/departments`              | GET    | ✅     |
| Peak Hours           | `/api/attendance/bi/analytics/peak-hours`           | GET    | ✅     |
| Fraud Metrics        | `/api/attendance/bi/metrics/fraud`                  | GET    | ✅     |
| Employee Reliability | `/api/attendance/bi/analytics/employee-reliability` | GET    | ✅     |
| Attendance Summary   | `/api/attendance/bi/summary`                        | GET    | ✅     |
| Export CSV           | `/api/attendance/bi/export/csv`                     | GET    | ✅     |
| Export PDF           | `/api/attendance/bi/export/pdf`                     | GET    | ✅     |
| Export Fraud CSV     | `/api/attendance/bi/export/fraud-csv`               | GET    | ✅     |
| Export Fraud PDF     | `/api/attendance/bi/export/fraud-pdf`               | GET    | ✅     |
| Export Trends CSV    | `/api/attendance/bi/export/trends-csv`              | GET    | ✅     |
| Export Trends PDF    | `/api/attendance/bi/export/trends-pdf`              | GET    | ✅     |

---

## Next Steps

1. Generate MQTT certificates: `bash infra/mosquitto/generate-mqtt-certs.sh`
2. Start services: `docker compose up mysql backend mqtt`
3. Run integration tests from this guide
4. Deploy to production with proper environment variables
5. Set up monitoring and alerting for MQTT broker

---

**Last Updated**: 2026-04-20  
**Module Status**: All TODOs completed ✅
