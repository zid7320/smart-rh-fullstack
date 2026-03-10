import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { Planning, Page } from '../models';

const BASE = environment.apiBaseUrl + '/api/planning';

@Injectable({ providedIn: 'root' })
export class PlanningApiService {
  private http = inject(HttpClient);

  getAll(opts?: { page?: number; size?: number }): Observable<Page<Planning>> {
    const params = new HttpParams()
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 20));
    return this.http.get<Page<Planning>>(BASE, { params });
  }

  getByEmployee(employeId: number, opts?: { page?: number; size?: number }): Observable<Page<Planning>> {
    const params = new HttpParams()
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 20));
    return this.http.get<Page<Planning>>(`${BASE}/byEmployee/${employeId}`, { params });
  }

  getById(id: number): Observable<Planning> {
    return this.http.get<Planning>(`${BASE}/${id}`);
  }

  create(body: Partial<Planning>): Observable<Planning> {
    return this.http.post<Planning>(BASE, body);
  }

  update(id: number, body: Partial<Planning>): Observable<Planning> {
    return this.http.put<Planning>(`${BASE}/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE}/${id}`);
  }
}
