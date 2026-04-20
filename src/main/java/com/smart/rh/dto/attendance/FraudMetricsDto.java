package com.smart.rh.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Fraud metrics and statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudMetricsDto {
  private int totalEvents;
  private int suspiciousEvents;
  private double fraudRate; // % of total events that are suspicious
  private int unverifiedAlerts;
  private int verifiedFraudCount;
  private List<FraudReasonCount> topFraudReasons; // Most common fraud reasons
}
