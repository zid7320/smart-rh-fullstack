import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { AttendanceRecord, Page } from '../models';

const BASE = environment.apiBaseUrl + '/api/attendance';

@Injectable({ providedIn: 'root' })
export class AttendanceApiService {
  protected http = inject(HttpClient);

  getHistory(opts?: { page?: number; size?: number }): Observable<Page<AttendanceRecord>> {
    const params = new HttpParams()
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 20));
    return this.http.get<Page<AttendanceRecord>>(BASE + '/history', { params });
  }

  getByEmployee(employeId: number, opts?: { page?: number; size?: number }): Observable<Page<AttendanceRecord>> {
    const params = new HttpParams()
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 20));
    return this.http.get<Page<AttendanceRecord>>(BASE + '/byEmployee/' + employeId, { params });
  }

  /** Trigger a manual / simulated attendance event (ADMIN/RH only). */
  recognize(body: {
    employeId:   number;
    type:        'IN' | 'OUT';
    confidence?: number;
    clockedAt?:  string;
    cameraId?:   string;
    siteId?:     string;
  }): Observable<AttendanceRecord> {
    return this.http.post<AttendanceRecord>(BASE + '/recognition', body);
  }
}
