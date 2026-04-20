import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AttendanceEventService, AttendanceSummary, Page, AttendanceEventDto } from '../../../core/api/attendance-event.service';
import { WebsocketService } from '../../../core/websocket/websocket.service';
import { AuthService } from '../../../core/services/auth.service';
import { AttendanceFeedComponent } from './attendance-feed/attendance-feed.component';
import { AttendanceSummaryComponent } from './attendance-summary/attendance-summary.component';
import { FraudAlertCenterComponent } from './fraud-alert-center/fraud-alert-center.component';

/**
 * Main Facial Recognition Attendance Dashboard
 * Real-time monitoring of employee attendance with fraud detection
 */
@Component({
  selector: 'app-attendance-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    AttendanceFeedComponent,
    AttendanceSummaryComponent,
    FraudAlertCenterComponent,
  ],
  template: `
    <div class="attendance-dashboard">
      <!-- Header -->
      <div class="dashboard-header">
        <div class="header-content">
          <h1>📸 Facial Recognition Attendance</h1>
          <div class="connection-badge" [class.connected]="isWsConnected">
            <span class="dot"></span>
            {{ isWsConnected ? 'Live' : 'Offline' }}
          </div>
        </div>
      </div>

      <!-- Main Grid -->
      <div class="dashboard-grid">
        <!-- Summary Widget (Top Left) -->
        <div class="card summary-card">
          <app-attendance-summary
            [summary]="summary"
            [loading]="summaryLoading">
          </app-attendance-summary>
        </div>

        <!-- Real-Time Feed (Main) -->
        <div class="card feed-card">
          <app-attendance-feed
            [events]="recentEvents"
            [loading]="feedLoading"
            [isRhAdmin]="isRhAdmin"
            (onVerify)="onEventVerify(\$event)"
            (onLoadMore)="onLoadMore()">
          </app-attendance-feed>
        </div>

        <!-- Fraud Alert Center (Top Right, if HR) -->
        <div class="card fraud-card" *ngIf="isRhAdmin">
          <app-fraud-alert-center
            [alerts]="fraudAlerts"
            [loading]="fraudLoading"
            (onVerify)="onFraudVerify(\$event)">
          </app-fraud-alert-center>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .attendance-dashboard {
      padding: 1rem;
      background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
      min-height: 100vh;
    }

    .dashboard-header {
      margin-bottom: 2rem;
      background: white;
      padding: 1.5rem;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .header-content h1 {
      margin: 0;
      font-size: 1.75rem;
      color: #2c3e50;
    }

    .connection-badge {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem 1rem;
      background: #f0f0f0;
      border-radius: 20px;
      font-size: 0.875rem;
      font-weight: 500;
      color: #555;
    }

    .connection-badge.connected {
      background: #d4edda;
      color: #155724;
    }

    .dot {
      display: inline-block;
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #999;
    }

    .connection-badge.connected .dot {
      background: #28a745;
      animation: pulse 2s infinite;
    }

    @keyframes pulse {
      0%, 100% { opacity: 1; }
      50% { opacity: 0.5; }
    }

    .dashboard-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 2rem;
      max-width: 1400px;
      margin: 0 auto;
    }

    @media (min-width: 1024px) {
      .dashboard-grid {
        grid-template-columns: 300px 1fr;
        grid-template-rows: auto auto;
      }

      .summary-card {
        grid-column: 1;
        grid-row: 1;
      }

      .feed-card {
        grid-column: 2;
        grid-row: 1 / 3;
      }

      .fraud-card {
        grid-column: 1;
        grid-row: 2;
      }
    }

    .card {
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      padding: 1.5rem;
    }
  `]
})
export class AttendanceDashboardComponent implements OnInit {
  private api = inject(AttendanceEventService);
  private ws = inject(WebsocketService);
  private auth = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  isRhAdmin = false;
  isWsConnected = false;

  summary: AttendanceSummary | null = null;
  summaryLoading = false;

  recentEvents: AttendanceEventDto[] = [];
  feedLoading = false;

  fraudAlerts: AttendanceEventDto[] = [];
  fraudLoading = false;

  private currentPage = 0;
  private pageSize = 50;

  ngOnInit(): void {
    this.isRhAdmin = ['ADMIN', 'RH'].includes(this.auth.currentUserRole() ?? '');
    this.isWsConnected = this.ws.connected;

    if (!this.isWsConnected) {
      this.ws.connect();
    }

    this.loadSummary();
    this.loadRecentEvents();
    if (this.isRhAdmin) {
      this.loadFraudAlerts();
    }

    // Subscribe to real-time events
    this.ws.attendanceEvent$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(event => this.onRealtimeEvent(event));
  }

  private loadSummary(): void {
    this.summaryLoading = true;
    this.api.getTodaySummary()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: summary => {
          this.summary = summary;
          this.summaryLoading = false;
        },
        error: () => { this.summaryLoading = false; }
      });
  }

  private loadRecentEvents(): void {
    this.feedLoading = true;
    this.api.getRecentEvents(this.currentPage, this.pageSize)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: page => {
          this.recentEvents = page.content;
          this.feedLoading = false;
        },
        error: () => { this.feedLoading = false; }
      });
  }

  private loadFraudAlerts(): void {
    this.fraudLoading = true;
    this.api.getUnverifiedFraudAlerts()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: alerts => {
          this.fraudAlerts = alerts;
          this.fraudLoading = false;
        },
        error: () => { this.fraudLoading = false; }
      });
  }

  private onRealtimeEvent(event: AttendanceEventDto): void {
    // Update summary
    if (this.summary) {
      if (event.eventType === 'IN') this.summary.checkIns++;
      else if (event.eventType === 'OUT') this.summary.checkOuts++;
      else if (event.eventType === 'SUSPICIOUS') this.summary.suspicious++;
    }

    // Add to feed (prepend)
    if (event.processingStatus === 'PROCESSED') {
      this.recentEvents = [event, ...this.recentEvents].slice(0, this.pageSize);
    }

    // Add to fraud alerts if suspicious
    if (event.isFraudSuspected && !event.manuallyVerified) {
      this.fraudAlerts = [event, ...this.fraudAlerts];
    }
  }

  onLoadMore(): void {
    this.currentPage++;
    const obs$ = this.api.getRecentEvents(this.currentPage, this.pageSize);
    obs$.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(page => {
        this.recentEvents = [...this.recentEvents, ...page.content];
      });
  }

  onEventVerify(event: AttendanceEventDto): void {
    if (this.isRhAdmin) {
      this.api.verifyAttendanceEvent(event.id, true, 'Verified by HR')
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(() => {
          console.log('Event verified:', event.id);
          this.loadFraudAlerts();
        });
    }
  }

  onFraudVerify(event: AttendanceEventDto): void {
    this.onEventVerify(event);
  }
}
