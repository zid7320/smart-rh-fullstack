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
import { FormationApiService }    from '../../core/api/training.service';
import { EmployeApiService }      from '../../core/api/employee.service';
import { AuthService }            from '../../core/services/auth.service';
import { FormationFormComponent } from './formation-form/formation-form.component';
import type { Formation }         from '../../core/models';

@Component({
  selector: 'app-formations',
  templateUrl: './formations.component.html',
  styleUrl:    './formations.component.scss',
  imports: [
    CommonModule,
    MatTableModule, MatPaginatorModule, MatButtonModule,
    MatIconModule, MatCardModule, MatProgressSpinnerModule, MatTooltipModule,
  ],
})
export class FormationsComponent implements OnInit {
  private api    = inject(FormationApiService);
  private empApi = inject(EmployeApiService);
  private auth   = inject(AuthService);
  private dialog = inject(MatDialog);

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  dataSource    = new MatTableDataSource<Formation>();
  loading       = true;
  totalElements = 0;
  pageSize      = 20;
  isRhAdmin     = false;
  currentEmpId  = 0;

  get displayedCols(): string[] {
    const cols = ['employe', 'titre', 'certification', 'organisme', 'dates'];
    if (this.isRhAdmin) cols.push('actions');
    return cols;
  }

  ngOnInit(): void {
    const role = this.auth.currentUserRole();
    this.isRhAdmin = role === 'ADMIN' || role === 'RH';
    if (this.isRhAdmin) {
      this.load(0);
    } else {
      const email = this.auth.currentUser()?.email ?? '';
      this.empApi.getAll({ search: email, size: 5 }).subscribe(p => {
        const me = p.content.find(e => e.email === email);
        if (me) { this.currentEmpId = me.id; this.loadByEmployee(0); }
        else { this.loading = false; }
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
    if (this.isRhAdmin) this.load(e.pageIndex);
    else this.loadByEmployee(e.pageIndex);
  }

  openForm(formation?: Formation): void {
    this.dialog.open(FormationFormComponent, { width: '560px', data: { formation } })
      .afterClosed().subscribe((saved: Formation | undefined) => {
        if (!saved) return;
        if (formation) {
          const data = [...this.dataSource.data];
          const idx  = data.findIndex(f => f.id === saved.id);
          if (idx >= 0) { data[idx] = saved; this.dataSource.data = data; }
        } else {
          this.load(0);
        }
      });
  }
}
