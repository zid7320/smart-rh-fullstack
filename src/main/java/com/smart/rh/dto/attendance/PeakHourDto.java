package com.smart.rh.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Peak hours analysis data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeakHourDto {
  private int hour; // 0-23
  private int checkInCount;
  private int checkOutCount;
  private int suspiciousCount;
}
