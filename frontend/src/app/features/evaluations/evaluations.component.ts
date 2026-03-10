import { Component, OnInit, inject, ViewChild } from '@angular/core';
import { CommonModule }           from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatButtonModule }        from '@angular/material/button';
import { MatIconModule }          from '@angular/material/icon';
import { MatCardModule }          from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog }              from '@angular/material/dialog';
import { MatTooltipModule }       from '@angular/material/tooltip';
import { MatSnackBar }            from '@angular/material/snack-bar';
import { EvaluationApiService }   from '../../core/api/evaluation.service';
import { EmployeApiService }      from '../../core/api/employee.service';
import { AuthService }            from '../../core/services/auth.service';
import { EvaluationFormComponent } from './evaluation-form/evaluation-form.component';
import type { Evaluation }        from '../../core/models';

@Component({
  selector: 'app-evaluations',
  templateUrl: './evaluations.component.html',
  styleUrl:    './evaluations.component.scss',
  imports: [
    CommonModule,
    MatTableModule, MatPaginatorModule, MatButtonModule,
    MatIconModule, MatCardModule, MatProgressSpinnerModule, MatTooltipModule,
  ],
})
export class EvaluationsComponent implements OnInit {
  private api    = inject(EvaluationApiService);
  private empApi = inject(EmployeApiService);
  private auth   = inject(AuthService);
  private dialog = inject(MatDialog);
  private snack  = inject(MatSnackBar);

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  dataSource    = new MatTableDataSource<Evaluation>();
  loading       = true;
  totalElements = 0;
  pageSize      = 20;
  isRhAdmin     = false;
  currentEmpId  = 0;

  get displayedCols(): string[] {
    const cols = ['employe', 'objectifs', 'kpi', 'score', 'dateEvaluation'];
    if (this.isRhAdmin) cols.push('actions');
    return cols;
  }

  ngOnInit(): void {
    const role = this.auth.currentUserRole();
    this.isRhAdmin = role === 'ADMIN' || role === 'RH';
    if (this.isRhAdmin) {
      this.load(0);
    } else {
      // EMPLOYEE: use /api/employees/me to find own record
      this.empApi.getMe().subscribe({
        next: emp => { this.currentEmpId = emp.id; this.loadByEmployee(0); },
        error: () => { this.loading = false; },
      });
    }
  }

  load(page: number): void {
    this.loading = true;
    this.api.getAll({ page, size: this.pageSize }).subscribe({
      next: p => { this.dataSource.data = p.content; this.totalElements = p.totalElements; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  loadByEmployee(page: number): void {
    this.loading = true;
    this.api.getByEmployee(this.currentEmpId, { page, size: this.pageSize }).subscribe({
      next: p => { this.dataSource.data = p.content; this.totalElements = p.totalElements; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  onPage(e: PageEvent): void {
    if (this.isRhAdmin) { this.load(e.pageIndex); }
    else { this.loadByEmployee(e.pageIndex); }
  }

  openForm(evaluation?: Evaluation): void {
    this.dialog.open(EvaluationFormComponent, { width: '560px', data: { evaluation } })
      .afterClosed().subscribe((saved: Evaluation | undefined) => {
        if (!saved) return;
        if (evaluation) {
          const data = [...this.dataSource.data];
          const idx  = data.findIndex(e => e.id === saved.id);
          if (idx >= 0) { data[idx] = saved; this.dataSource.data = data; }
        } else {
          this.load(0);
        }
      });
  }

  truncate(s: string | undefined, n = 50): string {
    if (!s) return '-';
    return s.length > n ? s.slice(0, n) + '...' : s;
  }
}
