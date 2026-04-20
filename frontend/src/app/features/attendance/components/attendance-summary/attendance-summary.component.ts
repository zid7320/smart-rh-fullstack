import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AttendanceSummary } from '../../../../core/api/attendance-event.service';

/**
 * Attendance Summary Widget
 * Displays today's statistics: check-ins, check-outs, absent, suspicious
 */
@Component({
  selector: 'app-attendance-summary',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="summary-widget">
      <h2 class="widget-title">Today's Summary</h2>

      <div class="summary-grid" *ngIf="summary && !loading">
        <!-- Check-Ins -->
        <div class="metric-card check-in">
          <div class="metric-icon">✓</div>
          <div class="metric-content">
            <div class="metric-label">Check-ins</div>
            <div class="metric-value">{{ summary.checkIns }}</div>
          </div>
        </div>

        <!-- Check-Outs -->
        <div class="metric-card check-out">
          <div class="metric-icon">⬅</div>
          <div class="metric-content">
            <div class="metric-label">Check-outs</div>
            <div class="metric-value">{{ summary.checkOuts }}</div>
          </div>
        </div>

        <!-- Present -->
        <div class="metric-card present">
          <div class="metric-icon">👤</div>
          <div class="metric-content">
            <div class="metric-label">Present</div>
            <div class="metric-value">{{ summary.present }}</div>
          </div>
        </div>

        <!-- Suspicious -->
        <div class="metric-card suspicious">
          <div class="metric-icon">⚠</div>
          <div class="metric-content">
            <div class="metric-label">Suspicious</div>
            <div class="metric-value">{{ summary.suspicious }}</div>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div class="loading-skeleton" *ngIf="loading">
        <div class="skeleton-bar"></div>
      </div>
    </div>
  `,
  styles: [`
    .summary-widget {
      width: 100%;
    }

    .widget-title {
      margin: 0 0 1rem 0;
      font-size: 1rem;
      font-weight: 600;
      color: #2c3e50;
    }

    .summary-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 0.75rem;
    }

    .metric-card {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.75rem;
      border-radius: 6px;
      background: #f8f9fa;
      border-left: 4px solid #ccc;
    }

    .metric-card.check-in {
      border-left-color: #28a745;
      background: rgba(40, 167, 69, 0.1);
    }

    .metric-card.check-out {
      border-left-color: #6c757d;
      background: rgba(108, 117, 125, 0.1);
    }

    .metric-card.present {
      border-left-color: #007bff;
      background: rgba(0, 123, 255, 0.1);
    }

    .metric-card.suspicious {
      border-left-color: #ffc107;
      background: rgba(255, 193, 7, 0.1);
    }

    .metric-icon {
      font-size: 1.5rem;
      min-width: 2rem;
      text-align: center;
    }

    .metric-content {
      flex: 1;
    }

    .metric-label {
      font-size: 0.75rem;
      color: #666;
      font-weight: 500;
      text-transform: uppercase;
    }

    .metric-value {
      font-size: 1.5rem;
      font-weight: 700;
      color: #2c3e50;
    }

    .loading-skeleton {
      padding: 1rem;
    }

    .skeleton-bar {
      height: 60px;
      background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
      background-size: 200% 100%;
      animation: loading 1.5s infinite;
      border-radius: 4px;
    }

    @keyframes loading {
      0% { background-position: 200% 0; }
      100% { background-position: -200% 0; }
    }
  `]
})
export class AttendanceSummaryComponent {
  @Input() summary: AttendanceSummary | null = null;
  @Input() loading = false;
}
