package com.smart.rh.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Daily attendance trend data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyTrendDto {
  private String date; // YYYY-MM-DD
  private int checkIns;
  private int checkOuts;
  private int present;
  private int absent;
  private int suspicious;
}
