import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AttendanceEventDto } from '../../../../core/api/attendance-event.service';

/**
 * Real-Time Attendance Feed Component
 * Displays latest facial recognition events with fraud indicators
 */
@Component({
  selector: 'app-attendance-feed',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="feed-widget">
      <div class="feed-header">
        <h2 class="widget-title">Real-Time Attendance Feed</h2>
        <span class="event-count" *ngIf="!loading">{{ events.length }} events</span>
      </div>

      <!-- Event List -->
      <div class="feed-list" *ngIf="events.length > 0 && !loading">
        <div class="feed-item" *ngFor="let event of events" [class.fraud]="event.isFraudSuspected">
          <!-- Left: Employee & Device Info -->
          <div class="event-info">
            <div class="event-header">
              <span class="employee-name">{{ event.employeeName }}</span>
              <span class="event-type" [class]="event.eventType.toLowerCase()">
                {{ event.eventType }}
              </span>
            </div>
            <div class="event-meta">
              <span class="device-name">📷 {{ event.deviceName }}</span>
              <span class="event-time">{{ formatTime(event.eventTimestamp) }}</span>
            </div>
            <div class="event-confidence">
              Confidence: <span [class.low]="event.confidenceScore < 70">{{ event.confidenceScore }}%</span>
            </div>
          </div>

          <!-- Right: Status & Actions -->
          <div class="event-actions">
            <!-- Fraud Indicator -->
            <div class="fraud-badge" *ngIf="event.isFraudSuspected">
              <span class="badge-warning">⚠ {{ event.fraudReason }}</span>
            </div>

            <!-- Status Badge -->
            <div class="status-badge" *ngIf="!event.isFraudSuspected">
              <span class="badge-success">✓ OK</span>
            </div>

            <!-- Verify Button (for HR) -->
            <button
              class="btn-verify"
              *ngIf="isRhAdmin && event.isFraudSuspected && !event.manuallyVerified"
              (click)="onVerifyClick(event)">
              Verify
            </button>

            <!-- Verified Badge -->
            <span class="verified-badge" *ngIf="event.manuallyVerified">
              ✓ Verified
            </span>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div class="empty-state" *ngIf="events.length === 0 && !loading">
        <div class="empty-icon">📭</div>
        <p>No attendance events yet today</p>
      </div>

      <!-- Loading State -->
      <div class="loading-state" *ngIf="loading">
        <div class="loader"></div>
        <p>Loading attendance events...</p>
      </div>

      <!-- Load More Button -->
      <button
        class="btn-load-more"
        *ngIf="events.length > 0 && !loading"
        (click)="onLoadMoreClick()">
        Load More Events
      </button>
    </div>
  `,
  styles: [`
    .feed-widget {
      width: 100%;
      display: flex;
      flex-direction: column;
    }

    .feed-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
    }

    .widget-title {
      margin: 0;
      font-size: 1rem;
      font-weight: 600;
      color: #2c3e50;
    }

    .event-count {
      font-size: 0.75rem;
      color: #999;
      background: #f0f0f0;
      padding: 0.25rem 0.75rem;
      border-radius: 12px;
    }

    .feed-list {
      flex: 1;
      overflow-y: auto;
      max-height: 500px;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .feed-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1rem;
      background: white;
      border: 1px solid #e0e0e0;
      border-left: 4px solid #d0d0d0;
      border-radius: 6px;
      transition: all 0.2s ease;
    }

    .feed-item:hover {
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      border-left-color: #007bff;
    }

    .feed-item.fraud {
      background: rgba(255, 193, 7, 0.05);
      border-left-color: #ffc107;
      border-color: #ffe082;
    }

    .event-info {
      flex: 1;
      min-width: 0;
    }

    .event-header {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 0.5rem;
    }

    .employee-name {
      font-weight: 600;
      color: #2c3e50;
      font-size: 0.95rem;
    }

    .event-type {
      font-size: 0.7rem;
      font-weight: 700;
      padding: 0.25rem 0.5rem;
      border-radius: 3px;
      text-transform: uppercase;
    }

    .event-type.in {
      background: #d4edda;
      color: #155724;
    }

    .event-type.out {
      background: #d1ecf1;
      color: #0c5460;
    }

    .event-type.suspicious {
      background: #f8d7da;
      color: #721c24;
    }

    .event-meta {
      display: flex;
      gap: 1rem;
      font-size: 0.85rem;
      color: #666;
      margin-bottom: 0.5rem;
    }

    .device-name {
      white-space: nowrap;
    }

    .event-time {
      color: #999;
    }

    .event-confidence {
      font-size: 0.8rem;
      color: #666;
    }

    .event-confidence .low {
      color: #ffc107;
      font-weight: 600;
    }

    .event-actions {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-left: 1rem;
      white-space: nowrap;
    }

    .fraud-badge,
    .status-badge,
    .verified-badge {
      font-size: 0.75rem;
      font-weight: 600;
    }

    .badge-warning {
      background: #fff3cd;
      color: #856404;
      padding: 0.25rem 0.75rem;
      border-radius: 12px;
      display: block;
    }

    .badge-success {
      background: #d4edda;
      color: #155724;
      padding: 0.25rem 0.75rem;
      border-radius: 12px;
      display: block;
    }

    .verified-badge {
      background: #d4edda;
      color: #155724;
      padding: 0.25rem 0.75rem;
      border-radius: 12px;
    }

    .btn-verify {
      padding: 0.4rem 0.75rem;
      font-size: 0.75rem;
      background: #ffc107;
      color: #000;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.2s ease;
    }

    .btn-verify:hover {
      background: #e0a800;
    }

    .empty-state {
      text-align: center;
      padding: 2rem 1rem;
      color: #999;
    }

    .empty-icon {
      font-size: 2.5rem;
      margin-bottom: 0.5rem;
    }

    .empty-state p {
      margin: 0;
      font-size: 0.9rem;
    }

    .loading-state {
      text-align: center;
      padding: 2rem 1rem;
      color: #999;
    }

    .loader {
      width: 24px;
      height: 24px;
      border: 3px solid #f0f0f0;
      border-top: 3px solid #007bff;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin: 0 auto 0.5rem;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    .btn-load-more {
      align-self: center;
      margin-top: 1rem;
      padding: 0.5rem 1.5rem;
      background: #007bff;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-weight: 500;
      transition: all 0.2s ease;
    }

    .btn-load-more:hover {
      background: #0056b3;
    }
  `]
})
export class AttendanceFeedComponent {
  @Input() events: AttendanceEventDto[] = [];
  @Input() loading = false;
  @Input() isRhAdmin = false;

  @Output() onVerify = new EventEmitter<AttendanceEventDto>();
  @Output() onLoadMore = new EventEmitter<void>();

  formatTime(iso: string): string {
    const date = new Date(iso);
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${hours}:${minutes}`;
  }

  onVerifyClick(event: AttendanceEventDto): void {
    this.onVerify.emit(event);
  }

  onLoadMoreClick(): void {
    this.onLoadMore.emit();
  }
}
