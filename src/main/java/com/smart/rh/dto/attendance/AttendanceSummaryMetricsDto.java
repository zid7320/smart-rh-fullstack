package com.smart.rh.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Attendance summary metrics for period
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryMetricsDto {
  private PeriodDto period;
  private int totalCheckIns;
  private int totalCheckOuts;
  private int totalPresent;
  private int totalAbsent;
  private int totalSuspicious;
  private double averageDailyAttendance; // %
  private double fraudRate; // %
  private double averageConfidenceScore; // 0-100
}
