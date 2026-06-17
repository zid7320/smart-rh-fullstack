package com.smart.rh.controller;

import com.smart.rh.dto.attendance.*;
import com.smart.rh.service.BiService;
import com.smart.rh.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * BI & Analytics REST Controller
 * Provides attendance reporting and analytics endpoints
 */
@RestController
@RequestMapping("/api/attendance/bi")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'RH')")
public class BiController {
  private final BiService biService;
  private final ExportService exportService;

  /**
   * Get daily attendance trends
   * GET /api/attendance/bi/trends/daily?days=30
   */
  @GetMapping("/trends/daily")
  public ResponseEntity<List<DailyTrendDto>> getDailyTrends(
      @RequestParam(defaultValue = "30") int days) {
    return ResponseEntity.ok(biService.getDailyTrends(days));
  }

  /**
   * Get department-wise attendance statistics
   * GET
   * /api/attendance/bi/stats/departments?startDate=2026-01-01&endDate=2026-02-01
   */
  @GetMapping("/stats/departments")
  public ResponseEntity<List<DepartmentStatDto>> getDepartmentStats(
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {

    LocalDate start = (startDate == null || startDate.isBlank()) ? LocalDate.now().minusMonths(1) : LocalDate.parse(startDate);
    LocalDate end = (endDate == null || endDate.isBlank()) ? LocalDate.now() : LocalDate.parse(endDate);

    return ResponseEntity.ok(biService.getDepartmentStats(start, end));
  }

  /**
   * Get hourly attendance distribution (peak hours)
   * GET /api/attendance/bi/analytics/peak-hours?date=2026-04-20
   */
  @GetMapping("/analytics/peak-hours")
  public ResponseEntity<List<PeakHourDto>> getPeakHours(
      @RequestParam(required = false) String date) {
    LocalDate dateParam = (date == null || date.isBlank()) ? LocalDate.now() : LocalDate.parse(date);
    return ResponseEntity.ok(biService.getPeakHours(dateParam));
  }

  /**
   * Get fraud statistics and metrics
   * GET /api/attendance/bi/metrics/fraud?startDate=2026-01-01&endDate=2026-02-01
   */
  @GetMapping("/metrics/fraud")
  public ResponseEntity<FraudMetricsDto> getFraudMetrics(
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {

    LocalDate start = (startDate == null || startDate.isBlank()) ? LocalDate.now().minusMonths(1) : LocalDate.parse(startDate);
    LocalDate end = (endDate == null || endDate.isBlank()) ? LocalDate.now() : LocalDate.parse(endDate);

    return ResponseEntity.ok(biService.getFraudMetrics(start, end));
  }

  /**
   * Get employee unreliability scores
   * GET /api/attendance/bi/analytics/employee-reliability?limit=20
   */
  @GetMapping("/analytics/employee-reliability")
  public ResponseEntity<List<EmployeeReliabilityDto>> getEmployeeReliability(
      @RequestParam(defaultValue = "20") int limit) {
    return ResponseEntity.ok(biService.getEmployeeReliability(limit));
  }

  /**
   * Get attendance summary for date range
   * GET /api/attendance/bi/summary?startDate=2026-01-01&endDate=2026-02-01
   */
  @GetMapping("/summary")
  public ResponseEntity<AttendanceSummaryMetricsDto> getAttendanceSummary(
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {
    LocalDate start = (startDate == null || startDate.isBlank()) ? LocalDate.now().minusMonths(1) : LocalDate.parse(startDate);
    LocalDate end = (endDate == null || endDate.isBlank()) ? LocalDate.now() : LocalDate.parse(endDate);
    return ResponseEntity.ok(biService.getAttendanceSummary(start, end));
  }

  /**
   * Export attendance report as CSV
   * GET /api/attendance/bi/export/csv?startDate=2026-01-01&endDate=2026-02-01
   */
  @GetMapping("/export/csv")
  public ResponseEntity<byte[]> exportCsv(
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {
    try {
      LocalDate start = (startDate == null || startDate.isBlank()) ? LocalDate.now().minusMonths(1) : LocalDate.parse(startDate);
      LocalDate end = (endDate == null || endDate.isBlank()) ? LocalDate.now() : LocalDate.parse(endDate);
      byte[] csvData = exportService.exportAttendanceSummaryAsCSV(start, end);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.TEXT_PLAIN);
      headers.setContentDispositionFormData("attachment", "attendance_report_" + start + "_" + end + ".csv");
      return ResponseEntity.ok().headers(headers).body(csvData);
    } catch (Exception e) {
      return ResponseEntity.status(500).build();
    }
  }

  /**
   * Export attendance report as PDF
   * GET /api/attendance/bi/export/pdf?startDate=2026-01-01&endDate=2026-02-01
   */
  @GetMapping("/export/pdf")
  public ResponseEntity<byte[]> exportPdf(
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {
    try {
      LocalDate start = (startDate == null || startDate.isBlank()) ? LocalDate.now().minusMonths(1) : LocalDate.parse(startDate);
      LocalDate end = (endDate == null || endDate.isBlank()) ? LocalDate.now() : LocalDate.parse(endDate);
      byte[] pdfData = exportService.exportAttendanceSummaryAsPDF(start, end);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      headers.setContentDispositionFormData("attachment", "attendance_report_" + start + "_" + end + ".pdf");
      return ResponseEntity.ok().headers(headers).body(pdfData);
    } catch (Exception e) {
      return ResponseEntity.status(500).build();
    }
  }

  /**
   * Export fraud metrics as CSV
   * GET /api/attendance/bi/export/fraud-csv?startDate=2026-01-01&endDate=2026-02-01
   */
  @GetMapping("/export/fraud-csv")
  public ResponseEntity<byte[]> exportFraudCsv(
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {
    try {
      LocalDate start = (startDate == null || startDate.isBlank()) ? LocalDate.now().minusMonths(1) : LocalDate.parse(startDate);
      LocalDate end = (endDate == null || endDate.isBlank()) ? LocalDate.now() : LocalDate.parse(endDate);
      byte[] csvData = exportService.exportFraudMetricsAsCSV(start, end);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.TEXT_PLAIN);
      headers.setContentDispositionFormData("attachment", "fraud_report_" + start + "_" + end + ".csv");
      return ResponseEntity.ok().headers(headers).body(csvData);
    } catch (Exception e) {
      return ResponseEntity.status(500).build();
    }
  }

  /**
   * Export fraud metrics as PDF
   * GET /api/attendance/bi/export/fraud-pdf?startDate=2026-01-01&endDate=2026-02-01
   */
  @GetMapping("/export/fraud-pdf")
  public ResponseEntity<byte[]> exportFraudPdf(
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {
    try {
      LocalDate start = (startDate == null || startDate.isBlank()) ? LocalDate.now().minusMonths(1) : LocalDate.parse(startDate);
      LocalDate end = (endDate == null || endDate.isBlank()) ? LocalDate.now() : LocalDate.parse(endDate);
      byte[] pdfData = exportService.exportFraudMetricsAsPDF(start, end);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      headers.setContentDispositionFormData("attachment", "fraud_report_" + start + "_" + end + ".pdf");
      return ResponseEntity.ok().headers(headers).body(pdfData);
    } catch (Exception e) {
      return ResponseEntity.status(500).build();
    }
  }

  /**
   * Export daily trends as CSV
   * GET /api/attendance/bi/export/trends-csv?days=30
   */
  @GetMapping("/export/trends-csv")
  public ResponseEntity<byte[]> exportTrendsCsv(
      @RequestParam(defaultValue = "30") int days) {
    try {
      byte[] csvData = exportService.exportDailyTrendsAsCSV(days);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.TEXT_PLAIN);
      headers.setContentDispositionFormData("attachment", "daily_trends_" + days + "d.csv");
      return ResponseEntity.ok().headers(headers).body(csvData);
    } catch (Exception e) {
      return ResponseEntity.status(500).build();
    }
  }

  /**
   * Export daily trends as PDF
   * GET /api/attendance/bi/export/trends-pdf?days=30
   */
  @GetMapping("/export/trends-pdf")
  public ResponseEntity<byte[]> exportTrendsPdf(
      @RequestParam(defaultValue = "30") int days) {
    try {
      byte[] pdfData = exportService.exportDailyTrendsAsPDF(days);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      headers.setContentDispositionFormData("attachment", "daily_trends_" + days + "d.pdf");
      return ResponseEntity.ok().headers(headers).body(pdfData);
    } catch (Exception e) {
      return ResponseEntity.status(500).build();
    }
  }
}
