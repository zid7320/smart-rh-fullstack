import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { BiService, DailyTrendDto, DepartmentStatDto, PeakHourDto, FraudMetricsDto, EmployeeReliabilityDto } from '../../core/api/bi.service';
import { AuthService } from '../../core/services/auth.service';

/**
 * BI & Analytics Dashboard
 * Attendance reporting, trends, fraud metrics, employee statistics
 */
@Component({
  selector: 'app-bi-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="bi-dashboard">
      <!-- Header -->
      <div class="dashboard-header">
        <div class="header-content">
          <h1>📊 Attendance Analytics & Reporting</h1>
          <p class="subtitle">Comprehensive attendance insights and metrics</p>
        </div>
        <div class="header-actions">
          <button class="btn-export" (click)="exportCsv()">📥 Export CSV</button>
          <button class="btn-export btn-pdf" (click)="exportPdf()">📄 Export PDF</button>
        </div>
      </div>

      <!-- Date Range Filter -->
      <div class="filter-section">
        <div class="filter-group">
          <label>Start Date:</label>
          <input type="date" [(ngModel)]="filterStartDate" (change)="loadData()">
        </div>
        <div class="filter-group">
          <label>End Date:</label>
          <input type="date" [(ngModel)]="filterEndDate" (change)="loadData()">
        </div>
        <button class="btn-refresh" (click)="loadData()">🔄 Refresh</button>
      </div>

      <!-- KPI Cards (High-Level Metrics) -->
      <div class="kpi-section">
        <div class="kpi-card">
          <div class="kpi-label">Total Attendance</div>
          <div class="kpi-value">{{ summaryMetrics?.totalPresent ?? 0 }}</div>
          <div class="kpi-subtitle">Days Present</div>
        </div>

        <div class="kpi-card">
          <div class="kpi-label">Attendance Rate</div>
          <div class="kpi-value">{{ (summaryMetrics?.averageDailyAttendance ?? 0) | number: '1.0-0' }}%</div>
          <div class="kpi-subtitle">Average Daily</div>
        </div>

        <div class="kpi-card alert">
          <div class="kpi-label">Fraud Rate</div>
          <div class="kpi-value">{{ (summaryMetrics?.fraudRate ?? 0) | number: '1.0-0' }}%</div>
          <div class="kpi-subtitle">Suspicious Events</div>
        </div>

        <div class="kpi-card">
          <div class="kpi-label">Avg Confidence</div>
          <div class="kpi-value">{{ (summaryMetrics?.averageConfidenceScore ?? 0) | number: '1.0-0' }}%</div>
          <div class="kpi-subtitle">Face Recognition</div>
        </div>
      </div>

      <!-- Main Charts Grid -->
      <div class="charts-grid">
        <!-- Daily Trends Chart -->
        <div class="card chart-card">
          <h3 class="card-title">Daily Attendance Trends</h3>
          <div class="placeholder-chart" *ngIf="dailyTrends.length > 0">
            <svg viewBox="0 0 500 200" class="simple-line-chart">
              <polyline
                [attr.points]="getTrendPoints()"
                fill="none"
                stroke="#007bff"
                stroke-width="2" />
              <circle *ngFor="let point of getTrendPointsArray()"
                [attr.cx]="point.x"
                [attr.cy]="point.y"
                r="3"
                fill="#007bff" />
              <line x1="0" y1="150" x2="500" y2="150" stroke="#e0e0e0" stroke-width="1" />
            </svg>
            <div class="chart-legend">
              <span class="legend-item"><span class="dot" style="background: #28a745;"></span> Check-ins</span>
              <span class="legend-item"><span class="dot" style="background: #6c757d;"></span> Check-outs</span>
              <span class="legend-item"><span class="dot" style="background: #ffc107;"></span> Suspicious</span>
            </div>
          </div>
          <div class="empty-message" *ngIf="dailyTrends.length === 0">
            No trend data available
          </div>
        </div>

        <!-- Department Statistics -->
        <div class="card chart-card">
          <h3 class="card-title">Department Attendance Rate</h3>
          <div class="placeholder-chart bar-chart" *ngIf="departmentStats.length > 0">
            <div class="bar-group" *ngFor="let dept of departmentStats">
              <div class="bar-label">{{ dept.departmentName | slice: 0: 12 }}</div>
              <div class="bar-container">
                <div class="bar" [style.width.%]="dept.attendanceRate"></div>
                <span class="bar-value">{{ dept.attendanceRate | number: '1.0-0' }}%</span>
              </div>
            </div>
          </div>
          <div class="empty-message" *ngIf="departmentStats.length === 0">
            No department data available
          </div>
        </div>

        <!-- Peak Hours Analysis -->
        <div class="card chart-card">
          <h3 class="card-title">Peak Hours Distribution</h3>
          <div class="placeholder-chart" *ngIf="peakHours.length > 0">
            <div class="peak-hours-grid">
              <div class="hour-bar" *ngFor="let hour of peakHours">
                <div class="hour-label">{{ formatHour(hour.hour) }}</div>
                <div class="hour-bars">
                  <div class="bar in" [style.height.%]="(hour.checkInCount / maxPeakCount) * 100"></div>
                  <div class="bar out" [style.height.%]="(hour.checkOutCount / maxPeakCount) * 100"></div>
                </div>
              </div>
            </div>
            <div class="peak-legend">
              <span><span class="dot" style="background: #28a745;"></span> Check-in</span>
              <span><span class="dot" style="background: #6c757d;"></span> Check-out</span>
            </div>
          </div>
          <div class="empty-message" *ngIf="peakHours.length === 0">
            No peak hours data available
          </div>
        </div>

        <!-- Fraud Breakdown -->
        <div class="card chart-card">
          <h3 class="card-title">Top Fraud Reasons</h3>
          <div class="fraud-breakdown" *ngIf="fraudMetrics && fraudMetrics.topFraudReasons.length > 0">
            <div class="fraud-item" *ngFor="let reason of fraudMetrics.topFraudReasons">
              <div class="fraud-header">
                <span class="fraud-reason">{{ formatFraudReason(reason.reason) }}</span>
                <span class="fraud-count">{{ reason.count }}</span>
              </div>
              <div class="fraud-bar">
                <div class="fraud-fill" [style.width.%]="reason.percentage"></div>
              </div>
              <span class="fraud-percentage">{{ reason.percentage | number: '1.0-0' }}%</span>
            </div>
          </div>
          <div class="empty-message" *ngIf="!fraudMetrics || fraudMetrics.topFraudReasons.length === 0">
            No fraud data available
          </div>
        </div>
      </div>

      <!-- Employee Reliability Rankings -->
      <div class="card full-width">
        <h3 class="card-title">Employee Unreliability Rankings</h3>
        <div class="employee-table" *ngIf="employeeReliability.length > 0">
          <table>
            <thead>
              <tr>
                <th>Employee</th>
                <th>Department</th>
                <th>Attendance Rate</th>
                <th>Unreliability Score</th>
                <th>Last Week Absences</th>
                <th>Recent Fraud Alerts</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let emp of employeeReliability" [class.high-risk]="emp.inconsistencyScore > 70">
                <td class="emp-name">{{ emp.employeeName }}</td>
                <td>{{ emp.departmentName }}</td>
                <td>{{ emp.attendanceRate | number: '1.0-0' }}%</td>
                <td>
                  <span class="score-badge" [class.high]="emp.inconsistencyScore > 70">
                    {{ emp.inconsistencyScore | number: '1.0-0' }}/100
                  </span>
                </td>
                <td>{{ emp.lastWeekAbsences }}</td>
                <td>{{ emp.lastMonthFraudAlerts }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="empty-message" *ngIf="employeeReliability.length === 0">
          No employee reliability data available
        </div>
      </div>
    </div>
  `,
  styles: [`
    .bi-dashboard {
      padding: 2rem;
      background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
      min-height: 100vh;
    }

    .dashboard-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 2rem;
      background: white;
      padding: 2rem;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .header-content h1 {
      margin: 0 0 0.5rem 0;
      font-size: 1.75rem;
      color: #2c3e50;
    }

    .subtitle {
      margin: 0;
      color: #666;
      font-size: 0.95rem;
    }

    .header-actions {
      display: flex;
      gap: 1rem;
    }

    .btn-export {
      padding: 0.75rem 1.5rem;
      background: #28a745;
      color: white;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.2s ease;
    }

    .btn-export:hover {
      background: #218838;
    }

    .btn-export.btn-pdf {
      background: #dc3545;
    }

    .btn-export.btn-pdf:hover {
      background: #c82333;
    }

    .filter-section {
      display: flex;
      gap: 1rem;
      margin-bottom: 2rem;
      background: white;
      padding: 1rem;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .filter-group {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .filter-group label {
      font-size: 0.85rem;
      font-weight: 600;
      color: #555;
    }

    .filter-group input {
      padding: 0.5rem;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 0.9rem;
    }

    .btn-refresh {
      align-self: flex-end;
      padding: 0.5rem 1rem;
      background: #007bff;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-weight: 500;
      transition: all 0.2s ease;
    }

    .btn-refresh:hover {
      background: #0056b3;
    }

    /* KPI Cards */
    .kpi-section {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1.5rem;
      margin-bottom: 2rem;
    }

    .kpi-card {
      background: white;
      padding: 1.5rem;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      border-left: 4px solid #007bff;
      text-align: center;
    }

    .kpi-card.alert {
      border-left-color: #ffc107;
      background: rgba(255, 193, 7, 0.05);
    }

    .kpi-label {
      font-size: 0.85rem;
      color: #666;
      font-weight: 600;
      text-transform: uppercase;
      margin-bottom: 0.5rem;
    }

    .kpi-value {
      font-size: 2.5rem;
      font-weight: 700;
      color: #2c3e50;
    }

    .kpi-subtitle {
      font-size: 0.8rem;
      color: #999;
      margin-top: 0.5rem;
    }

    /* Charts Grid */
    .charts-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
      gap: 2rem;
      margin-bottom: 2rem;
    }

    .card {
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      padding: 1.5rem;
    }

    .card-title {
      margin: 0 0 1rem 0;
      font-size: 1.1rem;
      font-weight: 600;
      color: #2c3e50;
    }

    .chart-card {
      min-height: 300px;
    }

    .placeholder-chart {
      height: 250px;
      position: relative;
      overflow: auto;
    }

    .simple-line-chart {
      width: 100%;
      height: 100%;
    }

    .chart-legend {
      display: flex;
      gap: 1.5rem;
      margin-top: 1rem;
      font-size: 0.85rem;
      justify-content: center;
    }

    .legend-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      display: inline-block;
    }

    /* Bar Chart */
    .bar-chart {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .bar-group {
      display: flex;
      gap: 1rem;
      align-items: center;
    }

    .bar-label {
      font-size: 0.8rem;
      font-weight: 600;
      min-width: 100px;
      color: #555;
    }

    .bar-container {
      flex: 1;
      position: relative;
      background: #f0f0f0;
      height: 20px;
      border-radius: 4px;
      overflow: hidden;
    }

    .bar {
      height: 100%;
      background: linear-gradient(90deg, #007bff, #0056b3);
      transition: width 0.3s ease;
    }

    .bar-value {
      font-size: 0.75rem;
      font-weight: 600;
      color: #555;
      margin-left: 0.5rem;
    }

    /* Peak Hours */
    .peak-hours-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(30px, 1fr));
      gap: 0.5rem;
      height: 200px;
    }

    .hour-bar {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.25rem;
    }

    .hour-label {
      font-size: 0.7rem;
      font-weight: 600;
      color: #666;
      writing-mode: vertical-rl;
      transform: rotate(180deg);
    }

    .hour-bars {
      width: 100%;
      height: 150px;
      display: flex;
      gap: 2px;
      align-items: flex-end;
      border-bottom: 1px solid #e0e0e0;
    }

    .hour-bars .bar {
      flex: 1;
      border-radius: 2px;
      transition: all 0.2s ease;
    }

    .hour-bars .bar.in {
      background: #28a745;
    }

    .hour-bars .bar.out {
      background: #6c757d;
    }

    .peak-legend {
      display: flex;
      gap: 1rem;
      margin-top: 1rem;
      font-size: 0.8rem;
      justify-content: center;
    }

    /* Fraud Breakdown */
    .fraud-breakdown {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .fraud-item {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .fraud-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .fraud-reason {
      font-weight: 600;
      color: #555;
      font-size: 0.9rem;
    }

    .fraud-count {
      background: #fff3cd;
      color: #856404;
      padding: 0.25rem 0.75rem;
      border-radius: 4px;
      font-weight: 600;
      font-size: 0.85rem;
    }

    .fraud-bar {
      width: 100%;
      height: 12px;
      background: #f0f0f0;
      border-radius: 6px;
      overflow: hidden;
    }

    .fraud-fill {
      height: 100%;
      background: linear-gradient(90deg, #ffc107, #ff9800);
      transition: width 0.3s ease;
    }

    .fraud-percentage {
      font-size: 0.8rem;
      color: #666;
    }

    /* Employee Table */
    .full-width {
      grid-column: 1 / -1;
    }

    .employee-table {
      overflow-x: auto;
    }

    table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.9rem;
    }

    thead {
      background: #f8f9fa;
    }

    th {
      padding: 0.75rem;
      text-align: left;
      font-weight: 600;
      color: #555;
      border-bottom: 2px solid #dee2e6;
    }

    td {
      padding: 0.75rem;
      border-bottom: 1px solid #dee2e6;
      color: #666;
    }

    tbody tr {
      transition: background-color 0.2s ease;
    }

    tbody tr:hover {
      background: #f8f9fa;
    }

    tbody tr.high-risk {
      background: rgba(255, 193, 7, 0.05);
    }

    .emp-name {
      font-weight: 600;
      color: #2c3e50;
    }

    .score-badge {
      padding: 0.25rem 0.75rem;
      border-radius: 4px;
      background: #d4edda;
      color: #155724;
      font-weight: 600;
      font-size: 0.85rem;
    }

    .score-badge.high {
      background: #f8d7da;
      color: #721c24;
    }

    .empty-message {
      text-align: center;
      padding: 2rem 1rem;
      color: #999;
      font-size: 0.95rem;
    }

    @media (max-width: 1024px) {
      .charts-grid {
        grid-template-columns: 1fr;
      }

      .dashboard-header {
        flex-direction: column;
      }

      .header-actions {
        width: 100%;
        margin-top: 1rem;
      }

      .filter-section {
        flex-direction: column;
      }

      .filter-group input {
        width: 100%;
      }
    }
  `]
})
export class BiDashboardComponent implements OnInit {
  private biService = inject(BiService);
  private auth = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  // Data
  dailyTrends: DailyTrendDto[] = [];
  departmentStats: DepartmentStatDto[] = [];
  peakHours: PeakHourDto[] = [];
  fraudMetrics: FraudMetricsDto | null = null;
  employeeReliability: EmployeeReliabilityDto[] = [];
  summaryMetrics: any = null;

  // Filters
  filterStartDate = this.getDateString(new Date(Date.now() - 30 * 24 * 60 * 60 * 1000));
  filterEndDate = this.getDateString(new Date());

  maxPeakCount = 0;

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    // Load all analytics in parallel
    this.biService.getDailyTrends(30)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(data => this.dailyTrends = data);

    this.biService.getDepartmentStats(this.filterStartDate, this.filterEndDate)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(data => this.departmentStats = data);

    this.biService.getPeakHours()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(data => {
        this.peakHours = data;
        this.maxPeakCount = Math.max(
          ...data.map(h => Math.max(h.checkInCount, h.checkOutCount)),
          1
        );
      });

    this.biService.getFraudMetrics(this.filterStartDate, this.filterEndDate)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(data => this.fraudMetrics = data);

    this.biService.getEmployeeReliability(20)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(data => this.employeeReliability = data);

    this.biService.getAttendanceSummary(this.filterStartDate, this.filterEndDate)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(data => this.summaryMetrics = data);
  }

  exportCsv(): void {
    this.biService.exportCsv(this.filterStartDate, this.filterEndDate)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `attendance-report-${this.filterStartDate}-to-${this.filterEndDate}.csv`;
        a.click();
        window.URL.revokeObjectURL(url);
      });
  }

  exportPdf(): void {
    this.biService.exportPdf(this.filterStartDate, this.filterEndDate)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `attendance-report-${this.filterStartDate}-to-${this.filterEndDate}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      });
  }

  formatFraudReason(reason: string): string {
    return reason
      .split('_')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
      .join(' ');
  }

  formatHour(hour: number): string {
    return `${hour.toString().padStart(2, '0')}:00`;
  }

  getTrendPoints(): string {
    // Simple line chart points
    if (this.dailyTrends.length === 0) return '';
    const maxCheckIns = Math.max(...this.dailyTrends.map(d => d.checkIns), 1);
    const width = 500;
    const height = 200;
    const pointWidth = width / (this.dailyTrends.length - 1 || 1);

    return this.dailyTrends
      .map((trend, idx) => {
        const x = idx * pointWidth;
        const y = height - (trend.checkIns / maxCheckIns) * height;
        return `${x},${y}`;
      })
      .join(' ');
  }

  getTrendPointsArray(): any[] {
    if (this.dailyTrends.length === 0) return [];
    const maxCheckIns = Math.max(...this.dailyTrends.map(d => d.checkIns), 1);
    const width = 500;
    const height = 200;
    const pointWidth = width / (this.dailyTrends.length - 1 || 1);

    return this.dailyTrends.map((trend, idx) => ({
      x: idx * pointWidth,
      y: height - (trend.checkIns / maxCheckIns) * height
    }));
  }

  private getDateString(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
