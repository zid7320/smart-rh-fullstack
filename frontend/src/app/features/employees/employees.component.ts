import { Component, ViewChild, OnInit, AfterViewInit, inject } from '@angular/core';
import { CommonModule }       from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink }         from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule }     from '@angular/material/input';
import { MatButtonModule }    from '@angular/material/button';
import { MatIconModule }      from '@angular/material/icon';
import { MatTooltipModule }   from '@angular/material/tooltip';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatCardModule }      from '@angular/material/card';
import { MatDialog }          from '@angular/material/dialog';
import { MatSnackBar }        from '@angular/material/snack-bar';
import { EmployeApiService }  from '../../core/api/employee.service';
import { EmployeeFormComponent }   from './employee-form/employee-form.component';
import { ConfirmDialogComponent }  from '../../shared/components/confirm-dialog/confirm-dialog.component';
import type { Employe } from '../../core/models';

@Component({
  selector: 'app-employees',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatTableModule, MatPaginatorModule, MatSortModule,
    MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatTooltipModule,
    MatProgressBarModule, MatCardModule,
  ],
  templateUrl: './employees.component.html',
  styleUrl:    './employees.component.scss',
})
export class EmployeesComponent implements OnInit, AfterViewInit {
  private api    = inject(EmployeApiService);
  private dialog = inject(MatDialog);
  private snack  = inject(MatSnackBar);

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort)      sort!: MatSort;

  displayedColumns = ['nom', 'prenom', 'email', 'posteLibelle', 'actions'];
  dataSource    = new MatTableDataSource<Employe>();
  totalElements = 0;
  pageSize      = 20;
  loading       = false;
  searchControl = new FormControl('');

  ngOnInit(): void {
    this.searchControl.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged())
      .subscribe(() => { if (this.paginator) this.paginator.pageIndex = 0; this.load(); });
    this.load();
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
    this.paginator.page.subscribe(() => this.load());
  }

  load(): void {
    this.loading = true;
    const q = this.searchControl.value?.trim();
    this.api.getAll({
      page:   this.paginator?.pageIndex ?? 0,
      size:   this.paginator?.pageSize  ?? this.pageSize,
      search: q || undefined,
    }).subscribe({
      next: pg => { this.dataSource.data = pg.content; this.totalElements = pg.totalElements; this.loading = false; },
      error: ()  => { this.loading = false; },
    });
  }

  openForm(emp?: Employe): void {
    this.dialog.open(EmployeeFormComponent, { width: '560px', data: emp ?? null })
      .afterClosed().subscribe(saved => { if (saved) this.load(); });
  }

  delete(emp: Employe): void {
    this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Supprimer employé', message: 'Supprimer ' + emp.nom + ' ' + emp.prenom + ' ?', confirmLabel: 'Supprimer' }
    }).afterClosed().subscribe(ok => {
      if (!ok) return;
      this.api.delete(emp.id).subscribe({
        next: () => { this.snack.open('Employé supprimé', 'OK', { duration: 3000 }); this.load(); },
        error: ()  => this.snack.open('Erreur lors de la suppression', 'OK', { duration: 4000 }),
      });
    });
  }
}
