import {
  Component, OnInit, inject, DestroyRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatListModule } from '@angular/material/list';
import { MatTabsModule } from '@angular/material/tabs';
import { BureauSensorService } from '../../core/api/bureau-sensor.service';
import { BureauWebsocketService } from '../../core/websocket/bureau-websocket.service';
import { AuthService } from '../../core/services/auth.service';
import { TemperatureCardComponent } from './components/temperature-card/temperature-card.component';
import { Co2CardComponent } from './components/co2-card/co2-card.component';
import { OccupancyCardComponent } from './components/occupancy-card/occupancy-card.component';
import { SensorAlertsComponent } from './components/sensor-alerts/sensor-alerts.component';
import type { BureauDashboardDto } from '../../core/models';

@Component({
  selector: 'app-bureau',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatProgressBarModule, MatChipsModule, MatListModule, MatTabsModule,
    TemperatureCardComponent, Co2CardComponent, OccupancyCardComponent,
    SensorAlertsComponent,
  ],
  templateUrl: './bureau.component.html',
  styleUrl: './bureau.component.scss',
})
export class BureauComponent implements OnInit {
  private api = inject(BureauSensorService);
  private ws = inject(BureauWebsocketService);
  private auth = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  dashboards: BureauDashboardDto[] = [];
  loading = false;
  isRhAdmin = false;

  get isWsConnected(): boolean { return this.ws.connected; }

  ngOnInit(): void {
    const role = this.auth.currentUserRole();
    this.isRhAdmin = role === 'ADMIN' || role === 'RH';

    this.loadDashboards();

    // Subscribe to WebSocket updates
    this.ws.sensorUpdates$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(dashboard => this.onSensorUpdate(dashboard));

    this.ws.alertUpdates$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.loadDashboards());
  }

  loadDashboards(): void {
    this.loading = true;
    this.api.getAllDashboards().subscribe({
      next: dashboards => {
        this.dashboards = dashboards;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
      },
    });
  }

  private onSensorUpdate(dashboard: BureauDashboardDto): void {
    const index = this.dashboards.findIndex(d => d.sensor.id === dashboard.sensor.id);
    if (index >= 0) {
      this.dashboards[index] = dashboard;
    } else {
      this.dashboards.push(dashboard);
    }
  }

  refreshDashboard(sensorId: number): void {
    this.api.getSensorDashboard(sensorId).subscribe({
      next: dashboard => {
        const index = this.dashboards.findIndex(d => d.sensor.id === sensorId);
        if (index >= 0) {
          this.dashboards[index] = dashboard;
        }
      },
    });
  }
}
