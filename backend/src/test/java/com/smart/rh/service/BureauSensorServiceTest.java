package com.smart.rh.service;

import com.smart.rh.dto.BureauDashboardDto;
import com.smart.rh.entity.BureauSensor;
import com.smart.rh.repository.BureauSensorRepository;
import com.smart.rh.repository.Co2ReadingRepository;
import com.smart.rh.repository.OccupancyStatusRepository;
import com.smart.rh.repository.SensorAlertRepository;
import com.smart.rh.repository.SensorHealthRepository;
import com.smart.rh.repository.TemperatureReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BureauSensorServiceTest {

    @Mock
    private BureauSensorRepository bureauSensorRepository;

    @Mock
    private TemperatureReadingRepository temperatureReadingRepository;

    @Mock
    private Co2ReadingRepository co2ReadingRepository;

    @Mock
    private OccupancyStatusRepository occupancyStatusRepository;

    @Mock
    private SensorHealthRepository sensorHealthRepository;

    @Mock
    private SensorAlertRepository sensorAlertRepository;

    @InjectMocks
    private BureauSensorService bureauSensorService;

    private BureauSensor testSensor;

    @BeforeEach
    void setUp() {
        testSensor = new BureauSensor();
        testSensor.setId(1L);
        testSensor.setDeviceId("SENSOR_001");
        testSensor.setName("Conference Room");
        testSensor.setLocation("Building A");
        testSensor.setIsActive(true);
    }

    @Test
    void testRegisterSensor_Success() {
        when(bureauSensorRepository.save(any(BureauSensor.class))).thenReturn(testSensor);

        BureauSensor result = bureauSensorService.registerSensor(testSensor);

        assertNotNull(result);
        assertEquals("SENSOR_001", result.getDeviceId());
        verify(bureauSensorRepository, times(1)).save(any(BureauSensor.class));
    }

    @Test
    void testGetSensorDashboard_Success() {
        when(bureauSensorRepository.findById(1L)).thenReturn(Optional.of(testSensor));
        when(temperatureReadingRepository.findLatestBySensorId(1L)).thenReturn(null);
        when(co2ReadingRepository.findLatestBySensorId(1L)).thenReturn(null);
        when(occupancyStatusRepository.findLatestBySensorId(1L)).thenReturn(null);
        when(sensorHealthRepository.findLatestBySensorId(1L)).thenReturn(null);
        when(sensorAlertRepository.findBySensorIdAndIsActiveTrue(1L)).thenReturn(new ArrayList<>());

        BureauDashboardDto result = bureauSensorService.getSensorDashboard(1L);

        assertNotNull(result);
        assertEquals(1L, result.getSensor().getId());
        verify(bureauSensorRepository, times(1)).findById(1L);
    }

    @Test
    void testGetSensorDashboard_NotFound() {
        when(bureauSensorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> bureauSensorService.getSensorDashboard(99L));
    }

    @Test
    void testGetAllSensorsDashboard() {
        List<BureauSensor> sensors = new ArrayList<>();
        sensors.add(testSensor);
        when(bureauSensorRepository.findAllByIsActiveTrue()).thenReturn(sensors);
        when(temperatureReadingRepository.findLatestBySensorId(anyLong())).thenReturn(null);
        when(co2ReadingRepository.findLatestBySensorId(anyLong())).thenReturn(null);
        when(occupancyStatusRepository.findLatestBySensorId(anyLong())).thenReturn(null);
        when(sensorHealthRepository.findLatestBySensorId(anyLong())).thenReturn(null);
        when(sensorAlertRepository.findBySensorIdAndIsActiveTrue(anyLong())).thenReturn(new ArrayList<>());

        List<BureauDashboardDto> results = bureauSensorService.getAllSensorsDashboard();

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(bureauSensorRepository, times(1)).findAllByIsActiveTrue();
    }

    @Test
    void testUpdateSensor_Success() {
        testSensor.setName("Updated Name");
        when(bureauSensorRepository.findById(1L)).thenReturn(Optional.of(testSensor));
        when(bureauSensorRepository.save(any(BureauSensor.class))).thenReturn(testSensor);

        BureauSensor result = bureauSensorService.updateSensor(1L, testSensor);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        verify(bureauSensorRepository, times(1)).save(any(BureauSensor.class));
    }

    @Test
    void testGetTemperatureHistory() {
        when(temperatureReadingRepository.findLatestBySensorIdAndHours(1L, 24)).thenReturn(new ArrayList<>());

        bureauSensorService.getTemperatureHistory(1L, 24);

        verify(temperatureReadingRepository, times(1)).findLatestBySensorIdAndHours(1L, 24);
    }

    @Test
    void testGetCo2History() {
        when(co2ReadingRepository.findLatestBySensorIdAndHours(1L, 24)).thenReturn(new ArrayList<>());

        bureauSensorService.getCo2History(1L, 24);

        verify(co2ReadingRepository, times(1)).findLatestBySensorIdAndHours(1L, 24);
    }

    @Test
    void testGetOccupancyHistory() {
        when(occupancyStatusRepository.findLatestBySensorIdAndHours(1L, 24)).thenReturn(new ArrayList<>());

        bureauSensorService.getOccupancyHistory(1L, 24);

        verify(occupancyStatusRepository, times(1)).findLatestBySensorIdAndHours(1L, 24);
    }
}
