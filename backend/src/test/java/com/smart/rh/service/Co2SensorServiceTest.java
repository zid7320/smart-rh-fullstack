package com.smart.rh.service;

import com.smart.rh.dto.Co2ReadingDto;
import com.smart.rh.entity.BureauSensor;
import com.smart.rh.entity.Co2Reading;
import com.smart.rh.repository.BureauSensorRepository;
import com.smart.rh.repository.Co2ReadingRepository;
import com.smart.rh.repository.SensorAlertRepository;
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
class Co2SensorServiceTest {

    @Mock
    private Co2ReadingRepository co2ReadingRepository;

    @Mock
    private BureauSensorRepository bureauSensorRepository;

    @Mock
    private SensorAlertRepository sensorAlertRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private Co2SensorService co2SensorService;

    private BureauSensor testSensor;
    private Co2Reading testReading;

    @BeforeEach
    void setUp() {
        testSensor = new BureauSensor();
        testSensor.setId(1L);
        testSensor.setDeviceId("CO2_SENSOR_01");
        testSensor.setName("Office Area");
        testSensor.setIsActive(true);

        testReading = new Co2Reading();
        testReading.setId(1L);
        testReading.setSensor(testSensor);
        testReading.setCo2Level(800);
        testReading.setGasConcentration(new BigDecimal("0.08"));
        testReading.setAlarmTriggered(false);
        testReading.setTimestamp(Instant.now());
    }

    @Test
    void testProcessReading_NormalLevel() {
        when(bureauSensorRepository.findById(1L)).thenReturn(Optional.of(testSensor));
        when(co2ReadingRepository.save(any(Co2Reading.class))).thenReturn(testReading);

        Co2ReadingDto result = co2SensorService.processReading(
                1L, 800, new BigDecimal("0.08"), Instant.now());

        assertNotNull(result);
        assertEquals(800, result.getCo2Level());
        assertFalse(result.isAlarmTriggered());
        verify(co2ReadingRepository, times(1)).save(any(Co2Reading.class));
    }

    @Test
    void testProcessReading_WarningLevel() {
        testReading.setCo2Level(1200);
        when(bureauSensorRepository.findById(1L)).thenReturn(Optional.of(testSensor));
        when(co2ReadingRepository.save(any(Co2Reading.class))).thenReturn(testReading);

        Co2ReadingDto result = co2SensorService.processReading(
                1L, 1200, new BigDecimal("0.12"), Instant.now());

        assertNotNull(result);
        assertEquals(1200, result.getCo2Level());
        verify(messagingTemplate, times(2)).convertAndSend(anyString(), any());
    }

    @Test
    void testProcessReading_AlarmLevel() {
        testReading.setCo2Level(2000);
        testReading.setAlarmTriggered(true);
        when(bureauSensorRepository.findById(1L)).thenReturn(Optional.of(testSensor));
        when(co2ReadingRepository.save(any(Co2Reading.class))).thenReturn(testReading);

        Co2ReadingDto result = co2SensorService.processReading(
                1L, 2000, new BigDecimal("0.20"), Instant.now());

        assertNotNull(result);
        assertEquals(2000, result.getCo2Level());
        assertTrue(result.isAlarmTriggered());
        verify(messagingTemplate, times(2)).convertAndSend(anyString(), any());
    }

    @Test
    void testProcessReading_BelowMinimum() {
        when(bureauSensorRepository.findById(1L)).thenReturn(Optional.of(testSensor));

        assertThrows(IllegalArgumentException.class,
                () -> co2SensorService.processReading(1L, -100, new BigDecimal("0.0"), Instant.now()));
    }

    @Test
    void testProcessReading_AboveMaximum() {
        when(bureauSensorRepository.findById(1L)).thenReturn(Optional.of(testSensor));

        assertThrows(IllegalArgumentException.class,
                () -> co2SensorService.processReading(1L, 6000, new BigDecimal("0.6"), Instant.now()));
    }

    @Test
    void testGetCo2Status_Good() {
        String status = co2SensorService.getCo2Status(800);
        assertEquals("GOOD", status);
    }

    @Test
    void testGetCo2Status_Warning() {
        String status = co2SensorService.getCo2Status(1200);
        assertEquals("WARNING", status);
    }

    @Test
    void testGetCo2Status_Alert() {
        String status = co2SensorService.getCo2Status(2000);
        assertEquals("ALERT", status);
    }
}
