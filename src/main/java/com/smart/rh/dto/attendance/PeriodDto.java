package com.smart.rh.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Period information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodDto {
  private String startDate;
  private String endDate;
  private int daysCount;
}
