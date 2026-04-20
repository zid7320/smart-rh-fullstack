package com.smart.rh.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.smart.rh.dto.attendance.*;
import com.smart.rh.entity.AttendanceEvent;
import com.smart.rh.repository.AttendanceEventRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Export Service
 * Handles CSV and PDF generation for attendance and analytics reports
 */
@Service
@RequiredArgsConstructor
public class ExportService {
  private final AttendanceEventRepository attendanceRepo;
  private final BiService biService;

  /**
   * Export attendance summary as CSV
   */
  public byte[] exportAttendanceSummaryAsCSV(LocalDate startDate, LocalDate endDate) throws IOException {
    AttendanceSummaryMetricsDto summary = biService.getAttendanceSummary(startDate, endDate);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    OutputStreamWriter writer = new OutputStreamWriter(out);

    CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(
        "Metric", "Value");

    try (CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
      printer.printRecord("Report Period", startDate + " to " + endDate);
      printer.printRecord("Total Days", summary.getPeriod().getDaysCount());
      printer.printRecord("");

      printer.printRecord("Check-ins", summary.getTotalCheckIns());
      printer.printRecord("Check-outs", summary.getTotalCheckOuts());
      printer.printRecord("Present", summary.getTotalPresent());
      printer.printRecord("Absent", summary.getTotalAbsent());
      printer.printRecord("Suspicious Events", summary.getTotalSuspicious());
      printer.printRecord("");

      printer.printRecord("Avg Daily Attendance (%)", String.format("%.2f", summary.getAverageDailyAttendance()));
      printer.printRecord("Fraud Rate (%)", String.format("%.2f", summary.getFraudRate()));
      printer.printRecord("Avg Confidence Score", String.format("%.2f", summary.getAverageConfidenceScore()));

      writer.flush();
    }

    return out.toByteArray();
  }

  /**
   * Export daily trends as CSV
   */
  public byte[] exportDailyTrendsAsCSV(int days) throws IOException {
    List<DailyTrendDto> trends = biService.getDailyTrends(days);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    OutputStreamWriter writer = new OutputStreamWriter(out);

    CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(
        "Date", "Check-ins", "Check-outs", "Present", "Absent", "Suspicious");

    try (CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
      for (DailyTrendDto trend : trends) {
        printer.printRecord(
            trend.getDate(),
            trend.getCheckIns(),
            trend.getCheckOuts(),
            trend.getPresent(),
            trend.getAbsent(),
            trend.getSuspicious());
      }
      writer.flush();
    }

    return out.toByteArray();
  }

  /**
   * Export fraud metrics as CSV
   */
  public byte[] exportFraudMetricsAsCSV(LocalDate startDate, LocalDate endDate) throws IOException {
    FraudMetricsDto metrics = biService.getFraudMetrics(startDate, endDate);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    OutputStreamWriter writer = new OutputStreamWriter(out);

    CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(
        "Metric", "Value");

    try (CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
      printer.printRecord("Report Period", startDate + " to " + endDate);
      printer.printRecord("");

      printer.printRecord("Total Events", metrics.getTotalEvents());
      printer.printRecord("Suspicious Events", metrics.getSuspiciousEvents());
      printer.printRecord("Fraud Rate (%)", String.format("%.2f", metrics.getFraudRate()));
      printer.printRecord("Unverified Alerts", metrics.getUnverifiedAlerts());
      printer.printRecord("Verified Fraud Count", metrics.getVerifiedFraudCount());
      printer.printRecord("");

      printer.printRecord("Top Fraud Reasons");
      printer.printRecord("Reason", "Count", "Percentage (%)");
      for (FraudReasonCount reason : metrics.getTopFraudReasons()) {
        printer.printRecord(
            reason.getReason(),
            reason.getCount(),
            String.format("%.2f", reason.getPercentage()));
      }

      writer.flush();
    }

    return out.toByteArray();
  }

  /**
   * Export attendance summary as PDF
   */
  public byte[] exportAttendanceSummaryAsPDF(LocalDate startDate, LocalDate endDate)
      throws DocumentException, IOException {
    AttendanceSummaryMetricsDto summary = biService.getAttendanceSummary(startDate, endDate);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Document document = new Document(PageSize.A4, 50, 50, 50, 50);
    PdfWriter.getInstance(document, out);

    document.open();

    // Title
    Paragraph title = new Paragraph("Attendance Summary Report", new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD));
    title.setAlignment(Element.ALIGN_CENTER);
    document.add(title);

    // Period
    Paragraph period = new Paragraph(
        "Period: " + startDate + " to " + endDate,
        new Font(Font.FontFamily.HELVETICA, 12));
    period.setAlignment(Element.ALIGN_CENTER);
    document.add(period);

    document.add(new Paragraph(" ")); // Spacing

    // Summary Table
    PdfPTable table = new PdfPTable(2);
    table.setWidthPercentage(100);

    addTableHeader(table, "Metric", "Value");

    addTableRow(table, "Total Check-ins", String.valueOf(summary.getTotalCheckIns()));
    addTableRow(table, "Total Check-outs", String.valueOf(summary.getTotalCheckOuts()));
    addTableRow(table, "Present", String.valueOf(summary.getTotalPresent()));
    addTableRow(table, "Absent", String.valueOf(summary.getTotalAbsent()));
    addTableRow(table, "Suspicious Events", String.valueOf(summary.getTotalSuspicious()));
    addTableRow(table, "Avg Daily Attendance (%)", String.format("%.2f", summary.getAverageDailyAttendance()));
    addTableRow(table, "Fraud Rate (%)", String.format("%.2f", summary.getFraudRate()));
    addTableRow(table, "Avg Confidence Score", String.format("%.2f", summary.getAverageConfidenceScore()));

    document.add(table);

    // Footer
    document.add(new Paragraph(" ")); // Spacing
    Paragraph footer = new Paragraph(
        "Generated on: " + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        new Font(Font.FontFamily.HELVETICA, 10));
    footer.setAlignment(Element.ALIGN_RIGHT);
    document.add(footer);

    document.close();

    return out.toByteArray();
  }

  /**
   * Export fraud metrics as PDF
   */
  public byte[] exportFraudMetricsAsPDF(LocalDate startDate, LocalDate endDate) throws DocumentException, IOException {
    FraudMetricsDto metrics = biService.getFraudMetrics(startDate, endDate);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Document document = new Document(PageSize.A4, 50, 50, 50, 50);
    PdfWriter.getInstance(document, out);

    document.open();

    // Title
    Paragraph title = new Paragraph("Fraud Metrics Report", new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD));
    title.setAlignment(Element.ALIGN_CENTER);
    document.add(title);

    // Period
    Paragraph period = new Paragraph(
        "Period: " + startDate + " to " + endDate,
        new Font(Font.FontFamily.HELVETICA, 12));
    period.setAlignment(Element.ALIGN_CENTER);
    document.add(period);

    document.add(new Paragraph(" ")); // Spacing

    // Metrics Table
    PdfPTable metricsTable = new PdfPTable(2);
    metricsTable.setWidthPercentage(100);

    addTableHeader(metricsTable, "Metric", "Value");
    addTableRow(metricsTable, "Total Events", String.valueOf(metrics.getTotalEvents()));
    addTableRow(metricsTable, "Suspicious Events", String.valueOf(metrics.getSuspiciousEvents()));
    addTableRow(metricsTable, "Fraud Rate (%)", String.format("%.2f", metrics.getFraudRate()));
    addTableRow(metricsTable, "Unverified Alerts", String.valueOf(metrics.getUnverifiedAlerts()));
    addTableRow(metricsTable, "Verified Fraud Count", String.valueOf(metrics.getVerifiedFraudCount()));

    document.add(metricsTable);

    // Fraud Reasons Section
    document.add(new Paragraph(" ")); // Spacing
    Paragraph reasonsTitle = new Paragraph("Top Fraud Reasons", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
    document.add(reasonsTitle);

    PdfPTable reasonsTable = new PdfPTable(3);
    reasonsTable.setWidthPercentage(100);

    addTableHeader(reasonsTable, "Reason", "Count", "Percentage (%)");
    for (FraudReasonCount reason : metrics.getTopFraudReasons()) {
      addTableRow(
          reasonsTable,
          reason.getReason(),
          String.valueOf(reason.getCount()),
          String.format("%.2f", reason.getPercentage()));
    }

    document.add(reasonsTable);

    // Footer
    document.add(new Paragraph(" ")); // Spacing
    Paragraph footer = new Paragraph(
        "Generated on: " + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        new Font(Font.FontFamily.HELVETICA, 10));
    footer.setAlignment(Element.ALIGN_RIGHT);
    document.add(footer);

    document.close();

    return out.toByteArray();
  }

  /**
   * Export daily trends as PDF
   */
  public byte[] exportDailyTrendsAsPDF(int days) throws DocumentException, IOException {
    List<DailyTrendDto> trends = biService.getDailyTrends(days);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Document document = new Document(PageSize.A4, 50, 50, 50, 50);
    PdfWriter.getInstance(document, out);

    document.open();

    // Title
    Paragraph title = new Paragraph("Daily Trends Report", new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD));
    title.setAlignment(Element.ALIGN_CENTER);
    document.add(title);

    // Period
    Paragraph period = new Paragraph(
        "Last " + days + " days",
        new Font(Font.FontFamily.HELVETICA, 12));
    period.setAlignment(Element.ALIGN_CENTER);
    document.add(period);

    document.add(new Paragraph(" ")); // Spacing

    // Trends Table
    PdfPTable table = new PdfPTable(6);
    table.setWidthPercentage(100);

    addTableHeader(table, "Date", "Check-ins", "Check-outs", "Present", "Absent", "Suspicious");

    for (DailyTrendDto trend : trends) {
      addTableRow(
          table,
          trend.getDate(),
          String.valueOf(trend.getCheckIns()),
          String.valueOf(trend.getCheckOuts()),
          String.valueOf(trend.getPresent()),
          String.valueOf(trend.getAbsent()),
          String.valueOf(trend.getSuspicious()));
    }

    document.add(table);

    // Footer
    document.add(new Paragraph(" ")); // Spacing
    Paragraph footer = new Paragraph(
        "Generated on: " + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        new Font(Font.FontFamily.HELVETICA, 10));
    footer.setAlignment(Element.ALIGN_RIGHT);
    document.add(footer);

    document.close();

    return out.toByteArray();
  }

  /**
   * Helper: Add table header
   */
  private void addTableHeader(PdfPTable table, String... headers) {
    for (String header : headers) {
      PdfPCell cell = new PdfPCell(new Phrase(header, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
      cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
      cell.setPadding(5);
      table.addCell(cell);
    }
  }

  /**
   * Helper: Add table row
   */
  private void addTableRow(PdfPTable table, String... values) {
    for (String value : values) {
      PdfPCell cell = new PdfPCell(new Phrase(value, new Font(Font.FontFamily.HELVETICA, 11)));
      cell.setPadding(5);
      table.addCell(cell);
    }
  }
}
