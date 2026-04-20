package com.smart.rh.dto.sensors;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for bureau dashboard - composite view of all sensor data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BureauDashboardDto {

    @JsonProperty("sensor")
    private BureauSensorDto sensor;

    @JsonProperty("latestTemperature")
    private TemperatureReadingDto latestTemperature;

    @JsonProperty("latestCo2")
    private Co2ReadingDto latestCo2;

    @JsonProperty("latestOccupancy")
    private OccupancyStatusDto latestOccupancy;

    @JsonProperty("health")
    private SensorHealthDto health;

    @JsonProperty("activeAlerts")
    private List<SensorAlertDto> activeAlerts;

    @JsonProperty("temperatureHistory")
    private List<TemperatureReadingDto> temperatureHistory;

    @JsonProperty("co2History")
    private List<Co2ReadingDto> co2History;
}
