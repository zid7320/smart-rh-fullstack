import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { Paie, Page } from '../models';

const BASE = environment.apiBaseUrl + '/api/payroll';

@Injectable({ providedIn: 'root' })
export class PaieApiService {
  protected http = inject(HttpClient);

  getAll(opts?: { page?: number; size?: number }): Observable<Page<Paie>> {
    const params = new HttpParams()
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 20));
    return this.http.get<Page<Paie>>(BASE, { params });
  }

  getByEmployee(employeId: number, opts?: { page?: number; size?: number }): Observable<Page<Paie>> {
    const params = new HttpParams()
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 12));
    return this.http.get<Page<Paie>>(BASE + '/byEmployee/' + employeId, { params });
  }

  generate(body: { mois: number; annee: number }): Observable<Paie[]> {
    return this.http.post<Paie[]>(BASE + '/generate', body);
  }

  getById(id: number): Observable<Paie> {
    return this.http.get<Paie>(BASE + '/' + id);
  }

  downloadPdf(id: number): Observable<Blob> {
    return this.http.get(BASE + '/' + id + '/pdf', { responseType: 'blob' });
  }
}
