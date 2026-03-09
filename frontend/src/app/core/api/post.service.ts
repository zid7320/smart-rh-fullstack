import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { Poste, Page } from '../models';

const BASE = environment.apiBaseUrl + '/api/posts';

@Injectable({ providedIn: 'root' })
export class PosteApiService {
  protected http = inject(HttpClient);

  getAll(opts?: { page?: number; size?: number }): Observable<Page<Poste>> {
    const params = new HttpParams()
      .set('page', String(opts?.page ?? 0))
      .set('size', String(opts?.size ?? 20));
    return this.http.get<Page<Poste>>(BASE, { params });
  }

  getById(id: number): Observable<Poste>  { return this.http.get<Poste>(BASE + '/' + id); }
  create(body: Partial<Poste>): Observable<Poste>             { return this.http.post<Poste>(BASE, body); }
  update(id: number, body: Partial<Poste>): Observable<Poste> { return this.http.put<Poste>(BASE + '/' + id, body); }
  delete(id: number): Observable<void>                        { return this.http.delete<void>(BASE + '/' + id); }

  attachCompetence(postId: number, compId: number): Observable<Poste> {
    return this.http.post<Poste>(BASE + '/' + postId + '/competences/' + compId, {});
  }
  detachCompetence(postId: number, compId: number): Observable<Poste> {
    return this.http.delete<Poste>(BASE + '/' + postId + '/competences/' + compId);
  }
}
