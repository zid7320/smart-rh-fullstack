package com.smart.rh.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Employee reliability metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeReliabilityDto {
  private long employeeId;
  private String employeeName;
  private String departmentName;
  private double attendanceRate; // % of working days present
  private double inconsistencyScore; // 0-100 (0=perfect, 100=unreliable)
  private int lastWeekAbsences;
  private int lastMonthFraudAlerts;
}
