package com.smart.rh.service;

import com.smart.rh.dto.SensorAlertDto;
import com.smart.rh.entity.SensorAlert;
import com.smart.rh.repository.SensorAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorAlertServiceTest {

    @Mock
    private SensorAlertRepository sensorAlertRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private SensorAlertService sensorAlertService;

    private SensorAlert testAlert;

    @BeforeEach
    void setUp() {
        testAlert = new SensorAlert();
        testAlert.setId(1L);
        testAlert.setSensorId(1L);
        testAlert.setAlertType("TEMPERATURE_HIGH");
        testAlert.setThresholdValue(30.0);
        testAlert.setActualValue(35.5);
        testAlert.setIsActive(true);
        testAlert.setTriggeredAt(Instant.now());
    }

    @Test
    void testCreateAlert_Success() {
        when(sensorAlertRepository.save(any(SensorAlert.class))).thenReturn(testAlert);

        SensorAlertDto result = sensorAlertService.createAlert(
                1L, "TEMPERATURE_HIGH", 30.0, 35.5);

        assertNotNull(result);
        assertEquals("TEMPERATURE_HIGH", result.getAlertType());
        assertTrue(result.isActive());
        verify(sensorAlertRepository, times(1)).save(any(SensorAlert.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any());
    }

    @Test
    void testGetActiveAlerts() {
        List<SensorAlert> alerts = new ArrayList<>();
        alerts.add(testAlert);
        when(sensorAlertRepository.findAllByIsActiveTrue()).thenReturn(alerts);

        List<SensorAlertDto> results = sensorAlertService.getActiveAlerts();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("TEMPERATURE_HIGH", results.get(0).getAlertType());
        verify(sensorAlertRepository, times(1)).findAllByIsActiveTrue();
    }

    @Test
    void testAcknowledgeAlert_Success() {
        testAlert.setAcknowledgedAt(Instant.now());
        testAlert.setAcknowledgedBy("admin");
        testAlert.setIsActive(false);

        when(sensorAlertRepository.findById(1L)).thenReturn(Optional.of(testAlert));
        when(sensorAlertRepository.save(any(SensorAlert.class))).thenReturn(testAlert);

        SensorAlertDto result = sensorAlertService.acknowledgeAlert(1L, "admin");

        assertNotNull(result);
        assertFalse(result.isActive());
        assertEquals("admin", result.getAcknowledgedBy());
        verify(sensorAlertRepository, times(1)).save(any(SensorAlert.class));
    }

    @Test
    void testAcknowledgeAlert_NotFound() {
        when(sensorAlertRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> sensorAlertService.acknowledgeAlert(99L, "admin"));
    }

    @Test
    void testGetAlertsBySensorId() {
        List<SensorAlert> alerts = new ArrayList<>();
        alerts.add(testAlert);
        when(sensorAlertRepository.findBySensorId(1L)).thenReturn(alerts);

        List<SensorAlertDto> results = sensorAlertService.getAlertsBySensorId(1L);

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(sensorAlertRepository, times(1)).findBySensorId(1L);
    }

    @Test
    void testGetActiveBySensorId() {
        List<SensorAlert> alerts = new ArrayList<>();
        alerts.add(testAlert);
        when(sensorAlertRepository.findBySensorIdAndIsActiveTrue(1L)).thenReturn(alerts);

        List<SensorAlertDto> results = sensorAlertService.getActiveBySensorId(1L);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isActive());
        verify(sensorAlertRepository, times(1)).findBySensorIdAndIsActiveTrue(1L);
    }

    @Test
    void testCheckThresholds_TemperatureHigh() {
        when(sensorAlertRepository.findBySensorIdAndAlertTypeAndIsActiveTrue(
                1L, "TEMPERATURE_HIGH")).thenReturn(null);
        when(sensorAlertRepository.save(any(SensorAlert.class))).thenReturn(testAlert);

        sensorAlertService.checkAndCreateTemperatureAlert(1L, 35.5, 30.0);

        verify(sensorAlertRepository, times(1)).save(any(SensorAlert.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any());
    }

    @Test
    void testCheckThresholds_TemperatureLow() {
        SensorAlert lowAlert = new SensorAlert();
        lowAlert.setAlertType("TEMPERATURE_LOW");
        lowAlert.setThresholdValue(15.0);
        lowAlert.setActualValue(10.0);

        when(sensorAlertRepository.findBySensorIdAndAlertTypeAndIsActiveTrue(
                1L, "TEMPERATURE_LOW")).thenReturn(null);
        when(sensorAlertRepository.save(any(SensorAlert.class))).thenReturn(lowAlert);

        sensorAlertService.checkAndCreateTemperatureAlert(1L, 10.0, 15.0);

        verify(sensorAlertRepository, times(1)).save(any(SensorAlert.class));
    }

    @Test
    void testCheckThresholds_Co2Alert() {
        SensorAlert co2Alert = new SensorAlert();
        co2Alert.setAlertType("CO2_HIGH");
        co2Alert.setThresholdValue(1500.0);
        co2Alert.setActualValue(1800.0);

        when(sensorAlertRepository.findBySensorIdAndAlertTypeAndIsActiveTrue(
                1L, "CO2_HIGH")).thenReturn(null);
        when(sensorAlertRepository.save(any(SensorAlert.class))).thenReturn(co2Alert);

        sensorAlertService.checkAndCreateCo2Alert(1L, 1800, 1500.0);

        verify(sensorAlertRepository, times(1)).save(any(SensorAlert.class));
    }

    @Test
    void testAlertMapping() {
        SensorAlertDto dto = sensorAlertService.mapToDto(testAlert);

        assertNotNull(dto);
        assertEquals(testAlert.getId(), dto.getId());
        assertEquals(testAlert.getAlertType(), dto.getAlertType());
        assertEquals(testAlert.getThresholdValue(), dto.getThresholdValue());
        assertEquals(testAlert.getActualValue(), dto.getActualValue());
    }
}
