import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { Employe, Page } from '../models';

const BASE = environment.apiBaseUrl + '/api/employees';

@Injectable({ providedIn: 'root' })
export class EmployeApiService {
  protected http = inject(HttpClient);

  /** Returns the employee record linked to the currently authenticated user. */
  getMe(): Observable<Employe> {
    return this.http.get<Employe>(BASE + '/me');
  }

  getAll(opts?: { page?: number; size?: number; search?: string }): Observable<Page<Employe>> {
    let params = new HttpParams()
      .set('page', String(opts?.page  ?? 0))
      .set('size', String(opts?.size  ?? 20));
    if (opts?.search) params = params.set('search', opts.search);
    return this.http.get<Page<Employe>>(BASE, { params });
  }

  getById(id: number): Observable<Employe>  { return this.http.get<Employe>(BASE + '/' + id); }
  create(body: Partial<Employe>): Observable<Employe>               { return this.http.post<Employe>(BASE, body); }
  update(id: number, body: Partial<Employe>): Observable<Employe>   { return this.http.put<Employe>(BASE + '/' + id, body); }
  delete(id: number): Observable<void>                              { return this.http.delete<void>(BASE + '/' + id); }
}
