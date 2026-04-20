import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import type { BureauDashboardDto } from '../../../../core/models';

@Component({
  selector: 'app-temperature-card',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatProgressBarModule,
    MatIconModule,
    MatChipsModule,
    MatButtonModule,
  ],
  template: `
    <div class="card-content">
      <div class="metric-display">
        <div class="temperature-box">
          <div class="temp-value">{{ dashboard.latestTemperature?.temperature }}°C</div>
          <div class="temp-status" [class.high]="isHighTemp()" [class.low]="isLowTemp()">
            {{ getTempStatus() }}
          </div>
        </div>

        <div class="humidity-box">
          <mat-icon>water_drop</mat-icon>
          <div class="humidity-label">Humidity</div>
          <div class="humidity-value">{{ dashboard.latestTemperature?.humidity }}%</div>
        </div>
      </div>

      <mat-progress-bar
        mode="determinate"
        [value]="getTempPercentage()"
        [class.high]="isHighTemp()"
        [class.low]="isLowTemp()">
      </mat-progress-bar>

      <div class="history-summary" *ngIf="dashboard.temperatureHistory && dashboard.temperatureHistory.length > 0">
        <div class="stat">
          <span class="label">Min:</span>
          <span class="value">{{ getMinTemp() }}°C</span>
        </div>
        <div class="stat">
          <span class="label">Max:</span>
          <span class="value">{{ getMaxTemp() }}°C</span>
        </div>
        <div class="stat">
          <span class="label">Avg:</span>
          <span class="value">{{ getAvgTemp() }}°C</span>
        </div>
      </div>

      <button mat-stroked-button color="primary" (click)="refresh.emit()" class="refresh-btn">
        <mat-icon>history</mat-icon>
        View 24h History
      </button>
    </div>
  `,
  styles: [`
    .card-content {
      padding: 24px;
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .metric-display {
      display: grid;
      grid-template-columns: 2fr 1fr;
      gap: 16px;
      align-items: center;
    }

    .temperature-box {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 24px;
      border-radius: 8px;
      text-align: center;

      .temp-value {
        font-size: 48px;
        font-weight: bold;
        line-height: 1;
      }

      .temp-status {
        font-size: 12px;
        margin-top: 8px;
        padding: 4px 12px;
        border-radius: 4px;
        display: inline-block;
        background: rgba(255, 255, 255, 0.2);
      }

      .temp-status.high {
        background: #ff6b6b;
      }

      .temp-status.low {
        background: #4ecdc4;
      }
    }

    .humidity-box {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      padding: 16px;
      background: #f5f5f5;
      border-radius: 8px;

      mat-icon {
        font-size: 32px;
        width: 32px;
        height: 32px;
        color: #2196f3;
      }

      .humidity-label {
        font-size: 12px;
        color: #666;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }

      .humidity-value {
        font-size: 20px;
        font-weight: bold;
        color: #333;
      }
    }

    mat-progress-bar {
      height: 8px;
      border-radius: 4px;

      &.high {
        ::ng-deep .mat-mdc-progress-bar-fill::after {
          background-color: #ff6b6b;
        }
      }

      &.low {
        ::ng-deep .mat-mdc-progress-bar-fill::after {
          background-color: #4ecdc4;
        }
      }
    }

    .history-summary {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 12px;

      .stat {
        padding: 12px;
        background: #f9f9f9;
        border-radius: 4px;
        text-align: center;

        .label {
          font-size: 11px;
          color: #999;
          text-transform: uppercase;
          letter-spacing: 0.5px;
          display: block;
          margin-bottom: 4px;
        }

        .value {
          font-size: 16px;
          font-weight: bold;
          color: #333;
        }
      }
    }

    .refresh-btn {
      margin-top: 8px;
    }
  `],
})
export class TemperatureCardComponent {
  @Input() dashboard!: BureauDashboardDto;
  @Output() refresh = new EventEmitter<void>();

  isHighTemp(): boolean {
    const temp = this.dashboard?.latestTemperature?.temperature;
    return temp != null && temp > 30;
  }

  isLowTemp(): boolean {
    const temp = this.dashboard?.latestTemperature?.temperature;
    return temp != null && temp < 15;
  }

  getTempStatus(): string {
    if (this.isHighTemp()) return 'High';
    if (this.isLowTemp()) return 'Low';
    return 'Normal';
  }

  getTempPercentage(): number {
    const temp = this.dashboard?.latestTemperature?.temperature ?? 20;
    return Math.max(0, Math.min(100, (temp + 40) / 1.2));
  }

  getMinTemp(): string {
    const temps = this.dashboard?.temperatureHistory?.map(t => t.temperature) ?? [];
    return temps.length > 0 ? Math.min(...temps).toFixed(1) : '-';
  }

  getMaxTemp(): string {
    const temps = this.dashboard?.temperatureHistory?.map(t => t.temperature) ?? [];
    return temps.length > 0 ? Math.max(...temps).toFixed(1) : '-';
  }

  getAvgTemp(): string {
    const temps = this.dashboard?.temperatureHistory?.map(t => t.temperature) ?? [];
    if (temps.length === 0) return '-';
    const avg = temps.reduce((a, b) => a + b, 0) / temps.length;
    return avg.toFixed(1);
  }
}
