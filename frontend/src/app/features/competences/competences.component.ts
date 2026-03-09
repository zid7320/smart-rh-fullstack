import { Component, ViewChild, OnInit, AfterViewInit, inject } from '@angular/core';
import { CommonModule }       from '@angular/common';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule }   from '@angular/material/paginator';
import { MatButtonModule }    from '@angular/material/button';
import { MatIconModule }      from '@angular/material/icon';
import { MatTooltipModule }   from '@angular/material/tooltip';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatCardModule }      from '@angular/material/card';
import { MatDialog }          from '@angular/material/dialog';
import { MatSnackBar }        from '@angular/material/snack-bar';
import { CompetenceApiService } from '../../core/api/competence.service';
import { CompetenceFormComponent } from './competence-form/competence-form.component';
import type { Competence }    from '../../core/models';

@Component({
  selector: 'app-competences',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule, MatPaginatorModule,
    MatButtonModule, MatIconModule, MatTooltipModule,
    MatProgressBarModule, MatCardModule,
  ],
  templateUrl: './competences.component.html',
  styleUrl:    './competences.component.scss',
})
export class CompetencesComponent implements OnInit, AfterViewInit {
  private api    = inject(CompetenceApiService);
  private dialog = inject(MatDialog);
  private snack  = inject(MatSnackBar);

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  displayedColumns = ['nom', 'niveau', 'actions'];
  dataSource    = new MatTableDataSource<Competence>();
  totalElements = 0;
  pageSize      = 20;
  loading       = false;

  ngOnInit():        void { this.load(); }
  ngAfterViewInit(): void { this.paginator.page.subscribe(() => this.load()); }

  load(): void {
    this.loading = true;
    this.api.getAll({ page: this.paginator?.pageIndex ?? 0, size: this.paginator?.pageSize ?? this.pageSize })
      .subscribe({
        next: pg => { this.dataSource.data = pg.content; this.totalElements = pg.totalElements; this.loading = false; },
        error: ()  => { this.loading = false; },
      });
  }

  openForm(comp?: Competence): void {
    this.dialog.open(CompetenceFormComponent, { width: '420px', data: comp ?? null })
      .afterClosed().subscribe(saved => { if (saved) this.load(); });
  }
}
