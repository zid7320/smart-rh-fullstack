package com.smart.rh.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Department-wise attendance statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentStatDto {
  private long departmentId;
  private String departmentName;
  private int totalEmployees;
  private int presentToday;
  private int absentToday;
  private double attendanceRate; // 0-100%
  private int suspiciousCount;
}
