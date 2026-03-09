import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { Formation, Page } from '../models';

const BASE = environment.apiBaseUrl + '/api/trainings';

@Injectable({ providedIn: 'root' })
export class FormationApiService {
  protected http = inject(HttpClient);

  getAll(opts?: { page?: number; size?: number }): Observable<Page<Formation>> {
    const params = new HttpParams()
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 20));
    return this.http.get<Page<Formation>>(BASE, { params });
  }

  getByEmployee(employeId: number, opts?: { page?: number; size?: number }): Observable<Page<Formation>> {
    const params = new HttpParams()
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 20));
    return this.http.get<Page<Formation>>(BASE + '/byEmployee/' + employeId, { params });
  }

  getById(id: number): Observable<Formation> { return this.http.get<Formation>(BASE + '/' + id); }
  create(body: Partial<Formation>): Observable<Formation> { return this.http.post<Formation>(BASE, body); }
  update(id: number, body: Partial<Formation>): Observable<Formation> { return this.http.put<Formation>(BASE + '/' + id, body); }
}
