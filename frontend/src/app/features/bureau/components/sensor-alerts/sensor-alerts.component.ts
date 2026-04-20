import { Component, Input, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { BureauSensorService } from '../../../../core/api/bureau-sensor.service';
import type { SensorAlertDto } from '../../../../core/models';

@Component({
  selector: 'app-sensor-alerts',
  standalone: true,
  imports: [
    CommonModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatChipsModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <div class="alerts-container">
      <div *ngIf="loading" class="loading">
        <mat-spinner diameter="40"></mat-spinner>
      </div>

      <div *ngIf="!loading && alerts.length === 0" class="empty-state">
        <mat-icon>check_circle</mat-icon>
        <p>No active alerts</p>
      </div>

      <mat-list *ngIf="!loading && alerts.length > 0">
        <mat-list-item *ngFor="let alert of alerts" class="alert-item" [class.high]="isHighSeverity(alert)">
          <mat-icon matListItemIcon [class]="'alert-icon ' + alert.alertType">
            {{ getAlertIcon(alert) }}
          </mat-icon>

          <div matListItemTitle class="alert-title">
            {{ getAlertTitle(alert) }}
          </div>

          <div matListItemLine class="alert-meta">
            <span class="sensor-name">{{ alert.sensorId }}</span>
            <span class="timestamp">{{ formatTime(alert.triggeredAt) }}</span>
          </div>

          <div matListItemLine class="alert-details" *ngIf="alert.thresholdValue">
            <span>Threshold: {{ alert.thresholdValue }}</span>
            <span *ngIf="alert.actualValue">Actual: {{ alert.actualValue }}</span>
          </div>

          <button mat-icon-button matListItemMeta color="primary"
                  (click)="acknowledgeAlert(alert)"
                  *ngIf="isRhAdmin"
                  matTooltip="Acknowledge alert">
            <mat-icon>check</mat-icon>
          </button>

          <mat-divider></mat-divider>
        </mat-list-item>
      </mat-list>
    </div>
  `,
  styles: [`
    .alerts-container {
      width: 100%;
    }

    .loading {
      display: flex;
      justify-content: center;
      align-items: center;
      padding: 40px 20px;
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 40px 20px;
      text-align: center;
      color: #999;

      mat-icon {
        font-size: 48px;
        width: 48px;
        height: 48px;
        margin-bottom: 12px;
        color: #4caf50;
      }

      p {
        margin: 0;
        font-size: 14px;
      }
    }

    mat-list {
      padding: 0;
    }

    .alert-item {
      padding: 16px 0 !important;
      border-bottom: 1px solid #eee;

      &.high {
        background: #fff5f5;

        .alert-icon {
          color: #dc3545;
        }
      }

      &:last-child {
        border-bottom: none;
      }

      .alert-icon {
        font-size: 24px;
        width: 24px;
        height: 24px;
        margin-right: 12px;

        &.OFFLINE {
          color: #999;
        }

        &.TEMPERATURE_HIGH,
        &.TEMPERATURE_LOW {
          color: #ff6b6b;
        }

        &.CO2_HIGH {
          color: #ffc107;
        }

        &.OCCUPANCY_CHANGE {
          color: #2196f3;
        }
      }

      .alert-title {
        font-weight: 500;
        color: #333;
        margin-bottom: 4px;
      }

      .alert-meta {
        display: flex;
        gap: 16px;
        font-size: 12px;
        color: #999;

        .sensor-name {
          font-weight: 500;
        }

        .timestamp {
          color: #bbb;
        }
      }

      .alert-details {
        font-size: 12px;
        color: #666;
        margin-top: 4px;
        display: flex;
        gap: 12px;
      }
    }
  `],
})
export class SensorAlertsComponent implements OnInit {
  @Input() isRhAdmin = false;

  private api = inject(BureauSensorService);

  alerts: SensorAlertDto[] = [];
  loading = false;

  ngOnInit(): void {
    this.loadAlerts();
    // Reload alerts every 30 seconds
    setInterval(() => this.loadAlerts(), 30000);
  }

  loadAlerts(): void {
    this.loading = true;
    this.api.getActiveAlerts().subscribe({
      next: alerts => {
        this.alerts = alerts;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  getAlertIcon(alert: SensorAlertDto): string {
    switch (alert.alertType) {
      case 'OFFLINE': return 'cloud_off';
      case 'TEMPERATURE_HIGH': return 'thermostat';
      case 'TEMPERATURE_LOW': return 'ac_unit';
      case 'CO2_HIGH': return 'air';
      case 'OCCUPANCY_CHANGE': return 'person';
      default: return 'warning';
    }
  }

  getAlertTitle(alert: SensorAlertDto): string {
    switch (alert.alertType) {
      case 'OFFLINE': return 'Sensor Offline';
      case 'TEMPERATURE_HIGH': return 'High Temperature';
      case 'TEMPERATURE_LOW': return 'Low Temperature';
      case 'CO2_HIGH': return 'High CO2 Level';
      case 'OCCUPANCY_CHANGE': return 'Occupancy Change';
      default: return 'Alert';
    }
  }

  isHighSeverity(alert: SensorAlertDto): boolean {
    return ['OFFLINE', 'TEMPERATURE_HIGH', 'CO2_HIGH'].includes(alert.alertType);
  }

  formatTime(timestamp: string): string {
    if (!timestamp) return '-';
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);

    if (minutes < 1) return 'just now';
    if (minutes < 60) return `${minutes}m ago`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}h ago`;
    const days = Math.floor(hours / 24);
    return `${days}d ago`;
  }

  acknowledgeAlert(alert: SensorAlertDto): void {
    if (!alert.id) return;
    this.api.acknowledgeAlert(alert.id, 'current_user').subscribe({
      next: () => {
        this.loadAlerts();
      },
    });
  }
}
