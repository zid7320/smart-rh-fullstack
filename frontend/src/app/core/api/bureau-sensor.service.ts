import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import type { BureauDashboardDto, SensorAlertDto } from '../models';
import { AuthService } from '../services/auth.service';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class BureauSensorService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);
  private apiUrl = `${environment.apiBaseUrl}/api/sensors`;

  private getHeaders(): HttpHeaders {
    const token = this.auth.currentToken();
    if (token) {
      return new HttpHeaders({
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      });
    }
    return new HttpHeaders({ 'Content-Type': 'application/json' });
  }

  getAllDashboards(): Observable<BureauDashboardDto[]> {
    return this.http.get<BureauDashboardDto[]>(`${this.apiUrl}/dashboard`, {
      headers: this.getHeaders()
    });
  }

  getSensorDashboard(sensorId: number): Observable<BureauDashboardDto> {
    return this.http.get<BureauDashboardDto>(`${this.apiUrl}/${sensorId}/dashboard`, {
      headers: this.getHeaders()
    });
  }

  getTemperatureHistory(sensorId: number, hours: number = 24): Observable<any[]> {
    const params = new HttpParams().set('hours', hours.toString());
    return this.http.get<any[]>(`${this.apiUrl}/${sensorId}/temperature/history`, { 
      params,
      headers: this.getHeaders()
    });
  }

  getCo2History(sensorId: number, hours: number = 24): Observable<any[]> {
    const params = new HttpParams().set('hours', hours.toString());
    return this.http.get<any[]>(`${this.apiUrl}/${sensorId}/co2/history`, { 
      params,
      headers: this.getHeaders()
    });
  }

  getOccupancyHistory(sensorId: number, hours: number = 24): Observable<any[]> {
    const params = new HttpParams().set('hours', hours.toString());
    return this.http.get<any[]>(`${this.apiUrl}/${sensorId}/occupancy/history`, { 
      params,
      headers: this.getHeaders()
    });
  }

  getActiveAlerts(): Observable<SensorAlertDto[]> {
    return this.http.get<SensorAlertDto[]>(`${this.apiUrl}/alerts`, {
      headers: this.getHeaders()
    });
  }

  acknowledgeAlert(alertId: number, acknowledgedBy: string): Observable<SensorAlertDto> {
    const params = new HttpParams().set('acknowledgedBy', acknowledgedBy);
    return this.http.put<SensorAlertDto>(
      `${this.apiUrl}/alerts/${alertId}/acknowledge`,
      {},
      { 
        params,
        headers: this.getHeaders()
      }
    );
  }
}
