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
import { DossierRhApiService }    from '../../core/api/dossier.service';
import { DossierEditDialogComponent } from './dossier-edit-dialog/dossier-edit-dialog.component';
import type { DossierRH }         from '../../core/models';

@Component({
  selector: 'app-dossiers-rh',
  templateUrl: './dossiers-rh.component.html',
  styleUrl:    './dossiers-rh.component.scss',
  imports: [
    CommonModule,
    MatTableModule, MatPaginatorModule, MatButtonModule,
    MatIconModule, MatCardModule, MatProgressSpinnerModule, MatTooltipModule,
  ],
})
export class DossiersRhComponent implements OnInit {
  private api    = inject(DossierRhApiService);
  private dialog = inject(MatDialog);

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  dataSource     = new MatTableDataSource<DossierRH>();
  displayedCols  = ['id', 'employe', 'infosPerso', 'diplomes', 'actions'];
  loading        = true;
  totalElements  = 0;
  pageSize       = 20;

  ngOnInit(): void { this.load(0); }

  load(page: number): void {
    this.loading = true;
    this.api.getAll({ page, size: this.pageSize }).subscribe({
      next: p => {
        this.dataSource.data = p.content;
        this.totalElements   = p.totalElements;
        this.loading         = false;
      },
      error: () => { this.loading = false; }
    });
  }

  onPage(e: PageEvent): void { this.load(e.pageIndex); }

  openEdit(dossier: DossierRH): void {
    this.dialog.open(DossierEditDialogComponent, { width: '560px', data: dossier })
      .afterClosed().subscribe((updated: DossierRH | undefined) => {
        if (!updated) return;
        const data = [...this.dataSource.data];
        const idx  = data.findIndex(d => d.id === updated.id);
        if (idx >= 0) { data[idx] = updated; this.dataSource.data = data; }
      });
  }

  truncate(s: string | undefined, n = 60): string {
    if (!s) return '—';
    return s.length > n ? s.slice(0, n) + '…' : s;
  }
}
