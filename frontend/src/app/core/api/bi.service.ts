import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';

const BASE = environment.apiBaseUrl + '/api/attendance';

/**
 * BI & Analytics service
 * Provides business intelligence queries for attendance reporting
 */
@Injectable({ providedIn: 'root' })
export class BiService {
  protected http = inject(HttpClient);

  /**
   * Get daily attendance trends (last N days)
   */
  getDailyTrends(days = 30): Observable<DailyTrendDto[]> {
    const params = new HttpParams().set('days', String(days));
    return this.http.get<DailyTrendDto[]>(`${BASE}/bi/trends/daily`, { params });
  }

  /**
   * Get department-wise attendance statistics
   */
  getDepartmentStats(startDate?: string, endDate?: string): Observable<DepartmentStatDto[]> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    return this.http.get<DepartmentStatDto[]>(`${BASE}/bi/stats/departments`, { params });
  }

  /**
   * Get hourly attendance distribution (peak hours)
   */
  getPeakHours(date?: string): Observable<PeakHourDto[]> {
    const params = date ? new HttpParams().set('date', date) : new HttpParams();
    return this.http.get<PeakHourDto[]>(`${BASE}/bi/analytics/peak-hours`, { params });
  }

  /**
   * Get fraud statistics and metrics
   */
  getFraudMetrics(startDate?: string, endDate?: string): Observable<FraudMetricsDto> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    return this.http.get<FraudMetricsDto>(`${BASE}/bi/metrics/fraud`, { params });
  }

  /**
   * Get employee unreliability scores (attendance consistency)
   */
  getEmployeeReliability(limit = 20): Observable<EmployeeReliabilityDto[]> {
    const params = new HttpParams().set('limit', String(limit));
    return this.http.get<EmployeeReliabilityDto[]>(`${BASE}/bi/analytics/employee-reliability`, { params });
  }

  /**
   * Get attendance summary for date range
   */
  getAttendanceSummary(startDate: string, endDate: string): Observable<AttendanceSummaryMetrics> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<AttendanceSummaryMetrics>(`${BASE}/bi/summary`, { params });
  }

  /**
   * Export attendance report as CSV
   */
  exportCsv(startDate: string, endDate: string): Observable<Blob> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get(`${BASE}/bi/export/csv`, {
      params,
      responseType: 'blob'
    });
  }

  /**
   * Export attendance report as PDF
   */
  exportPdf(startDate: string, endDate: string): Observable<Blob> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get(`${BASE}/bi/export/pdf`, {
      params,
      responseType: 'blob'
    });
  }
}

/**
 * Daily attendance trend DTO
 */
export interface DailyTrendDto {
  date: string;           // YYYY-MM-DD
  checkIns: number;
  checkOuts: number;
  present: number;
  absent: number;
  suspicious: number;
}

/**
 * Department-wise statistics
 */
export interface DepartmentStatDto {
  departmentId: number;
  departmentName: string;
  totalEmployees: number;
  presentToday: number;
  absentToday: number;
  attendanceRate: number;  // 0-100%
  suspiciousCount: number;
}

/**
 * Peak hours analysis
 */
export interface PeakHourDto {
  hour: number;           // 0-23
  checkInCount: number;
  checkOutCount: number;
  suspiciousCount: number;
}

/**
 * Fraud metrics
 */
export interface FraudMetricsDto {
  totalEvents: number;
  suspiciousEvents: number;
  fraudRate: number;                    // % of total events that are suspicious
  unverifiedAlerts: number;
  verifiedFraudCount: number;
  topFraudReasons: FraudReasonCount[];  // Most common fraud reasons
}

/**
 * Fraud reason count
 */
export interface FraudReasonCount {
  reason: string;         // MASK_DETECTED, SPOOFING_DETECTED, etc.
  count: number;
  percentage: number;     // % of total suspicious events
}

/**
 * Employee reliability metrics
 */
export interface EmployeeReliabilityDto {
  employeeId: number;
  employeeName: string;
  departmentName: string;
  attendanceRate: number;           // % of working days present
  inconsistencyScore: number;       // 0-100 (0=perfect, 100=unreliable)
  lastWeekAbsences: number;
  lastMonthFraudAlerts: number;
}

/**
 * Attendance summary for period
 */
export interface AttendanceSummaryMetrics {
  period: {
    startDate: string;
    endDate: string;
    daysCount: number;
  };
  totalCheckIns: number;
  totalCheckOuts: number;
  totalPresent: number;
  totalAbsent: number;
  totalSuspicious: number;
  averageDailyAttendance: number;   // %
  fraudRate: number;                // %
  averageConfidenceScore: number;   // 0-100
}
