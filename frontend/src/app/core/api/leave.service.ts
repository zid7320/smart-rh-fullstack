import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { Conge, Page } from '../models';

const BASE = environment.apiBaseUrl + '/api/leaves';

@Injectable({ providedIn: 'root' })
export class CongeApiService {
  protected http = inject(HttpClient);

  getAll(opts?: { page?: number; size?: number }): Observable<Page<Conge>> {
    const params = new HttpParams()
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 20));
    return this.http.get<Page<Conge>>(BASE, { params });
  }

  getByEmployee(employeId: number, opts?: { page?: number; size?: number }): Observable<Page<Conge>> {
    const params = new HttpParams()
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 20));
    return this.http.get<Page<Conge>>(BASE + '/byEmployee/' + employeId, { params });
  }

  getById(id: number):                   Observable<Conge>  { return this.http.get<Conge>(BASE + '/' + id); }
  create(body: Partial<Conge>):          Observable<Conge>  { return this.http.post<Conge>(BASE, body); }
  approve(id: number):                   Observable<Conge>  { return this.http.put<Conge>(BASE + '/' + id + '/approve', {}); }
  reject(id: number):                    Observable<Conge>  { return this.http.put<Conge>(BASE + '/' + id + '/reject', {}); }
}
