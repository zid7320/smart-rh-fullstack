import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { Recrutement, Candidate, Employe, Page } from '../models';

const BASE = environment.apiBaseUrl + '/api/recruitments';

@Injectable({ providedIn: 'root' })
export class RecrutementApiService {
  protected http = inject(HttpClient);

  getAll(opts?: { page?: number; size?: number }): Observable<Page<Recrutement>> {
    const params = new HttpParams()
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 20));
    return this.http.get<Page<Recrutement>>(BASE, { params });
  }

  getById(id: number):                       Observable<Recrutement>  { return this.http.get<Recrutement>(BASE + '/' + id); }
  create(body: Partial<Recrutement>):        Observable<Recrutement>  { return this.http.post<Recrutement>(BASE, body); }
  update(id: number, b: Partial<Recrutement>): Observable<Recrutement>  { return this.http.put<Recrutement>(BASE + '/' + id, b); }

  getCandidates(id: number, opts?: { page?: number; size?: number }): Observable<Page<Candidate>> {
    const params = new HttpParams()
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 50));
    return this.http.get<Page<Candidate>>(BASE + '/' + id + '/candidates', { params });
  }

  addCandidate(id: number, body: { nom: string; prenom: string; email: string }): Observable<Candidate> {
    return this.http.post<Candidate>(BASE + '/' + id + '/candidates', body);
  }

  hire(id: number, body: { candidateId: number; posteId?: number | null; email?: string }): Observable<Employe> {
    return this.http.post<Employe>(BASE + '/' + id + '/hire', body);
  }
}
