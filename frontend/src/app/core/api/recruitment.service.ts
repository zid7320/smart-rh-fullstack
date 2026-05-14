import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { Recrutement, Candidate, Employe, Page } from '../models';

const BASE = environment.apiBaseUrl + '/api/recruitments';
const RESUMES_BASE = environment.apiBaseUrl + '/api/resumes';
const CANDIDATE_ANALYSIS_BASE = environment.apiBaseUrl + '/api/candidate-analysis';

export interface Resume {
  id: number;
  full_name: string;
  email: string;
  phone?: string;
  skills?: string;
  experience?: string;
  education?: string;
  languages?: string;
  sender_email?: string;
  email_subject?: string;
  received_at?: string;
  created_at: string;
}

export interface CandidateAnalysis {
  id: number;
  job_id?: number;
  candidate_name: string;
  email: string;
  match_score?: number;
  composite_score?: number;
  overall_score?: number;
  rank_position?: number;
  recommendation?: string;
  matched_skills?: string;
  missing_skills?: string;
  experience_match?: boolean;
  education_match?: boolean;
  summary?: string;
  total_evaluated?: number;
  evaluation_timestamp?: string;
  resume_file_name?: string;
  created_at: string;
}

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

  // Resume endpoints
  getAllResumes(): Observable<Resume[]> {
    return this.http.get<Resume[]>(RESUMES_BASE);
  }

  getResumeById(id: number): Observable<Resume> {
    return this.http.get<Resume>(RESUMES_BASE + '/' + id);
  }

  getResumesByEmail(email: string): Observable<Resume[]> {
    return this.http.get<Resume[]>(RESUMES_BASE + '/email/' + encodeURIComponent(email));
  }

  getResumesBySenderEmail(senderEmail: string): Observable<Resume[]> {
    return this.http.get<Resume[]>(RESUMES_BASE + '/sender-email/' + encodeURIComponent(senderEmail));
  }

  createResume(body: Partial<Resume>): Observable<Resume> {
    return this.http.post<Resume>(RESUMES_BASE, body);
  }

  updateResume(id: number, body: Partial<Resume>): Observable<Resume> {
    return this.http.put<Resume>(RESUMES_BASE + '/' + id, body);
  }

  deleteResume(id: number): Observable<void> {
    return this.http.delete<void>(RESUMES_BASE + '/' + id);
  }

  getResumesCount(): Observable<number> {
    return this.http.get<number>(RESUMES_BASE + '/count');
  }

  // Candidate Analysis endpoints
  getAllCandidateAnalyses(): Observable<CandidateAnalysis[]> {
    return this.http.get<CandidateAnalysis[]>(CANDIDATE_ANALYSIS_BASE);
  }

  getCandidateAnalysisById(id: number): Observable<CandidateAnalysis> {
    return this.http.get<CandidateAnalysis>(CANDIDATE_ANALYSIS_BASE + '/' + id);
  }

  getCandidateAnalysesByJobId(jobId: number): Observable<CandidateAnalysis[]> {
    return this.http.get<CandidateAnalysis[]>(CANDIDATE_ANALYSIS_BASE + '/job/' + jobId);
  }

  getCandidateAnalysesByEmail(email: string): Observable<CandidateAnalysis[]> {
    return this.http.get<CandidateAnalysis[]>(CANDIDATE_ANALYSIS_BASE + '/email/' + encodeURIComponent(email));
  }

  getCandidateAnalysesByJobIdAndRecommendation(jobId: number, recommendation: string): Observable<CandidateAnalysis[]> {
    return this.http.get<CandidateAnalysis[]>(CANDIDATE_ANALYSIS_BASE + '/job/' + jobId + '/recommendation/' + recommendation);
  }

  createCandidateAnalysis(body: Partial<CandidateAnalysis>): Observable<CandidateAnalysis> {
    return this.http.post<CandidateAnalysis>(CANDIDATE_ANALYSIS_BASE, body);
  }

  updateCandidateAnalysis(id: number, body: Partial<CandidateAnalysis>): Observable<CandidateAnalysis> {
    return this.http.put<CandidateAnalysis>(CANDIDATE_ANALYSIS_BASE + '/' + id, body);
  }

  deleteCandidateAnalysis(id: number): Observable<void> {
    return this.http.delete<void>(CANDIDATE_ANALYSIS_BASE + '/' + id);
  }

  getCandidateAnalysesCount(): Observable<number> {
    return this.http.get<number>(CANDIDATE_ANALYSIS_BASE + '/count');
  }
}
