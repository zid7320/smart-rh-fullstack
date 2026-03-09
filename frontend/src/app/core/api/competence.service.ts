import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { Competence, Page } from '../models';

const BASE = environment.apiBaseUrl + '/api/competences';

@Injectable({ providedIn: 'root' })
export class CompetenceApiService {
  protected http = inject(HttpClient);

  getAll(opts?: { page?: number; size?: number }): Observable<Page<Competence>> {
    const params = new HttpParams()
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 50));
    return this.http.get<Page<Competence>>(BASE, { params });
  }

  getById(id: number): Observable<Competence>  { return this.http.get<Competence>(BASE + '/' + id); }
  create(body: Partial<Competence>): Observable<Competence>             { return this.http.post<Competence>(BASE, body); }
  update(id: number, body: Partial<Competence>): Observable<Competence> { return this.http.put<Competence>(BASE + '/' + id, body); }
  delete(id: number): Observable<void>                                  { return this.http.delete<void>(BASE + '/' + id); }
}
