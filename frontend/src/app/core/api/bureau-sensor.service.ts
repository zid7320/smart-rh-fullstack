import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import type { BureauDashboardDto, SensorAlertDto } from '../models';

@Injectable({
  providedIn: 'root',
})
export class BureauSensorService {
  private http = inject(HttpClient);
  private apiUrl = '/api/sensors';

  getAllDashboards(): Observable<BureauDashboardDto[]> {
    return this.http.get<BureauDashboardDto[]>(`${this.apiUrl}/dashboard`);
  }

  getSensorDashboard(sensorId: number): Observable<BureauDashboardDto> {
    return this.http.get<BureauDashboardDto>(`${this.apiUrl}/${sensorId}/dashboard`);
  }

  getTemperatureHistory(sensorId: number, hours: number = 24): Observable<any[]> {
    const params = new HttpParams().set('hours', hours.toString());
    return this.http.get<any[]>(`${this.apiUrl}/${sensorId}/temperature/history`, { params });
  }

  getCo2History(sensorId: number, hours: number = 24): Observable<any[]> {
    const params = new HttpParams().set('hours', hours.toString());
    return this.http.get<any[]>(`${this.apiUrl}/${sensorId}/co2/history`, { params });
  }

  getOccupancyHistory(sensorId: number, hours: number = 24): Observable<any[]> {
    const params = new HttpParams().set('hours', hours.toString());
    return this.http.get<any[]>(`${this.apiUrl}/${sensorId}/occupancy/history`, { params });
  }

  getActiveAlerts(): Observable<SensorAlertDto[]> {
    return this.http.get<SensorAlertDto[]>(`${this.apiUrl}/alerts`);
  }

  acknowledgeAlert(alertId: number, acknowledgedBy: string): Observable<SensorAlertDto> {
    const params = new HttpParams().set('acknowledgedBy', acknowledgedBy);
    return this.http.put<SensorAlertDto>(
      `${this.apiUrl}/alerts/${alertId}/acknowledge`,
      {},
      { params }
    );
  }
}
