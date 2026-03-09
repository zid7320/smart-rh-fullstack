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
import { PaieApiService }     from '../../core/api/payroll.service';
import { AuthService }        from '../../core/services/auth.service';
import { GenerateDialogComponent } from './generate-dialog/generate-dialog.component';
import type { Paie }          from '../../core/models';

const MOIS = ['','Janvier','Février','Mars','Avril','Mai','Juin','Juillet','Août','Septembre','Octobre','Novembre','Décembre'];

@Component({
  selector: 'app-paie',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule, MatPaginatorModule, MatSortModule,
    MatButtonModule, MatIconModule, MatTooltipModule,
    MatProgressBarModule, MatCardModule, MatProgressSpinnerModule,
  ],
  templateUrl: './paie.component.html',
  styleUrl:    './paie.component.scss',
})
export class PaieComponent implements OnInit, AfterViewInit {
  private api   = inject(PaieApiService);
  private auth  = inject(AuthService);
  private dialog = inject(MatDialog);
  private snack  = inject(MatSnackBar);

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort)      sort!: MatSort;

  isRhAdmin = false;
  displayedColumns = ['employeNomComplet', 'periode', 'montant', 'actions'];
  dataSource    = new MatTableDataSource<Paie>();
  totalElements = 0;
  pageSize      = 20;
  loading       = false;
  downloading   = new Set<number>();

  ngOnInit(): void {
    const role = this.auth.currentUserRole();
    this.isRhAdmin = role === 'ADMIN' || role === 'RH';
    this.load();
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
    this.paginator.page.subscribe(() => this.load());
  }

  load(): void {
    this.loading = true;
    this.api.getAll({ page: this.paginator?.pageIndex ?? 0, size: this.paginator?.pageSize ?? this.pageSize })
      .subscribe({
        next: pg => { this.dataSource.data = pg.content; this.totalElements = pg.totalElements; this.loading = false; },
        error: ()  => { this.loading = false; },
      });
  }

  openGenerate(): void {
    this.dialog.open(GenerateDialogComponent, { width: '400px' })
      .afterClosed().subscribe(generated => {
        if (generated) {
          this.snack.open(generated + ' bulletin(s) généré(s)', 'OK', { duration: 4000 });
          this.load();
        }
      });
  }

  downloadPdf(paie: Paie): void {
    this.downloading.add(paie.id);
    this.api.downloadPdf(paie.id).subscribe({
      next: (blob: Blob) => {
        const url  = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href     = url;
        link.download = 'bulletin-' + MOIS[paie.mois] + '-' + paie.annee + '.pdf';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
        this.downloading.delete(paie.id);
      },
      error: () => {
        this.downloading.delete(paie.id);
        this.snack.open('Erreur lors du téléchargement du PDF', 'OK', { duration: 4000 });
      },
    });
  }

  moisLabel(m: number): string { return MOIS[m] ?? String(m); }
  isDownloading(id: number): boolean { return this.downloading.has(id); }
}
