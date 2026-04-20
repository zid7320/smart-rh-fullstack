import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AttendanceEventDto } from '../../../../core/api/attendance-event.service';

/**
 * Fraud Alert Center Component (HR/Admin Only)
 * Displays unverified suspicious attendance events for manual review
 */
@Component({
  selector: 'app-fraud-alert-center',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="alert-center">
      <div class="alert-header">
        <h2 class="widget-title">🚨 Fraud Alert Center</h2>
        <span class="alert-badge" *ngIf="alerts.length > 0">{{ alerts.length }}</span>
      </div>

      <!-- Alerts List -->
      <div class="alerts-list" *ngIf="alerts.length > 0 && !loading">
        <div class="alert-item" *ngFor="let alert of alerts">
          <!-- Alert Content -->
          <div class="alert-content">
            <!-- Employee & Reason -->
            <div class="alert-header-content">
              <span class="employee-name">{{ alert.employeeName }}</span>
              <span class="fraud-reason">{{ alert.fraudReason }}</span>
            </div>

            <!-- Device & Timestamp -->
            <div class="alert-details">
              <span class="device-info">📷 {{ alert.deviceName }}</span>
              <span class="timestamp">{{ formatTime(alert.eventTimestamp) }}</span>
            </div>

            <!-- Confidence Score (Low = Higher Risk) -->
            <div class="confidence-bar">
              <div class="bar-label">Confidence: {{ alert.confidenceScore }}%</div>
              <div class="progress-bar">
                <div class="progress-fill" [style.width.%]="alert.confidenceScore"></div>
              </div>
            </div>

            <!-- Status -->
            <div class="alert-status">
              <span class="status-text">
                <span *ngIf="alert.manuallyVerified" class="verified-check">
                  ✓ Verified on {{ formatDate(alert.verifiedAt) }}
                </span>
                <span *ngIf="!alert.manuallyVerified" class="pending-review">
                  ⏳ Pending Review
                </span>
              </span>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="alert-actions">
            <button
              class="btn-approve"
              *ngIf="!alert.manuallyVerified"
              (click)="onApproveClick(alert)"
              title="Verify as legitimate attendance">
              Approve
            </button>
            <button
              class="btn-deny"
              *ngIf="!alert.manuallyVerified"
              (click)="onDenyClick(alert)"
              title="Mark as fraud, block event">
              Block
            </button>
            <button
              class="btn-evidence"
              (click)="onViewEvidence(alert)"
              title="View photo evidence and details">
              Evidence
            </button>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div class="empty-state" *ngIf="alerts.length === 0 && !loading">
        <div class="empty-icon">✓</div>
        <p>No pending fraud alerts</p>
        <span class="empty-subtitle">All suspicious events have been reviewed</span>
      </div>

      <!-- Loading State -->
      <div class="loading-state" *ngIf="loading">
        <div class="loader"></div>
        <p>Loading fraud alerts...</p>
      </div>
    </div>
  `,
  styles: [`
    .alert-center {
      width: 100%;
    }

    .alert-header {
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

    .alert-badge {
      background: #dc3545;
      color: white;
      font-weight: 700;
      padding: 0.25rem 0.75rem;
      border-radius: 12px;
      font-size: 0.85rem;
    }

    .alerts-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
      max-height: 500px;
      overflow-y: auto;
    }

    .alert-item {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 1rem;
      padding: 1rem;
      background: #fff3cd;
      border: 2px solid #ffc107;
      border-radius: 6px;
      transition: all 0.2s ease;
    }

    .alert-item:hover {
      box-shadow: 0 4px 12px rgba(255, 193, 7, 0.3);
      transform: translateY(-2px);
    }

    .alert-content {
      flex: 1;
      min-width: 0;
    }

    .alert-header-content {
      display: flex;
      gap: 0.75rem;
      margin-bottom: 0.5rem;
      flex-wrap: wrap;
    }

    .employee-name {
      font-weight: 700;
      color: #2c3e50;
      font-size: 0.95rem;
    }

    .fraud-reason {
      background: #ffa500;
      color: white;
      padding: 0.25rem 0.5rem;
      border-radius: 4px;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
    }

    .alert-details {
      display: flex;
      gap: 1rem;
      font-size: 0.85rem;
      color: #666;
      margin-bottom: 0.75rem;
    }

    .device-info {
      white-space: nowrap;
    }

    .timestamp {
      color: #999;
    }

    .confidence-bar {
      margin-bottom: 0.75rem;
    }

    .bar-label {
      font-size: 0.75rem;
      color: #555;
      font-weight: 600;
      margin-bottom: 0.25rem;
    }

    .progress-bar {
      width: 100%;
      height: 8px;
      background: #e0e0e0;
      border-radius: 4px;
      overflow: hidden;
    }

    .progress-fill {
      height: 100%;
      background: linear-gradient(90deg, #dc3545 0%, #ffc107 100%);
      transition: width 0.3s ease;
    }

    .alert-status {
      margin-top: 0.5rem;
    }

    .status-text {
      font-size: 0.8rem;
      font-weight: 600;
    }

    .verified-check {
      color: #155724;
      background: rgba(40, 167, 69, 0.1);
      padding: 0.25rem 0.5rem;
      border-radius: 3px;
    }

    .pending-review {
      color: #856404;
    }

    .alert-actions {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      min-width: 80px;
    }

    .btn-approve,
    .btn-deny,
    .btn-evidence {
      padding: 0.4rem 0.6rem;
      font-size: 0.7rem;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.2s ease;
      text-align: center;
      white-space: nowrap;
    }

    .btn-approve {
      background: #28a745;
      color: white;
    }

    .btn-approve:hover {
      background: #218838;
    }

    .btn-deny {
      background: #dc3545;
      color: white;
    }

    .btn-deny:hover {
      background: #c82333;
    }

    .btn-evidence {
      background: #6c757d;
      color: white;
    }

    .btn-evidence:hover {
      background: #5a6268;
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
      margin: 0.5rem 0 0 0;
      font-weight: 600;
      font-size: 0.9rem;
    }

    .empty-subtitle {
      display: block;
      font-size: 0.8rem;
      margin-top: 0.25rem;
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
      border-top: 3px solid #ffc107;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin: 0 auto 0.5rem;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
  `]
})
export class FraudAlertCenterComponent {
  @Input() alerts: AttendanceEventDto[] = [];
  @Input() loading = false;

  @Output() onVerify = new EventEmitter<AttendanceEventDto>();

  formatTime(iso: string): string {
    const date = new Date(iso);
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${hours}:${minutes}`;
  }

  formatDate(iso: string | null): string {
    if (!iso) return '';
    const date = new Date(iso);
    return date.toLocaleDateString();
  }

  onApproveClick(alert: AttendanceEventDto): void {
    // Emit with verified=true
    const verified: AttendanceEventDto = {
      ...alert,
      manuallyVerified: true
    };
    this.onVerify.emit(verified);
  }

  onDenyClick(alert: AttendanceEventDto): void {
    // Emit with verified=false (block)
    const denied: AttendanceEventDto = {
      ...alert,
      manuallyVerified: false
    };
    this.onVerify.emit(denied);
  }

  onViewEvidence(alert: AttendanceEventDto): void {
    // TODO: Open modal with photo evidence
    console.log('View evidence for:', alert.id, alert.photoData);
  }
}
