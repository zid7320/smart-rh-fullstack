import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import type { BureauDashboardDto } from '../../../../core/models';

@Component({
  selector: 'app-co2-card',
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
        <div class="co2-box" [class.alert]="isAlert()" [class.warning]="isWarning()">
          <div class="co2-value">{{ dashboard.latestCo2?.co2Level }} ppm</div>
          <div class="co2-status" [ngSwitch]="getStatus()">
            <mat-chip *ngSwitchCase="'Alert'" color="warn" selected>
              <mat-icon>warning</mat-icon>
              {{ getStatus() }}
            </mat-chip>
            <mat-chip *ngSwitchCase="'Warning'" color="accent" selected>
              <mat-icon>info</mat-icon>
              {{ getStatus() }}
            </mat-chip>
            <mat-chip *ngSwitchDefault>
              <mat-icon>check_circle</mat-icon>
              Good
            </mat-chip>
          </div>
        </div>

        <div class="thresholds-box">
          <div class="threshold-item">
            <span class="threshold-label">Normal</span>
            <span class="threshold-value">&lt; 1000 ppm</span>
          </div>
          <div class="threshold-item warning">
            <span class="threshold-label">Warning</span>
            <span class="threshold-value">1000-1500 ppm</span>
          </div>
          <div class="threshold-item alert">
            <span class="threshold-label">Alert</span>
            <span class="threshold-value">&gt; 1500 ppm</span>
          </div>
        </div>
      </div>

      <mat-progress-bar
        mode="determinate"
        [value]="getCo2Percentage()"
        [class.alert]="isAlert()"
        [class.warning]="isWarning()">
      </mat-progress-bar>

      <div class="history-summary" *ngIf="dashboard.co2History && dashboard.co2History.length > 0">
        <div class="stat">
          <span class="label">Min:</span>
          <span class="value">{{ getMinCo2() }} ppm</span>
        </div>
        <div class="stat">
          <span class="label">Max:</span>
          <span class="value">{{ getMaxCo2() }} ppm</span>
        </div>
        <div class="stat">
          <span class="label">Avg:</span>
          <span class="value">{{ getAvgCo2() }} ppm</span>
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

    .co2-box {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 24px;
      border-radius: 8px;
      text-align: center;
      transition: all 0.3s ease;

      &.alert {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }

      &.warning {
        background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
      }

      .co2-value {
        font-size: 42px;
        font-weight: bold;
        line-height: 1;
        margin-bottom: 12px;
      }

      .co2-status {
        display: flex;
        justify-content: center;

        mat-chip {
          font-size: 12px;
          padding: 4px 12px;
        }
      }
    }

    .thresholds-box {
      display: flex;
      flex-direction: column;
      gap: 8px;

      .threshold-item {
        padding: 8px 12px;
        background: #f5f5f5;
        border-radius: 4px;
        display: flex;
        flex-direction: column;
        gap: 2px;

        .threshold-label {
          font-size: 11px;
          color: #666;
          text-transform: uppercase;
          letter-spacing: 0.5px;
        }

        .threshold-value {
          font-size: 12px;
          font-weight: 500;
          color: #333;
        }

        &.warning {
          background: #fff3cd;
          border-left: 3px solid #ffc107;
        }

        &.alert {
          background: #f8d7da;
          border-left: 3px solid #dc3545;
        }
      }
    }

    mat-progress-bar {
      height: 8px;
      border-radius: 4px;

      &.alert {
        ::ng-deep .mat-mdc-progress-bar-fill::after {
          background-color: #dc3545;
        }
      }

      &.warning {
        ::ng-deep .mat-mdc-progress-bar-fill::after {
          background-color: #ffc107;
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
export class Co2CardComponent {
  @Input() dashboard!: BureauDashboardDto;
  @Output() refresh = new EventEmitter<void>();

  isAlert(): boolean {
    const level = this.dashboard?.latestCo2?.co2Level ?? 0;
    return level > 1500;
  }

  isWarning(): boolean {
    const level = this.dashboard?.latestCo2?.co2Level ?? 0;
    return level > 1000 && level <= 1500;
  }

  getStatus(): string {
    if (this.isAlert()) return 'Alert';
    if (this.isWarning()) return 'Warning';
    return 'Good';
  }

  getCo2Percentage(): number {
    const level = this.dashboard?.latestCo2?.co2Level ?? 0;
    return Math.min(100, (level / 5000) * 100);
  }

  getMinCo2(): number {
    const levels = this.dashboard?.co2History?.map(r => r.co2Level) ?? [];
    return levels.length > 0 ? Math.min(...levels) : 0;
  }

  getMaxCo2(): number {
    const levels = this.dashboard?.co2History?.map(r => r.co2Level) ?? [];
    return levels.length > 0 ? Math.max(...levels) : 0;
  }

  getAvgCo2(): string {
    const levels = this.dashboard?.co2History?.map(r => r.co2Level) ?? [];
    if (levels.length === 0) return '-';
    const avg = levels.reduce((a, b) => a + b, 0) / levels.length;
    return Math.round(avg).toString();
  }
}
