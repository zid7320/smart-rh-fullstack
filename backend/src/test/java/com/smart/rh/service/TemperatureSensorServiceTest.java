package com.smart.rh.service;

import com.smart.rh.dto.TemperatureReadingDto;
import com.smart.rh.entity.BureauSensor;
import com.smart.rh.entity.SensorAlert;
import com.smart.rh.entity.TemperatureReading;
import com.smart.rh.repository.BureauSensorRepository;
import com.smart.rh.repository.SensorAlertRepository;
import com.smart.rh.repository.TemperatureReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemperatureSensorServiceTest {

    @Mock
    private TemperatureReadingRepository temperatureReadingRepository;

    @Mock
    private BureauSensorRepository bureauSensorRepository;

    @Mock
    private SensorAlertRepository sensorAlertRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private TemperatureSensorService temperatureSensorService;

    private BureauSensor testSensor;
    private TemperatureReading testReading;

    @BeforeEach
    void setUp() {
        testSensor = new BureauSensor();
        testSensor.setId(1L);
        testSensor.setDeviceId("TEMP_SENSOR_01");
        testSensor.setName("Conference Room");
        testSensor.setIsActive(true);

        testReading = new TemperatureReading();
        testReading.setId(1L);
        testReading.setSensor(testSensor);
        testReading.setTemperature(new BigDecimal("22.5"));
        testReading.setHumidity(new BigDecimal("45.0"));
        testReading.setTimestamp(Instant.now());
    }

    @Test
    void testProcessReading_Success() {
        when(bureauSensorRepository.findById(1L)).thenReturn(Optional.of(testSensor));
        when(temperatureReadingRepository.save(any(TemperatureReading.class))).thenReturn(testReading);
        when(sensorAlertRepository.findBySensorIdAndIsActiveTrue(1L)).thenReturn(null);

        TemperatureReadingDto result = temperatureSensorService.processReading(
                1L, new BigDecimal("22.5"), new BigDecimal("45.0"), Instant.now());

        assertNotNull(result);
        assertEquals(new BigDecimal("22.5"), result.getTemperature());
        verify(temperatureReadingRepository, times(1)).save(any(TemperatureReading.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any());
    }

    @Test
    void testProcessReading_BelowMinimum() {
        when(bureauSensorRepository.findById(1L)).thenReturn(Optional.of(testSensor));

        assertThrows(IllegalArgumentException.class, () -> temperatureSensorService.processReading(
                1L, new BigDecimal("-50.0"), new BigDecimal("45.0"), Instant.now()));
    }

    @Test
    void testProcessReading_AboveMaximum() {
        when(bureauSensorRepository.findById(1L)).thenReturn(Optional.of(testSensor));

        assertThrows(IllegalArgumentException.class, () -> temperatureSensorService.processReading(
                1L, new BigDecimal("85.0"), new BigDecimal("45.0"), Instant.now()));
    }

    @Test
    void testProcessReading_HighTemperatureThreshold() {
        testReading.setTemperature(new BigDecimal("35.0"));
        when(bureauSensorRepository.findById(1L)).thenReturn(Optional.of(testSensor));
        when(temperatureReadingRepository.save(any(TemperatureReading.class))).thenReturn(testReading);

        TemperatureReadingDto result = temperatureSensorService.processReading(
                1L, new BigDecimal("35.0"), new BigDecimal("45.0"), Instant.now());

        assertNotNull(result);
        verify(messagingTemplate, times(2)).convertAndSend(anyString(), any());
    }

    @Test
    void testProcessReading_InactiveSensor() {
        testSensor.setIsActive(false);
        when(bureauSensorRepository.findById(1L)).thenReturn(Optional.of(testSensor));

        assertThrows(IllegalStateException.class, () -> temperatureSensorService.processReading(
                1L, new BigDecimal("22.5"), new BigDecimal("45.0"), Instant.now()));
    }

    @Test
    void testGetTemperatureHistory() {
        when(temperatureReadingRepository.findLatestByDeviceId(1L, 24)).thenReturn(null);

        temperatureSensorService.getTemperatureHistory(1L, 24);

        verify(temperatureReadingRepository, times(1)).findLatestByDeviceId(1L, 24);
    }
}
