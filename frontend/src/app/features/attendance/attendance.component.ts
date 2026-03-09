import {
  Component, ViewChild, OnInit, AfterViewInit, inject, DestroyRef,
} from '@angular/core';
import { CommonModule }             from '@angular/common';
import { takeUntilDestroyed }       from '@angular/core/rxjs-interop';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule }   from '@angular/material/paginator';
import { MatSort, MatSortModule }             from '@angular/material/sort';
import { MatButtonModule }          from '@angular/material/button';
import { MatIconModule }            from '@angular/material/icon';
import { MatTooltipModule }         from '@angular/material/tooltip';
import { MatProgressBarModule }     from '@angular/material/progress-bar';
import { MatCardModule }            from '@angular/material/card';
import { MatDialog }                from '@angular/material/dialog';
import { MatSnackBar }              from '@angular/material/snack-bar';
import { AttendanceApiService }     from '../../core/api/attendance.service';
import { EmployeApiService }        from '../../core/api/employee.service';
import { WebsocketService }         from '../../core/websocket/websocket.service';
import { AuthService }              from '../../core/services/auth.service';
import { SimulateDialogComponent }  from './simulate-dialog/simulate-dialog.component';
import type { AttendanceRecord }    from '../../core/models';

@Component({
  selector: 'app-attendance',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule, MatPaginatorModule, MatSortModule,
    MatButtonModule, MatIconModule, MatTooltipModule,
    MatProgressBarModule, MatCardModule,
  ],
  templateUrl: './attendance.component.html',
  styleUrl:    './attendance.component.scss',
})
export class AttendanceComponent implements OnInit, AfterViewInit {
  private api        = inject(AttendanceApiService);
  private empApi     = inject(EmployeApiService);
  private ws         = inject(WebsocketService);
  private auth       = inject(AuthService);
  private dialog     = inject(MatDialog);
  private snack      = inject(MatSnackBar);
  private destroyRef = inject(DestroyRef);

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort)      sort!: MatSort;

  isRhAdmin    = false;
  currentEmpId: number | null = null;
  liveCount    = 0;
  loading      = false;

  displayedColumns: string[] = [];
  dataSource    = new MatTableDataSource<AttendanceRecord>();
  totalElements = 0;
  pageSize      = 20;

  get isWsConnected(): boolean { return this.ws.connected; }

  ngOnInit(): void {
    const role  = this.auth.currentUserRole();
    this.isRhAdmin = role === 'ADMIN' || role === 'RH';

    if (this.isRhAdmin) {
      this.displayedColumns = ['employeNomComplet', 'type', 'clockedAt', 'confidence', 'siteId'];
      this.load();
    } else {
      // EMPLOYEE: look up own employee record by email, then load own history
      this.displayedColumns = ['type', 'clockedAt', 'confidence', 'siteId'];
      const email = this.auth.currentUser()?.email ?? '';
      this.loading = true;
      this.empApi.getAll({ search: email, size: 5 }).subscribe({
        next: pg => {
          const mine = pg.content.find(e => e.email === email);
          if (mine) { this.currentEmpId = mine.id; this.load(); }
          else { this.loading = false; }
        },
        error: () => { this.loading = false; },
      });
    }

    // Subscribe to realtime stream for the lifetime of this component
    this.ws.attendance$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(event => this.onLiveEvent(event));
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
    this.paginator.page.subscribe(() => this.load());
  }

  load(): void {
    this.loading = true;
    const page = this.paginator?.pageIndex ?? 0;
    const size = this.paginator?.pageSize  ?? this.pageSize;

    const obs$ = this.isRhAdmin
      ? this.api.getHistory({ page, size })
      : this.api.getByEmployee(this.currentEmpId!, { page, size });

    obs$.subscribe({
      next: pg => {
        this.dataSource.data = pg.content;
        this.totalElements   = pg.totalElements;
        this.loading         = false;
      },
      error: () => { this.loading = false; },
    });
  }

  private onLiveEvent(event: AttendanceRecord): void {
    // EMPLOYEE: ignore events not belonging to them
    if (!this.isRhAdmin && event.employeId !== this.currentEmpId) return;

    // Prepend to table only when viewing page 0 (most recent first)
    if ((this.paginator?.pageIndex ?? 0) === 0) {
      const patch = [event, ...this.dataSource.data].slice(0, this.pageSize);
      this.dataSource.data = patch;
      this.totalElements   = this.totalElements + 1;
    }

    this.liveCount++;

    const label = (event.employeNomComplet ?? 'Employé') + ' — ' +
                  (event.type === 'IN' ? 'Entrée' : 'Sortie');
    this.snack.open(label, 'OK', {
      duration:    4500,
      panelClass: event.type === 'IN' ? 'snack-ws-in' : 'snack-ws-out',
    });
  }

  openSimulate(): void {
    this.dialog.open(SimulateDialogComponent, { width: '460px' })
      .afterClosed().subscribe(submitted => { if (submitted) this.load(); });
  }

  resetLiveCount(): void { this.liveCount = 0; }

  typeClass(type: string): string {
    return type === 'IN' ? 'chip-in' : 'chip-out';
  }

  formatConfidence(v: number | undefined): string {
    return v != null ? (v * 100).toFixed(1) + ' %' : '—';
  }
}
