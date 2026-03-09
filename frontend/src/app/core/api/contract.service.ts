import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { Contrat, Page } from '../models';

const BASE = environment.apiBaseUrl + '/api/contracts';

@Injectable({ providedIn: 'root' })
export class ContratApiService {
  protected http = inject(HttpClient);

  getAll(opts?: { page?: number; size?: number }): Observable<Page<Contrat>> {
    const params = new HttpParams()
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 20));
    return this.http.get<Page<Contrat>>(BASE, { params });
  }

  getByEmployee(employeId: number, opts?: { page?: number; size?: number }): Observable<Page<Contrat>> {
    const params = new HttpParams()
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 20));
    return this.http.get<Page<Contrat>>(BASE + '/byEmployee/' + employeId, { params });
  }

  getById(id: number): Observable<Contrat> { return this.http.get<Contrat>(BASE + '/' + id); }
  create(body: Partial<Contrat>): Observable<Contrat> { return this.http.post<Contrat>(BASE, body); }
  update(id: number, body: Partial<Contrat>): Observable<Contrat> { return this.http.put<Contrat>(BASE + '/' + id, body); }
}
