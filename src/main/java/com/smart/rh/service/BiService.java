package com.smart.rh.service;

import com.smart.rh.dto.attendance.*;
import com.smart.rh.entity.Attendance;
import com.smart.rh.entity.AttendanceType;
import com.smart.rh.entity.Employe;
import com.smart.rh.repository.AttendanceRepository;
import com.smart.rh.repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Business Intelligence Service
 * Provides analytics queries for attendance reporting
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BiService {
  private final AttendanceRepository attendanceRepo;
  private final EmployeRepository employeRepo;

  /**
   * Get daily attendance trends for the last N days
   */
  public List<DailyTrendDto> getDailyTrends(int days) {
    LocalDate startDate = LocalDate.now().minusDays(days);
    LocalDate endDate = LocalDate.now();
    ZoneId utc = ZoneId.of("UTC");

    Map<LocalDate, DailyTrendDto> dailyStats = new HashMap<>();

    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
      LocalDate finalDate = date;
      List<Attendance> dayAttendances = attendanceRepo.findAll().stream()
          .filter(a -> a.getClockedAt().atZone(utc).toLocalDate().equals(finalDate))
          .collect(Collectors.toList());

      long checkIns = dayAttendances.stream().filter(a -> a.getType() == AttendanceType.IN).count();
      long checkOuts = dayAttendances.stream().filter(a -> a.getType() == AttendanceType.OUT).count();
      long suspicious = dayAttendances.stream().filter(a -> a.getConfidence() < 70.0).count();

      dailyStats.put(date, DailyTrendDto.builder()
          .date(date.toString())
          .checkIns((int) checkIns)
          .checkOuts((int) checkOuts)
          .present((int) checkIns)
          .absent(0)
          .suspicious((int) suspicious)
          .build());
    }

    return new ArrayList<>(dailyStats.values());
  }

  /**
   * Get department-wise attendance statistics
   */
  public List<DepartmentStatDto> getDepartmentStats(LocalDate startDate, LocalDate endDate) {
    ZoneId utc = ZoneId.of("UTC");
    List<Employe> employees = employeRepo.findAll();

    Map<String, DepartmentStatDto> deptStats = new HashMap<>();

    for (Employe emp : employees) {
      String dept = emp.getPoste() != null ? emp.getPoste().getTitre() : "Unknown";

      deptStats.putIfAbsent(dept, DepartmentStatDto.builder()
          .departmentId(0L)
          .departmentName(dept)
          .totalEmployees(0)
          .presentToday(0)
          .absentToday(0)
          .attendanceRate(0.0)
          .suspiciousCount(0)
          .build());

      DepartmentStatDto stat = deptStats.get(dept);
      stat.setTotalEmployees(stat.getTotalEmployees() + 1);

      // Count present/absent today
      List<Attendance> todayAttendance = attendanceRepo.findAll().stream()
          .filter(a -> a.getEmploye().getId().equals(emp.getId()) &&
              a.getClockedAt().atZone(utc).toLocalDate().equals(LocalDate.now()) &&
              a.getType() == AttendanceType.IN)
          .collect(Collectors.toList());

      if (!todayAttendance.isEmpty()) {
        stat.setPresentToday(stat.getPresentToday() + 1);
      } else {
        stat.setAbsentToday(stat.getAbsentToday() + 1);
      }
    }

    return new ArrayList<>(deptStats.values());
  }

  /**
   * Get peak hours analysis for a specific date
   */
  public List<PeakHourDto> getPeakHours(LocalDate date) {
    if (date == null) {
      date = LocalDate.now();
    }

    Map<Integer, Integer> checkInCounts = new HashMap<>();
    Map<Integer, Integer> checkOutCounts = new HashMap<>();
    Map<Integer, Integer> suspiciousCounts = new HashMap<>();
    LocalDate finalDate = date;
    ZoneId utc = ZoneId.of("UTC");

    attendanceRepo.findAll().stream()
        .filter(a -> a.getClockedAt().atZone(utc).toLocalDate().equals(finalDate))
        .forEach(a -> {
          int hour = a.getClockedAt().atZone(utc).getHour();
          if (a.getType() == AttendanceType.IN) {
            checkInCounts.put(hour, checkInCounts.getOrDefault(hour, 0) + 1);
          } else if (a.getType() == AttendanceType.OUT) {
            checkOutCounts.put(hour, checkOutCounts.getOrDefault(hour, 0) + 1);
          }
          if (a.getConfidence() < 70.0) {
            suspiciousCounts.put(hour, suspiciousCounts.getOrDefault(hour, 0) + 1);
          }
        });

    List<PeakHourDto> result = new ArrayList<>();
    for (int hour = 0; hour < 24; hour++) {
      result.add(PeakHourDto.builder()
          .hour(hour)
          .checkInCount(checkInCounts.getOrDefault(hour, 0))
          .checkOutCount(checkOutCounts.getOrDefault(hour, 0))
          .suspiciousCount(suspiciousCounts.getOrDefault(hour, 0))
          .build());
    }
    return result;
  }

  /**
   * Get fraud statistics and metrics
   */
  public FraudMetricsDto getFraudMetrics(LocalDate startDate, LocalDate endDate) {
    ZoneId utc = ZoneId.of("UTC");

    List<Attendance> attendances = attendanceRepo.findAll().stream()
        .filter(a -> {
          LocalDate attDate = a.getClockedAt().atZone(utc).toLocalDate();
          return !attDate.isBefore(startDate) && !attDate.isAfter(endDate);
        })
        .collect(Collectors.toList());

    long suspicious = attendances.stream()
        .filter(a -> a.getConfidence() < 70.0)
        .count();

    double fraudRate = attendances.isEmpty() ? 0 : (suspicious * 100.0) / attendances.size();

    return FraudMetricsDto.builder()
        .totalEvents(attendances.size())
        .suspiciousEvents((int) suspicious)
        .fraudRate(fraudRate)
        .unverifiedAlerts(0)
        .verifiedFraudCount(0)
        .topFraudReasons(new ArrayList<>())
        .build();
  }

  /**
   * Get employee reliability ranking
   */
  public List<EmployeeReliabilityDto> getEmployeeReliability(int limit) {
    LocalDate now = LocalDate.now();
    LocalDate weekAgo = now.minusDays(7);
    LocalDate monthAgo = now.minusDays(30);
    ZoneId utc = ZoneId.of("UTC");

    List<Employe> employees = employeRepo.findAll();
    List<EmployeeReliabilityDto> reliability = new ArrayList<>();

    for (Employe emp : employees) {
      List<Attendance> allAttendances = attendanceRepo.findAll().stream()
          .filter(a -> a.getEmploye().getId().equals(emp.getId()))
          .collect(Collectors.toList());

      List<Attendance> weekAttendances = allAttendances.stream()
          .filter(a -> {
            LocalDate attDate = a.getClockedAt().atZone(utc).toLocalDate();
            return !attDate.isBefore(weekAgo) && !attDate.isAfter(now) && "IN".equals(a.getType());
          })
          .collect(Collectors.toList());

      long fraudAlertsMonth = allAttendances.stream()
          .filter(a -> {
            LocalDate attDate = a.getClockedAt().atZone(utc).toLocalDate();
            return !attDate.isBefore(monthAgo) && a.getConfidence() < 70.0;
          })
          .count();

      double avgConfidence = allAttendances.stream()
          .mapToDouble(a -> a.getConfidence() != null ? a.getConfidence() : 0)
          .average()
          .orElse(0.0);

      double attendanceRate = weekAttendances.size() > 0 ? (weekAttendances.size() * 100.0 / 7.0) : 0.0;
      double inconsistencyScore = 100.0 - avgConfidence;

      reliability.add(EmployeeReliabilityDto.builder()
          .employeeId(emp.getId())
          .employeeName(emp.getNom() + " " + emp.getPrenom())
          .departmentName(emp.getPoste() != null ? emp.getPoste().getTitre() : "Unknown")
          .attendanceRate(attendanceRate)
          .inconsistencyScore(inconsistencyScore)
          .lastWeekAbsences(7 - weekAttendances.size())
          .lastMonthFraudAlerts((int) fraudAlertsMonth)
          .build());
    }

    return reliability.stream()
        .sorted(Comparator.comparingDouble(EmployeeReliabilityDto::getAttendanceRate).reversed())
        .limit(limit)
        .collect(Collectors.toList());
  }

  /**
   * Get overall attendance summary metrics
   */
  public AttendanceSummaryMetricsDto getAttendanceSummary(LocalDate startDate, LocalDate endDate) {
    ZoneId utc = ZoneId.of("UTC");

    List<Attendance> attendances = attendanceRepo.findAll().stream()
        .filter(a -> {
          LocalDate attDate = a.getClockedAt().atZone(utc).toLocalDate();
          return !attDate.isBefore(startDate) && !attDate.isAfter(endDate);
        })
        .collect(Collectors.toList());

    long checkIns = attendances.stream().filter(a -> a.getType() == AttendanceType.IN).count();
    long checkOuts = attendances.stream().filter(a -> a.getType() == AttendanceType.OUT).count();
    long suspicious = attendances.stream().filter(a -> a.getConfidence() < 70.0).count();

    double avgConfidence = attendances.stream()
        .mapToDouble(a -> a.getConfidence() != null ? a.getConfidence() : 0)
        .average()
        .orElse(0.0);

    double fraudRate = attendances.isEmpty() ? 0 : (suspicious * 100.0) / attendances.size();
    long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;

    return AttendanceSummaryMetricsDto.builder()
        .period(PeriodDto.builder()
            .startDate(startDate.toString())
            .endDate(endDate.toString())
            .daysCount((int) daysBetween)
            .build())
        .totalCheckIns((int) checkIns)
        .totalCheckOuts((int) checkOuts)
        .totalPresent((int) checkIns)
        .totalAbsent(0)
        .totalSuspicious((int) suspicious)
        .averageDailyAttendance(daysBetween > 0 ? (checkIns * 100.0) / daysBetween : 0)
        .fraudRate(fraudRate)
        .averageConfidenceScore(avgConfidence)
        .build();
  }
}
