import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

const BASE = environment.apiBaseUrl + '/api/attendance';

/**
 * Facial recognition attendance event API service
 * Provides access to real-time attendance data with fraud detection
 */
@Injectable({ providedIn: 'root' })
export class AttendanceEventService {
  protected http = inject(HttpClient);

  /**
   * Get recent attendance events for dashboard (real-time feed)
   */
  getRecentEvents(page = 0, size = 50): Observable<Page<AttendanceEventDto>> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));
    return this.http.get<Page<AttendanceEventDto>>(`${BASE}/recent`, { params });
  }

  /**
   * Get attendance history for a specific employee
   */
  getEmployeeHistory(employeeId: number, page = 0, size = 30): Observable<Page<AttendanceEventDto>> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));
    return this.http.get<Page<AttendanceEventDto>>(
      `${BASE}/employee/${employeeId}`,
      { params }
    );
  }

  /**
   * Get today's attendance for an employee
   */
  getTodayAttendance(employeeId: number): Observable<AttendanceEventDto[]> {
    return this.http.get<AttendanceEventDto[]>(`${BASE}/employee/${employeeId}/today`);
  }

  /**
   * Get current status for an employee (clocked in/out)
   */
  getCurrentStatus(employeeId: number): Observable<{ status: string }> {
    return this.http.get<{ status: string }>(`${BASE}/employee/${employeeId}/current-status`);
  }

  /**
   * Check if employee is currently on-site
   */
  isEmployeePresent(employeeId: number): Observable<{ isPresent: boolean }> {
    return this.http.get<{ isPresent: boolean }>(`${BASE}/employee/${employeeId}/is-present`);
  }

  /**
   * Get unverified fraud alerts (for HR investigation)
   */
  getUnverifiedFraudAlerts(): Observable<AttendanceEventDto[]> {
    return this.http.get<AttendanceEventDto[]>(`${BASE}/fraud/unverified`);
  }

  /**
   * Get suspicious attendance events
   */
  getSuspiciousEvents(page = 0, size = 30): Observable<Page<AttendanceEventDto>> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));
    return this.http.get<Page<AttendanceEventDto>>(`${BASE}/suspicious`, { params });
  }

  /**
   * Verify a suspicious attendance event (HR confirmation)
   */
  verifyAttendanceEvent(eventId: number, isValid: boolean, notes: string): Observable<void> {
    return this.http.post<void>(`${BASE}/${eventId}/verify`, { valid: isValid, notes });
  }

  /**
   * Get today's attendance summary (dashboard widget)
   */
  getTodaySummary(): Observable<AttendanceSummary> {
    return this.http.get<AttendanceSummary>(`${BASE}/summary/today`);
  }

  /**
   * Get attendance events from a specific camera/device
   */
  getDeviceEvents(deviceId: number, page = 0, size = 30): Observable<Page<AttendanceEventDto>> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));
    return this.http.get<Page<AttendanceEventDto>>(`${BASE}/device/${deviceId}`, { params });
  }
}

/**
 * DTO for paginated response
 */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

/**
 * Facial recognition attendance event DTO
 */
export interface AttendanceEventDto {
  id: number;
  employeeId: number;
  employeeName: string;
  deviceId: number;
  deviceName: string;
  eventType: 'IN' | 'OUT' | 'SUSPICIOUS';
  eventTimestamp: string;  // ISO 8601
  confidenceScore: number;  // 0-100
  isFraudSuspected: boolean;
  fraudReason: string | null;
  photoData: string | null;  // Base64 or path
  processingStatus: 'PENDING' | 'PROCESSED' | 'ERROR';
  processingTimeMs: number;
  manuallyVerified: boolean | null;
  verifiedAt: string | null;
  verificationNotes: string | null;
  createdAt: string;
}

/**
 * Today's attendance summary for dashboard
 */
export interface AttendanceSummary {
  checkIns: number;
  checkOuts: number;
  suspicious: number;
  present: number;
}
