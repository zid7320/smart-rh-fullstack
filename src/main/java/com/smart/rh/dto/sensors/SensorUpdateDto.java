package com.smart.rh.dto.sensors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smart.rh.entity.BureauSensor;
import com.smart.rh.entity.TemperatureReading;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for sensor updates via WebSocket to frontend
 * Wraps the latest reading with sensor metadata for real-time dashboard updates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorUpdateDto {

    @JsonProperty("sensor")
    private SensorInfoDto sensor;

    @JsonProperty("latestTemperature")
    private TemperatureReadingDto latestTemperature;

    @JsonProperty("latestCo2")
    private Co2EventDto latestCo2;

    @JsonProperty("latestOccupancy")
    private OccupancyEventDto latestOccupancy;

    @JsonProperty("temperatureHistory")
    private List<TemperatureReadingDto> temperatureHistory = List.of();

    @JsonProperty("co2History")
    private List<Co2EventDto> co2History = List.of();

    @JsonProperty("occupancyHistory")
    private List<OccupancyEventDto> occupancyHistory = List.of();

    /**
     * Minimal sensor info for WebSocket updates
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SensorInfoDto {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("deviceId")
        private String deviceId;

        @JsonProperty("name")
        private String name;

        @JsonProperty("location")
        private String location;

        @JsonProperty("sensorTypes")
        private String sensorTypes;

        @JsonProperty("isActive")
        private Boolean isActive;
    }
}
