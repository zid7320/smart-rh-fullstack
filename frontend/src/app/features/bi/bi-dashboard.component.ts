import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { BiService, DailyTrendDto, DepartmentStatDto, PeakHourDto, FraudMetricsDto, EmployeeReliabilityDto } from '../../core/api/bi.service';

@Component({
  selector: 'app-bi-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="bi-root">

      <!-- ── Page Header ─────────────────────────────────────────────── -->
      <div class="page-header">
        <div class="header-left">
          <span class="header-eyebrow">Reporting BI</span>
          <h1 class="header-title">Attendance Intelligence</h1>
          <p class="header-sub">Real-time workforce insights &nbsp;·&nbsp; Smart RH 4.0</p>
        </div>
        <div class="header-right">
          <div class="filter-bar">
            <input type="date" class="date-input" [(ngModel)]="filterStartDate" (change)="loadData()">
            <span class="filter-arrow">→</span>
            <input type="date" class="date-input" [(ngModel)]="filterEndDate" (change)="loadData()">
            <button class="btn-icon-round" (click)="loadData()" title="Refresh">↻</button>
          </div>
          <div class="export-group">
            <button class="btn-outline" (click)="exportCsv()">↓ CSV</button>
            <button class="btn-solid" (click)="exportPdf()">↓ PDF</button>
          </div>
        </div>
      </div>

      <!-- ── KPI Strip ───────────────────────────────────────────────── -->
      <div class="kpi-strip">

        <div class="kpi-card">
          <div class="kpi-icon kpi-indigo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
          </div>
          <div class="kpi-body">
            <div class="kpi-num">{{ summaryMetrics?.totalPresent ?? 0 }}</div>
            <div class="kpi-lbl">Total Check-ins</div>
          </div>
        </div>

        <div class="kpi-card">
          <div class="kpi-icon kpi-emerald">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
          </div>
          <div class="kpi-body">
            <div class="kpi-num">{{ (summaryMetrics?.averageDailyAttendance ?? 0) | number:'1.0-0' }}%</div>
            <div class="kpi-lbl">Avg Daily Rate</div>
          </div>
        </div>

        <div class="kpi-card">
          <div class="kpi-icon kpi-amber">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
          </div>
          <div class="kpi-body">
            <div class="kpi-num">{{ (fraudMetrics?.fraudRate ?? 0) | number:'1.1-1' }}%</div>
            <div class="kpi-lbl">Fraud Rate</div>
          </div>
        </div>

        <div class="kpi-card">
          <div class="kpi-icon kpi-sky">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          </div>
          <div class="kpi-body">
            <div class="kpi-num">{{ (summaryMetrics?.averageConfidenceScore ?? 0) | number:'1.0-0' }}%</div>
            <div class="kpi-lbl">Avg Confidence</div>
          </div>
        </div>

        <div class="kpi-card">
          <div class="kpi-icon kpi-rose">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg>
          </div>
          <div class="kpi-body">
            <div class="kpi-num">{{ fraudMetrics?.suspiciousEvents ?? 0 }}</div>
            <div class="kpi-lbl">Suspicious Events</div>
          </div>
        </div>

      </div>

      <!-- ── Charts Grid ─────────────────────────────────────────────── -->
      <div class="charts-grid">

        <!-- Daily Trends (wide) -->
        <div class="widget span-2">
          <div class="widget-head">
            <div>
              <h3 class="widget-title">Daily Attendance Trends</h3>
              <p class="widget-sub">Check-in and check-out activity · last 30 days</p>
            </div>
            <span class="tag tag-indigo">30 days</span>
          </div>
          <div class="trend-wrap" *ngIf="dailyTrends.length > 0">
            <svg viewBox="0 0 600 180" class="trend-svg" preserveAspectRatio="none">
              <defs>
                <linearGradient id="areaGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stop-color="#6366f1" stop-opacity="0.18"/>
                  <stop offset="100%" stop-color="#6366f1" stop-opacity="0"/>
                </linearGradient>
              </defs>
              <line x1="0" y1="45"  x2="600" y2="45"  stroke="#f1f5f9" stroke-width="1"/>
              <line x1="0" y1="90"  x2="600" y2="90"  stroke="#f1f5f9" stroke-width="1"/>
              <line x1="0" y1="135" x2="600" y2="135" stroke="#f1f5f9" stroke-width="1"/>
              <polygon [attr.points]="getTrendAreaPoints()" fill="url(#areaGrad)"/>
              <polyline [attr.points]="getTrendPoints()" fill="none" stroke="#6366f1" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
              <circle *ngFor="let p of getTrendPointsArray()" [attr.cx]="p.x" [attr.cy]="p.y" r="3.5" fill="#6366f1" stroke="white" stroke-width="2"/>
            </svg>
            <div class="legend-row">
              <span class="legend-pill indigo">● Check-ins</span>
              <span class="legend-pill slate">● Check-outs</span>
              <span class="legend-pill amber">● Suspicious</span>
            </div>
          </div>
          <div class="empty-state" *ngIf="dailyTrends.length === 0">No trend data available</div>
        </div>

        <!-- Department Attendance -->
        <div class="widget">
          <div class="widget-head">
            <div>
              <h3 class="widget-title">Department Attendance</h3>
              <p class="widget-sub">Rate per department · today</p>
            </div>
          </div>
          <div class="dept-list" *ngIf="departmentStats.length > 0">
            <div class="dept-row" *ngFor="let d of departmentStats; let i = index">
              <div class="dept-rank">{{ i + 1 }}</div>
              <div class="dept-info">
                <div class="dept-name">{{ d.departmentName }}</div>
                <div class="dept-meta">{{ d.presentToday }}/{{ d.totalEmployees }} present</div>
              </div>
              <div class="dept-right">
                <div class="dept-track">
                  <div class="dept-fill"
                    [style.width.%]="d.attendanceRate"
                    [class.fill-green]="d.attendanceRate >= 80"
                    [class.fill-amber]="d.attendanceRate >= 50 && d.attendanceRate < 80"
                    [class.fill-red]="d.attendanceRate < 50"></div>
                </div>
                <span class="dept-pct">{{ d.attendanceRate | number:'1.0-0' }}%</span>
              </div>
            </div>
          </div>
          <div class="empty-state" *ngIf="departmentStats.length === 0">No department data</div>
        </div>

        <!-- Peak Hours -->
        <div class="widget">
          <div class="widget-head">
            <div>
              <h3 class="widget-title">Peak Hours</h3>
              <p class="widget-sub">Hourly check-in / check-out volume</p>
            </div>
          </div>
          <div class="peak-chart" *ngIf="peakHours.length > 0">
            <div class="peak-col" *ngFor="let h of peakHours">
              <div class="peak-bars">
                <div class="peak-bar peak-in"  [style.height.%]="(h.checkInCount  / maxPeakCount) * 100"></div>
                <div class="peak-bar peak-out" [style.height.%]="(h.checkOutCount / maxPeakCount) * 100"></div>
              </div>
              <div class="peak-lbl">{{ formatHour(h.hour) }}</div>
            </div>
          </div>
          <div class="legend-row" *ngIf="peakHours.length > 0">
            <span class="legend-pill green">● Check-in</span>
            <span class="legend-pill slate">● Check-out</span>
          </div>
          <div class="empty-state" *ngIf="peakHours.length === 0">No peak data</div>
        </div>

        <!-- Fraud Reasons -->
        <div class="widget">
          <div class="widget-head">
            <div>
              <h3 class="widget-title">Top Fraud Reasons</h3>
              <p class="widget-sub">Distribution of suspicious events</p>
            </div>
            <span class="tag tag-rose">{{ fraudMetrics?.suspiciousEvents ?? 0 }} alerts</span>
          </div>
          <div class="fraud-list" *ngIf="fraudMetrics && fraudMetrics.topFraudReasons.length > 0">
            <div class="fraud-row" *ngFor="let r of fraudMetrics.topFraudReasons">
              <div class="fraud-dot">⚠</div>
              <div class="fraud-body">
                <div class="fraud-top">
                  <span class="fraud-lbl">{{ formatFraudReason(r.reason) }}</span>
                  <span class="fraud-pill">{{ r.count }}</span>
                </div>
                <div class="fraud-track">
                  <div class="fraud-fill" [style.width.%]="r.percentage"></div>
                </div>
                <span class="fraud-pct">{{ r.percentage | number:'1.0-0' }}% of alerts</span>
              </div>
            </div>
          </div>
          <div class="empty-state" *ngIf="!fraudMetrics || fraudMetrics.topFraudReasons.length === 0">
            No fraud data available
          </div>
        </div>

      </div>

      <!-- ── Employee Table ──────────────────────────────────────────── -->
      <div class="widget full-row">
        <div class="widget-head">
          <div>
            <h3 class="widget-title">Employee Reliability Report</h3>
            <p class="widget-sub">Ranked by unreliability score · last 30 days</p>
          </div>
          <span class="tag tag-slate">{{ employeeReliability.length }} employees</span>
        </div>
        <div class="table-wrap" *ngIf="employeeReliability.length > 0">
          <table class="emp-table">
            <thead>
              <tr>
                <th>#</th>
                <th>Employee</th>
                <th>Department</th>
                <th>Attendance Rate</th>
                <th>Reliability Score</th>
                <th>Absences (7d)</th>
                <th>Fraud Alerts (30d)</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let emp of employeeReliability; let i = index" [class.row-risk]="emp.inconsistencyScore > 70">
                <td class="rank-td">{{ i + 1 }}</td>
                <td>
                  <div class="emp-cell">
                    <div class="avatar" [class.avatar-risk]="emp.inconsistencyScore > 70">{{ emp.employeeName.charAt(0) }}</div>
                    <span class="emp-name">{{ emp.employeeName }}</span>
                  </div>
                </td>
                <td><span class="dept-chip">{{ emp.departmentName }}</span></td>
                <td>
                  <div class="rate-cell">
                    <div class="mini-track">
                      <div class="mini-fill"
                        [style.width.%]="emp.attendanceRate"
                        [class.fill-green]="emp.attendanceRate >= 80"
                        [class.fill-amber]="emp.attendanceRate >= 50 && emp.attendanceRate < 80"
                        [class.fill-red]="emp.attendanceRate < 50"></div>
                    </div>
                    <span class="rate-val">{{ emp.attendanceRate | number:'1.0-0' }}%</span>
                  </div>
                </td>
                <td>
                  <span class="score-badge"
                    [class.score-ok]="emp.inconsistencyScore <= 40"
                    [class.score-warn]="emp.inconsistencyScore > 40 && emp.inconsistencyScore <= 70"
                    [class.score-risk]="emp.inconsistencyScore > 70">
                    {{ emp.inconsistencyScore | number:'1.0-0' }}<span class="score-max">/100</span>
                  </span>
                </td>
                <td class="center-td">{{ emp.lastWeekAbsences }}</td>
                <td class="center-td">
                  <span class="alert-pill" *ngIf="emp.lastMonthFraudAlerts > 0">{{ emp.lastMonthFraudAlerts }}</span>
                  <span class="no-alert" *ngIf="emp.lastMonthFraudAlerts === 0">—</span>
                </td>
                <td>
                  <span class="status-chip"
                    [class.chip-ok]="emp.inconsistencyScore <= 40"
                    [class.chip-warn]="emp.inconsistencyScore > 40 && emp.inconsistencyScore <= 70"
                    [class.chip-risk]="emp.inconsistencyScore > 70">
                    {{ emp.inconsistencyScore > 70 ? 'At Risk' : emp.inconsistencyScore > 40 ? 'Monitor' : 'Good' }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="empty-state" *ngIf="employeeReliability.length === 0">No employee data available</div>
      </div>

    </div>
  `,
  styles: [`
    /* ── Reset & Root ─────────────────────────────────────────────────── */
    .bi-root {
      padding: 2rem;
      background: #f8fafc;
      min-height: 100vh;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      color: #1e293b;
    }

    /* ── Page Header ──────────────────────────────────────────────────── */
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      margin-bottom: 2rem;
      gap: 1rem;
    }
    .header-eyebrow {
      display: inline-block;
      font-size: 0.7rem;
      font-weight: 700;
      letter-spacing: 0.12em;
      text-transform: uppercase;
      color: #6366f1;
      background: #eef2ff;
      padding: 0.2rem 0.7rem;
      border-radius: 99px;
      margin-bottom: 0.5rem;
    }
    .header-title {
      font-size: 1.75rem;
      font-weight: 800;
      color: #0f172a;
      margin: 0 0 0.25rem 0;
      letter-spacing: -0.02em;
    }
    .header-sub {
      font-size: 0.875rem;
      color: #64748b;
      margin: 0;
    }
    .header-right {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: 0.75rem;
    }
    .filter-bar {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      padding: 0.4rem 0.75rem;
    }
    .date-input {
      border: none;
      outline: none;
      font-size: 0.85rem;
      color: #334155;
      background: transparent;
    }
    .filter-arrow {
      color: #94a3b8;
      font-size: 0.85rem;
    }
    .btn-icon-round {
      width: 28px;
      height: 28px;
      border-radius: 50%;
      border: 1px solid #e2e8f0;
      background: #f8fafc;
      cursor: pointer;
      font-size: 1rem;
      color: #475569;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.15s;
    }
    .btn-icon-round:hover { background: #6366f1; color: white; border-color: #6366f1; }
    .export-group { display: flex; gap: 0.5rem; }
    .btn-outline {
      padding: 0.5rem 1.25rem;
      border: 1.5px solid #e2e8f0;
      background: white;
      border-radius: 8px;
      font-size: 0.85rem;
      font-weight: 600;
      color: #475569;
      cursor: pointer;
      transition: all 0.15s;
    }
    .btn-outline:hover { border-color: #6366f1; color: #6366f1; }
    .btn-solid {
      padding: 0.5rem 1.25rem;
      background: #6366f1;
      color: white;
      border: none;
      border-radius: 8px;
      font-size: 0.85rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.15s;
    }
    .btn-solid:hover { background: #4f46e5; }

    /* ── KPI Strip ────────────────────────────────────────────────────── */
    .kpi-strip {
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 1rem;
      margin-bottom: 1.75rem;
    }
    .kpi-card {
      background: white;
      border: 1px solid #f1f5f9;
      border-radius: 14px;
      padding: 1.25rem 1.25rem 1rem;
      display: flex;
      align-items: center;
      gap: 1rem;
      box-shadow: 0 1px 3px rgba(0,0,0,0.05), 0 1px 2px rgba(0,0,0,0.04);
      transition: box-shadow 0.2s;
    }
    .kpi-card:hover { box-shadow: 0 4px 16px rgba(99,102,241,0.10); }
    .kpi-icon {
      width: 44px;
      height: 44px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .kpi-icon svg { width: 20px; height: 20px; }
    .kpi-indigo  { background: #eef2ff; color: #6366f1; }
    .kpi-emerald { background: #ecfdf5; color: #10b981; }
    .kpi-amber   { background: #fffbeb; color: #f59e0b; }
    .kpi-sky     { background: #f0f9ff; color: #0ea5e9; }
    .kpi-rose    { background: #fff1f2; color: #f43f5e; }
    .kpi-num {
      font-size: 1.7rem;
      font-weight: 800;
      color: #0f172a;
      line-height: 1;
      letter-spacing: -0.03em;
    }
    .kpi-lbl {
      font-size: 0.75rem;
      color: #64748b;
      font-weight: 500;
      margin-top: 0.2rem;
    }

    /* ── Widget Shell ─────────────────────────────────────────────────── */
    .charts-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 1.25rem;
      margin-bottom: 1.25rem;
    }
    .widget {
      background: white;
      border: 1px solid #f1f5f9;
      border-radius: 16px;
      padding: 1.5rem;
      box-shadow: 0 1px 3px rgba(0,0,0,0.05);
    }
    .widget.span-2 { grid-column: span 2; }
    .full-row { margin-bottom: 2rem; }
    .widget-head {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 1.25rem;
    }
    .widget-title {
      font-size: 1rem;
      font-weight: 700;
      color: #0f172a;
      margin: 0 0 0.2rem 0;
    }
    .widget-sub {
      font-size: 0.75rem;
      color: #94a3b8;
      margin: 0;
    }
    .tag {
      padding: 0.2rem 0.65rem;
      border-radius: 99px;
      font-size: 0.7rem;
      font-weight: 700;
      letter-spacing: 0.04em;
      white-space: nowrap;
    }
    .tag-indigo { background: #eef2ff; color: #6366f1; }
    .tag-rose   { background: #fff1f2; color: #f43f5e; }
    .tag-slate  { background: #f1f5f9; color: #64748b; }

    /* ── Daily Trend Chart ────────────────────────────────────────────── */
    .trend-wrap { display: flex; flex-direction: column; gap: 1rem; }
    .trend-svg { width: 100%; height: 180px; display: block; overflow: visible; }
    .legend-row {
      display: flex;
      gap: 1rem;
      font-size: 0.75rem;
      padding-left: 0.25rem;
    }
    .legend-pill { display: flex; align-items: center; gap: 0.35rem; font-weight: 500; }
    .legend-pill.indigo { color: #6366f1; }
    .legend-pill.slate  { color: #64748b; }
    .legend-pill.amber  { color: #f59e0b; }
    .legend-pill.green  { color: #10b981; }

    /* ── Department Bars ──────────────────────────────────────────────── */
    .dept-list { display: flex; flex-direction: column; gap: 1.1rem; }
    .dept-row { display: flex; align-items: center; gap: 0.9rem; }
    .dept-rank {
      width: 22px;
      height: 22px;
      border-radius: 6px;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      font-size: 0.7rem;
      font-weight: 700;
      color: #64748b;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .dept-info { flex: 1; min-width: 0; }
    .dept-name { font-size: 0.85rem; font-weight: 600; color: #1e293b; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .dept-meta { font-size: 0.72rem; color: #94a3b8; }
    .dept-right { display: flex; align-items: center; gap: 0.6rem; min-width: 130px; }
    .dept-track { flex: 1; height: 8px; background: #f1f5f9; border-radius: 99px; overflow: hidden; }
    .dept-fill  { height: 100%; border-radius: 99px; transition: width 0.5s cubic-bezier(.4,0,.2,1); }
    .dept-pct { font-size: 0.8rem; font-weight: 700; color: #374151; min-width: 36px; text-align: right; }
    .fill-green  { background: linear-gradient(90deg, #10b981, #34d399); }
    .fill-amber  { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
    .fill-red    { background: linear-gradient(90deg, #ef4444, #f87171); }

    /* ── Peak Hours ───────────────────────────────────────────────────── */
    .peak-chart {
      display: flex;
      align-items: flex-end;
      gap: 3px;
      height: 150px;
      padding-bottom: 1.5rem;
      border-bottom: 1px solid #f1f5f9;
      margin-bottom: 0.75rem;
      overflow-x: auto;
    }
    .peak-col { display: flex; flex-direction: column; align-items: center; gap: 2px; min-width: 24px; }
    .peak-bars { display: flex; align-items: flex-end; gap: 1px; height: 120px; }
    .peak-bar { width: 8px; border-radius: 3px 3px 0 0; transition: height 0.4s cubic-bezier(.4,0,.2,1); min-height: 2px; }
    .peak-in  { background: #10b981; }
    .peak-out { background: #94a3b8; }
    .peak-lbl { font-size: 0.6rem; color: #94a3b8; font-weight: 500; writing-mode: vertical-rl; transform: rotate(180deg); }

    /* ── Fraud Reasons ────────────────────────────────────────────────── */
    .fraud-list { display: flex; flex-direction: column; gap: 1.1rem; }
    .fraud-row { display: flex; gap: 0.9rem; align-items: flex-start; }
    .fraud-dot { font-size: 0.95rem; color: #f59e0b; margin-top: 0.1rem; flex-shrink: 0; }
    .fraud-body { flex: 1; }
    .fraud-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.4rem; }
    .fraud-lbl { font-size: 0.875rem; font-weight: 600; color: #1e293b; }
    .fraud-pill {
      background: #fff7ed;
      color: #c2410c;
      border: 1px solid #fed7aa;
      padding: 0.1rem 0.6rem;
      border-radius: 99px;
      font-size: 0.75rem;
      font-weight: 700;
    }
    .fraud-track { height: 6px; background: #f1f5f9; border-radius: 99px; overflow: hidden; margin-bottom: 0.3rem; }
    .fraud-fill  { height: 100%; background: linear-gradient(90deg, #f59e0b, #fb923c); border-radius: 99px; transition: width 0.5s cubic-bezier(.4,0,.2,1); }
    .fraud-pct { font-size: 0.7rem; color: #94a3b8; }

    /* ── Employee Table ───────────────────────────────────────────────── */
    .table-wrap { overflow-x: auto; }
    .emp-table { width: 100%; border-collapse: collapse; font-size: 0.875rem; }
    .emp-table thead tr { border-bottom: 1px solid #f1f5f9; }
    .emp-table th {
      padding: 0.75rem 1rem;
      text-align: left;
      font-size: 0.72rem;
      font-weight: 700;
      letter-spacing: 0.06em;
      text-transform: uppercase;
      color: #94a3b8;
      white-space: nowrap;
    }
    .emp-table td { padding: 0.875rem 1rem; border-bottom: 1px solid #f8fafc; vertical-align: middle; }
    .emp-table tbody tr { transition: background 0.15s; }
    .emp-table tbody tr:hover { background: #fafbff; }
    .emp-table tbody tr.row-risk { background: #fffbeb; }
    .emp-table tbody tr.row-risk:hover { background: #fef3c7; }
    .rank-td { color: #94a3b8; font-size: 0.8rem; font-weight: 700; }
    .emp-cell { display: flex; align-items: center; gap: 0.7rem; }
    .avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: linear-gradient(135deg, #6366f1, #818cf8);
      color: white;
      font-size: 0.8rem;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .avatar.avatar-risk { background: linear-gradient(135deg, #f59e0b, #fb923c); }
    .emp-name { font-weight: 600; color: #1e293b; font-size: 0.875rem; }
    .dept-chip {
      background: #f1f5f9;
      color: #475569;
      padding: 0.2rem 0.65rem;
      border-radius: 6px;
      font-size: 0.75rem;
      font-weight: 600;
    }
    .rate-cell { display: flex; align-items: center; gap: 0.6rem; }
    .mini-track { width: 70px; height: 6px; background: #f1f5f9; border-radius: 99px; overflow: hidden; flex-shrink: 0; }
    .mini-fill  { height: 100%; border-radius: 99px; }
    .rate-val { font-size: 0.8rem; font-weight: 700; color: #374151; min-width: 32px; }
    .score-badge {
      padding: 0.25rem 0.7rem;
      border-radius: 8px;
      font-size: 0.82rem;
      font-weight: 800;
      display: inline-block;
    }
    .score-max { font-weight: 500; color: inherit; opacity: 0.6; font-size: 0.75rem; }
    .score-ok   { background: #ecfdf5; color: #065f46; }
    .score-warn { background: #fffbeb; color: #92400e; }
    .score-risk { background: #fff1f2; color: #881337; }
    .center-td  { text-align: center; color: #64748b; font-weight: 600; }
    .alert-pill {
      background: #fff1f2;
      color: #be123c;
      border: 1px solid #fecdd3;
      padding: 0.15rem 0.6rem;
      border-radius: 99px;
      font-size: 0.75rem;
      font-weight: 700;
    }
    .no-alert { color: #cbd5e1; font-size: 0.9rem; }
    .status-chip {
      padding: 0.25rem 0.75rem;
      border-radius: 99px;
      font-size: 0.72rem;
      font-weight: 700;
      letter-spacing: 0.04em;
    }
    .chip-ok   { background: #ecfdf5; color: #065f46; }
    .chip-warn { background: #fffbeb; color: #92400e; }
    .chip-risk { background: #fff1f2; color: #be123c; }

    /* ── Empty State ──────────────────────────────────────────────────── */
    .empty-state {
      text-align: center;
      padding: 3rem 1rem;
      color: #cbd5e1;
      font-size: 0.9rem;
      font-weight: 500;
    }

    /* ── Responsive ───────────────────────────────────────────────────── */
    @media (max-width: 1280px) {
      .kpi-strip { grid-template-columns: repeat(3, 1fr); }
      .charts-grid { grid-template-columns: 1fr 1fr; }
      .widget.span-2 { grid-column: span 2; }
    }
    @media (max-width: 900px) {
      .bi-root { padding: 1rem; }
      .page-header { flex-direction: column; align-items: flex-start; }
      .kpi-strip { grid-template-columns: repeat(2, 1fr); }
      .charts-grid { grid-template-columns: 1fr; }
      .widget.span-2 { grid-column: span 1; }
    }
  `]
})
export class BiDashboardComponent implements OnInit {
  private biService = inject(BiService);
  private destroyRef = inject(DestroyRef);

  dailyTrends: DailyTrendDto[] = [];
  departmentStats: DepartmentStatDto[] = [];
  peakHours: PeakHourDto[] = [];
  fraudMetrics: FraudMetricsDto | null = null;
  employeeReliability: EmployeeReliabilityDto[] = [];
  summaryMetrics: any = null;

  filterStartDate = this.getDateString(new Date(Date.now() - 30 * 24 * 60 * 60 * 1000));
  filterEndDate = this.getDateString(new Date());

  maxPeakCount = 0;

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
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
        this.maxPeakCount = Math.max(...data.map(h => Math.max(h.checkInCount, h.checkOutCount)), 1);
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
    return reason.split('_').map(w => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase()).join(' ');
  }

  formatHour(hour: number): string {
    return `${hour.toString().padStart(2, '0')}:00`;
  }

  getTrendPoints(): string {
    if (this.dailyTrends.length === 0) return '';
    const max = Math.max(...this.dailyTrends.map(d => d.checkIns), 1);
    const W = 600, H = 160, pad = 10;
    const step = W / (this.dailyTrends.length - 1 || 1);
    return this.dailyTrends.map((t, i) => `${i * step},${pad + (1 - t.checkIns / max) * (H - pad * 2)}`).join(' ');
  }

  getTrendAreaPoints(): string {
    if (this.dailyTrends.length === 0) return '';
    const max = Math.max(...this.dailyTrends.map(d => d.checkIns), 1);
    const W = 600, H = 160, pad = 10;
    const step = W / (this.dailyTrends.length - 1 || 1);
    const linePoints = this.dailyTrends.map((t, i) => `${i * step},${pad + (1 - t.checkIns / max) * (H - pad * 2)}`).join(' ');
    const lastX = (this.dailyTrends.length - 1) * step;
    return `0,${H} ${linePoints} ${lastX},${H}`;
  }

  getTrendPointsArray(): { x: number; y: number }[] {
    if (this.dailyTrends.length === 0) return [];
    const max = Math.max(...this.dailyTrends.map(d => d.checkIns), 1);
    const W = 600, H = 160, pad = 10;
    const step = W / (this.dailyTrends.length - 1 || 1);
    return this.dailyTrends.map((t, i) => ({
      x: i * step,
      y: pad + (1 - t.checkIns / max) * (H - pad * 2)
    }));
  }

  private getDateString(date: Date): string {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }
}
