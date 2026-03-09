import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { DossierRH, Page } from '../models';

const BASE = environment.apiBaseUrl + '/api/dossiers';

@Injectable({ providedIn: 'root' })
export class DossierRhApiService {
  protected http = inject(HttpClient);

  getAll(opts?: { page?: number; size?: number }): Observable<Page<DossierRH>> {
    const params = new HttpParams()
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 20));
    return this.http.get<Page<DossierRH>>(BASE, { params });
  }

  getById(id: number): Observable<DossierRH> {
    return this.http.get<DossierRH>(BASE + '/' + id);
  }

  getByEmployee(employeId: number): Observable<DossierRH> {
    return this.http.get<DossierRH>(BASE + '/byEmployee/' + employeId);
  }

  update(id: number, body: Partial<DossierRH>): Observable<DossierRH> {
    return this.http.put<DossierRH>(BASE + '/' + id, body);
  }
}
