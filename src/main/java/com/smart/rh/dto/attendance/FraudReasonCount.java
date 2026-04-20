package com.smart.rh.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Fraud reason count
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudReasonCount {
  private String reason; // MASK_DETECTED, SPOOFING_DETECTED, etc.
  private int count;
  private double percentage; // % of total suspicious events
}
