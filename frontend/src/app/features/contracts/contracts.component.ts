import { Component, OnInit, inject, ViewChild } from '@angular/core';
import { CommonModule }           from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatButtonModule }        from '@angular/material/button';
import { MatIconModule }          from '@angular/material/icon';
import { MatCardModule }          from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule }       from '@angular/material/tooltip';
import { MatDialog }              from '@angular/material/dialog';
import { ContratApiService }      from '../../core/api/contract.service';
import { ContractFormComponent }  from './contract-form/contract-form.component';
import type { Contrat }           from '../../core/models';

@Component({
  selector: 'app-contracts',
  templateUrl: './contracts.component.html',
  styleUrl:    './contracts.component.scss',
  imports: [
    CommonModule,
    MatTableModule, MatPaginatorModule, MatButtonModule,
    MatIconModule, MatCardModule, MatProgressSpinnerModule, MatTooltipModule,
  ],
})
export class ContractsComponent implements OnInit {
  private api    = inject(ContratApiService);
  private dialog = inject(MatDialog);

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  dataSource    = new MatTableDataSource<Contrat>();
  displayedCols = ['employe', 'type', 'dateDebut', 'dateFin', 'salaire', 'actions'];
  loading       = true;
  totalElements = 0;
  pageSize      = 20;

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

  openForm(contrat?: Contrat): void {
    this.dialog.open(ContractFormComponent, { width: '560px', data: { contrat } })
      .afterClosed().subscribe((saved: Contrat | undefined) => {
        if (!saved) return;
        if (contrat) {
          const data = [...this.dataSource.data];
          const idx  = data.findIndex(c => c.id === saved.id);
          if (idx >= 0) { data[idx] = saved; this.dataSource.data = data; }
        } else {
          this.load(0);
        }
      });
  }
}
