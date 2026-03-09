import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { Evaluation, Page } from '../models';

const BASE = environment.apiBaseUrl + '/api/evaluations';

@Injectable({ providedIn: 'root' })
export class EvaluationApiService {
  protected http = inject(HttpClient);

  getAll(opts?: { page?: number; size?: number }): Observable<Page<Evaluation>> {
    const params = new HttpParams()
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 20));
    return this.http.get<Page<Evaluation>>(BASE, { params });
  }

  getByEmployee(employeId: number, opts?: { page?: number; size?: number }): Observable<Page<Evaluation>> {
    const params = new HttpParams()
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 20));
    return this.http.get<Page<Evaluation>>(BASE + '/byEmployee/' + employeId, { params });
  }

  getById(id: number): Observable<Evaluation> { return this.http.get<Evaluation>(BASE + '/' + id); }
  create(body: Partial<Evaluation>): Observable<Evaluation> { return this.http.post<Evaluation>(BASE, body); }
  update(id: number, body: Partial<Evaluation>): Observable<Evaluation> { return this.http.put<Evaluation>(BASE + '/' + id, body); }
}
