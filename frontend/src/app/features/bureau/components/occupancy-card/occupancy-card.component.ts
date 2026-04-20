import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import type { BureauDashboardDto } from '../../../../core/models';

@Component({
  selector: 'app-occupancy-card',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatChipsModule,
    MatButtonModule,
    MatProgressBarModule,
  ],
  template: `
    <div class="card-content">
      <div class="occupancy-display">
        <div class="occupancy-box" [class.occupied]="isOccupied()">
          <mat-icon class="status-icon">
            {{ isOccupied() ? 'person' : 'person_outline' }}
          </mat-icon>
          <div class="occupancy-status">
            {{ isOccupied() ? 'OCCUPIED' : 'EMPTY' }}
          </div>
        </div>

        <div class="occupancy-info">
          <div class="info-item">
            <span class="label">Motion Duration</span>
            <span class="value">{{ dashboard.latestOccupancy?.motionDuration || 0 }}s</span>
          </div>
          <div class="info-item">
            <span class="label">Confidence</span>
            <span class="value">{{ getConfidence() }}%</span>
          </div>
        </div>
      </div>

      <mat-progress-bar
        mode="determinate"
        [value]="getConfidenceValue()"
        [class.high]="getConfidenceValue() > 80">
      </mat-progress-bar>

      <div class="detection-stats" *ngIf="dashboard.occupancyHistory && dashboard.occupancyHistory.length > 0">
        <div class="stat">
          <span class="label">Total Changes</span>
          <span class="value">{{ countChanges() }}</span>
        </div>
        <div class="stat">
          <span class="label">Current Session</span>
          <span class="value">{{ getSessionDuration() }}</span>
        </div>
        <div class="stat">
          <span class="label">Latest Update</span>
          <span class="value">{{ getLastUpdate() }}</span>
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

    .occupancy-display {
      display: grid;
      grid-template-columns: 1.5fr 1fr;
      gap: 16px;
      align-items: center;
    }

    .occupancy-box {
      background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
      padding: 32px;
      border-radius: 12px;
      text-align: center;
      transition: all 0.3s ease;

      &.occupied {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        box-shadow: 0 8px 16px rgba(102, 126, 234, 0.2);

        .status-icon {
          color: white;
        }

        .occupancy-status {
          color: white;
        }
      }

      .status-icon {
        font-size: 56px;
        width: 56px;
        height: 56px;
        margin: 0 auto 12px;
        color: #333;
        transition: all 0.3s ease;
      }

      .occupancy-status {
        font-size: 24px;
        font-weight: bold;
        letter-spacing: 2px;
        color: #333;
        transition: all 0.3s ease;
      }
    }

    .occupancy-info {
      display: flex;
      flex-direction: column;
      gap: 12px;

      .info-item {
        padding: 12px;
        background: #f5f5f5;
        border-radius: 8px;
        display: flex;
        justify-content: space-between;
        align-items: center;

        .label {
          font-size: 11px;
          color: #999;
          text-transform: uppercase;
          letter-spacing: 0.5px;
        }

        .value {
          font-size: 16px;
          font-weight: bold;
          color: #333;
        }
      }
    }

    mat-progress-bar {
      height: 8px;
      border-radius: 4px;

      &.high {
        ::ng-deep .mat-mdc-progress-bar-fill::after {
          background-color: #667eea;
        }
      }
    }

    .detection-stats {
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
export class OccupancyCardComponent {
  @Input() dashboard!: BureauDashboardDto;
  @Output() refresh = new EventEmitter<void>();

  isOccupied(): boolean {
    return this.dashboard?.latestOccupancy?.isOccupied ?? false;
  }

  getConfidence(): string {
    const conf = this.dashboard?.latestOccupancy?.confidenceLevel;
    if (conf == null) return '-';
    return (conf * 100).toFixed(0);
  }

  getConfidenceValue(): number {
    const conf = this.dashboard?.latestOccupancy?.confidenceLevel ?? 0;
    return Math.min(100, conf * 100);
  }

  countChanges(): number {
    const history = this.dashboard?.occupancyHistory ?? [];
    let changes = 0;
    for (let i = 1; i < history.length; i++) {
      if (history[i].isOccupied !== history[i - 1].isOccupied) {
        changes++;
      }
    }
    return changes;
  }

  getSessionDuration(): string {
    if (!this.isOccupied() || !this.dashboard?.occupancyHistory?.length) {
      return '-';
    }
    const history = this.dashboard.occupancyHistory;
    const lastOccupied = history.find(h => h.isOccupied);
    if (!lastOccupied?.timestamp) return '-';

    const diff = Date.now() - new Date(lastOccupied.timestamp).getTime();
    const minutes = Math.floor(diff / 60000);
    if (minutes < 60) return `${minutes}m`;
    const hours = Math.floor(minutes / 60);
    return `${hours}h ${minutes % 60}m`;
  }

  getLastUpdate(): string {
    const timestamp = this.dashboard?.latestOccupancy?.timestamp;
    if (!timestamp) return '-';

    const diff = Date.now() - new Date(timestamp).getTime();
    const seconds = Math.floor(diff / 1000);
    if (seconds < 60) return `${seconds}s ago`;
    const minutes = Math.floor(seconds / 60);
    if (minutes < 60) return `${minutes}m ago`;
    const hours = Math.floor(minutes / 60);
    return `${hours}h ago`;
  }
}
