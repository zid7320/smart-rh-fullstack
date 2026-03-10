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
import { PlanningApiService }     from '../../core/api/planning.service';
import { PlanningFormComponent }  from './planning-form/planning-form.component';
import type { Planning }          from '../../core/models';

@Component({
  selector: 'app-planning',
  templateUrl: './planning.component.html',
  styleUrl:    './planning.component.scss',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule, MatPaginatorModule, MatButtonModule,
    MatIconModule, MatCardModule, MatProgressSpinnerModule, MatTooltipModule,
  ],
})
export class PlanningComponent implements OnInit {
  private api    = inject(PlanningApiService);
  private dialog = inject(MatDialog);
  private snack  = inject(MatSnackBar);

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  dataSource    = new MatTableDataSource<Planning>();
  loading       = true;
  totalElements = 0;
  pageSize      = 20;
  displayedCols = ['employe', 'type', 'horaires', 'dateDebut', 'dateFin', 'actions'];

  ngOnInit(): void { this.load(0); }

  load(page: number): void {
    this.loading = true;
    this.api.getAll({ page, size: this.pageSize }).subscribe({
      next: p => { this.dataSource.data = p.content; this.totalElements = p.totalElements; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  onPage(e: PageEvent): void { this.load(e.pageIndex); }

  openForm(planning?: Planning): void {
    this.dialog.open(PlanningFormComponent, { width: '560px', data: { planning } })
      .afterClosed().subscribe((saved: Planning | undefined) => {
        if (!saved) return;
        if (planning) {
          const data = [...this.dataSource.data];
          const idx  = data.findIndex(p => p.id === saved.id);
          if (idx >= 0) { data[idx] = saved; this.dataSource.data = data; }
        } else {
          this.load(0);
        }
      });
  }

  delete(p: Planning): void {
    if (!confirm('Supprimer cette entree planning ?')) return;
    this.api.delete(p.id).subscribe({
      next: () => {
        this.dataSource.data = this.dataSource.data.filter(x => x.id !== p.id);
        this.snack.open('Entree supprimee.', 'OK', { duration: 3000 });
      },
      error: () => this.snack.open('Erreur lors de la suppression.', 'OK', { duration: 3000 }),
    });
  }
}
