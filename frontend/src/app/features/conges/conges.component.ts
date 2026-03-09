import { Component, ViewChild, OnInit, AfterViewInit, inject } from '@angular/core';
import { CommonModule }       from '@angular/common';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule }   from '@angular/material/paginator';
import { MatSort, MatSortModule }             from '@angular/material/sort';
import { MatButtonModule }    from '@angular/material/button';
import { MatIconModule }      from '@angular/material/icon';
import { MatTooltipModule }   from '@angular/material/tooltip';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule }      from '@angular/material/card';
import { MatDialog }          from '@angular/material/dialog';
import { MatSnackBar }        from '@angular/material/snack-bar';
import { CongeApiService }    from '../../core/api/leave.service';
import { EmployeApiService }  from '../../core/api/employee.service';
import { AuthService }        from '../../core/services/auth.service';
import { CongeFormComponent } from './conge-form/conge-form.component';
import type { Conge }         from '../../core/models';

@Component({
  selector: 'app-conges',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule, MatPaginatorModule, MatSortModule,
    MatButtonModule, MatIconModule, MatTooltipModule,
    MatProgressBarModule, MatCardModule, MatProgressSpinnerModule,
  ],
  templateUrl: './conges.component.html',
  styleUrl:    './conges.component.scss',
})
export class CongesComponent implements OnInit, AfterViewInit {
  private api     = inject(CongeApiService);
  private empApi  = inject(EmployeApiService);
  private auth    = inject(AuthService);
  private dialog  = inject(MatDialog);
  private snack   = inject(MatSnackBar);

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort)      sort!: MatSort;

  isRhAdmin    = false;
  currentEmpId: number | null = null;

  displayedColumns: string[] = [];
  dataSource    = new MatTableDataSource<Conge>();
  totalElements = 0;
  pageSize      = 20;
  loading       = false;
  acting        = new Set<number>();   // row IDs being processed

  ngOnInit(): void {
    const role = this.auth.currentUserRole();
    this.isRhAdmin = role === 'ADMIN' || role === 'RH';

    if (this.isRhAdmin) {
      this.displayedColumns = ['employeNomComplet', 'type', 'dateDebut', 'dateFin', 'statut', 'actions'];
      this.load();
    } else {
      // EMPLOYEE: find own employee record by email, then load own leaves
      const email = this.auth.currentUser()?.email ?? '';
      this.displayedColumns = ['type', 'dateDebut', 'dateFin', 'statut'];
      this.empApi.getAll({ search: email, size: 5 }).subscribe({
        next: pg => {
          const mine = pg.content.find(e => e.email === email);
          if (mine) { this.currentEmpId = mine.id; this.load(); }
          else { this.loading = false; }
        },
        error: () => { this.loading = false; },
      });
    }
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
      ? this.api.getAll({ page, size })
      : this.api.getByEmployee(this.currentEmpId!, { page, size });

    obs$.subscribe({
      next: pg => { this.dataSource.data = pg.content; this.totalElements = pg.totalElements; this.loading = false; },
      error: ()  => { this.loading = false; },
    });
  }

  openForm(): void {
    this.dialog.open(CongeFormComponent, { width: '500px', data: { currentEmpId: this.currentEmpId, isRhAdmin: this.isRhAdmin } })
      .afterClosed().subscribe(saved => { if (saved) this.load(); });
  }

  approve(c: Conge): void {
    this.acting.add(c.id);
    this.api.approve(c.id).subscribe({
      next: updated => { this.replaceRow(updated); this.acting.delete(c.id); this.snack.open('Congé approuvé', 'OK', { duration: 3000 }); },
      error: ()     => { this.acting.delete(c.id); this.snack.open('Erreur', 'OK', { duration: 3000 }); },
    });
  }

  reject(c: Conge): void {
    this.acting.add(c.id);
    this.api.reject(c.id).subscribe({
      next: updated => { this.replaceRow(updated); this.acting.delete(c.id); this.snack.open('Congé rejeté', 'OK', { duration: 3000 }); },
      error: ()     => { this.acting.delete(c.id); this.snack.open('Erreur', 'OK', { duration: 3000 }); },
    });
  }

  private replaceRow(updated: Conge): void {
    const data = [...this.dataSource.data];
    const idx  = data.findIndex(c => c.id === updated.id);
    if (idx >= 0) { data[idx] = updated; this.dataSource.data = data; }
  }

  statusClass(s: string): string { return 'chip-' + s.toLowerCase(); }
  isActing(id: number):   boolean { return this.acting.has(id); }
}
