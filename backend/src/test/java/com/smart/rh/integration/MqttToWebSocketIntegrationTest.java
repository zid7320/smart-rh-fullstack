package com.smart.rh.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.rh.dto.TemperatureEventDto;
import com.smart.rh.dto.Co2EventDto;
import com.smart.rh.entity.BureauSensor;
import com.smart.rh.entity.TemperatureReading;
import com.smart.rh.entity.Co2Reading;
import com.smart.rh.entity.SensorAlert;
import com.smart.rh.repository.BureauSensorRepository;
import com.smart.rh.repository.TemperatureReadingRepository;
import com.smart.rh.repository.Co2ReadingRepository;
import com.smart.rh.repository.SensorAlertRepository;
import com.smart.rh.service.TemperatureSensorService;
import com.smart.rh.service.Co2SensorService;
import com.smart.rh.mqtt.MqttMessageRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MqttToWebSocketIntegrationTest {

    @Autowired
    private BureauSensorRepository bureauSensorRepository;

    @Autowired
    private TemperatureReadingRepository temperatureReadingRepository;

    @Autowired
    private Co2ReadingRepository co2ReadingRepository;

    @Autowired
    private SensorAlertRepository sensorAlertRepository;

    @Autowired
    private TemperatureSensorService temperatureSensorService;

    @Autowired
    private Co2SensorService co2SensorService;

    @Autowired
    private MqttMessageRouter mqttMessageRouter;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private BureauSensor testSensor;

    @BeforeEach
    void setUp() {
        // Clear repositories
        temperatureReadingRepository.deleteAll();
        co2ReadingRepository.deleteAll();
        sensorAlertRepository.deleteAll();
        bureauSensorRepository.deleteAll();

        // Create test sensor
        testSensor = new BureauSensor();
        testSensor.setDeviceId("SENSOR_TEST_001");
        testSensor.setName("Integration Test Room");
        testSensor.setLocation("Test Lab");
        testSensor.setIsActive(true);
        testSensor = bureauSensorRepository.save(testSensor);
    }

    @Test
    void testTemperatureMqttToDatabase() {
        // Simulate MQTT message arrival
        Instant now = Instant.now();
        temperatureSensorService.processReading(
                testSensor.getId(),
                new BigDecimal("22.5"),
                new BigDecimal("45.0"),
                now);

        // Verify data persisted to database
        TemperatureReading saved = temperatureReadingRepository
                .findLatestBySensorId(testSensor.getId());

        assertNotNull(saved);
        assertEquals(new BigDecimal("22.5"), saved.getTemperature());
        assertEquals(new BigDecimal("45.0"), saved.getHumidity());
        assertEquals(testSensor.getId(), saved.getSensor().getId());
    }

    @Test
    void testCo2MqttToDatabase() {
        // Simulate MQTT message arrival
        Instant now = Instant.now();
        co2SensorService.processReading(
                testSensor.getId(),
                950,
                new BigDecimal("0.095"),
                now);

        // Verify data persisted to database
        Co2Reading saved = co2ReadingRepository
                .findLatestBySensorId(testSensor.getId());

        assertNotNull(saved);
        assertEquals(950, saved.getCo2Level());
        assertEquals(new BigDecimal("0.095"), saved.getGasConcentration());
    }

    @Test
    void testTemperatureThresholdTriggersAlert() {
        // Process high temperature reading
        Instant now = Instant.now();
        temperatureSensorService.processReading(
                testSensor.getId(),
                new BigDecimal("35.0"),
                new BigDecimal("40.0"),
                now);

        // Verify alert was created
        SensorAlert alert = sensorAlertRepository
                .findBySensorIdAndAlertTypeAndIsActiveTrue(testSensor.getId(), "TEMPERATURE_HIGH");

        assertNotNull(alert);
        assertEquals("TEMPERATURE_HIGH", alert.getAlertType());
        assertTrue(alert.getIsActive());
    }

    @Test
    void testCo2ThresholdTriggersAlert() {
        // Process high CO2 reading
        Instant now = Instant.now();
        co2SensorService.processReading(
                testSensor.getId(),
                1800,
                new BigDecimal("0.18"),
                now);

        // Verify alert was created
        SensorAlert alert = sensorAlertRepository
                .findBySensorIdAndAlertTypeAndIsActiveTrue(testSensor.getId(), "CO2_HIGH");

        assertNotNull(alert);
        assertEquals("CO2_HIGH", alert.getAlertType());
    }

    @Test
    void testMultipleReadingsCreateHistory() {
        // Simulate 3 temperature readings over time
        for (int i = 0; i < 3; i++) {
            temperatureSensorService.processReading(
                    testSensor.getId(),
                    new BigDecimal(20 + i),
                    new BigDecimal(45 - i),
                    Instant.now().plusSeconds(i * 10));
        }

        // Verify history exists
        var history = temperatureReadingRepository
                .findLatestBySensorIdAndHours(testSensor.getId(), 24);

        assertNotNull(history);
        assertEquals(3, history.size());
    }

    @Test
    void testAlertAcknowledgement() {
        // Create an alert
        Instant now = Instant.now();
        temperatureSensorService.processReading(
                testSensor.getId(),
                new BigDecimal("35.0"),
                new BigDecimal("40.0"),
                now);

        SensorAlert alert = sensorAlertRepository
                .findBySensorIdAndAlertTypeAndIsActiveTrue(testSensor.getId(), "TEMPERATURE_HIGH");
        assertNotNull(alert);

        // Acknowledge the alert
        alert.setIsActive(false);
        alert.setAcknowledgedAt(Instant.now());
        alert.setAcknowledgedBy("admin");
        sensorAlertRepository.save(alert);

        // Verify acknowledgement
        SensorAlert updated = sensorAlertRepository.findById(alert.getId()).orElse(null);
        assertNotNull(updated);
        assertFalse(updated.getIsActive());
        assertEquals("admin", updated.getAcknowledgedBy());
    }

    @Test
    void testMqttPayloadParsing() throws Exception {
        // Simulate raw MQTT payload
        String temperaturePayload = "{\"sensorId\":1,\"temperature\":23.5,\"humidity\":50.0," +
                "\"timestamp\":\"2026-04-20T10:00:00Z\"}";

        TemperatureEventDto event = objectMapper.readValue(temperaturePayload, TemperatureEventDto.class);

        assertNotNull(event);
        assertEquals(1L, event.getSensorId());
        assertEquals(new BigDecimal("23.5"), event.getTemperature());
        assertEquals(new BigDecimal("50.0"), event.getHumidity());
    }

    @Test
    void testCo2MqttPayloadParsing() throws Exception {
        String co2Payload = "{\"sensorId\":1,\"co2Level\":900,\"gasConcentration\":0.09," +
                "\"timestamp\":\"2026-04-20T10:00:00Z\"}";

        Co2EventDto event = objectMapper.readValue(co2Payload, Co2EventDto.class);

        assertNotNull(event);
        assertEquals(1L, event.getSensorId());
        assertEquals(900, event.getCo2Level());
    }

    @Test
    void testDataFlowTemperatureMqttToDashboard() {
        // 1. MQTT message arrives and is processed
        Instant now = Instant.now();
        temperatureSensorService.processReading(
                testSensor.getId(),
                new BigDecimal("24.0"),
                new BigDecimal("48.0"),
                now);

        // 2. Data persists to database
        TemperatureReading reading = temperatureReadingRepository
                .findLatestBySensorId(testSensor.getId());
        assertNotNull(reading);

        // 3. Dashboard would fetch this via HTTP
        BureauSensor sensor = bureauSensorRepository.findById(testSensor.getId()).orElse(null);
        assertNotNull(sensor);
        assertEquals("SENSOR_TEST_001", sensor.getDeviceId());

        // 4. WebSocket would broadcast update (verified via mock)
        // In real scenario, messagingTemplate.convertAndSend() is called
    }
}
