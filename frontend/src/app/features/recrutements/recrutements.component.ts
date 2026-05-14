import { Component, ViewChild, OnInit, AfterViewInit, inject } from '@angular/core';
import { CommonModule }       from '@angular/common';
import { Router }             from '@angular/router';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule }   from '@angular/material/paginator';
import { MatButtonModule }    from '@angular/material/button';
import { MatIconModule }      from '@angular/material/icon';
import { MatTooltipModule }   from '@angular/material/tooltip';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatCardModule }      from '@angular/material/card';
import { MatChipsModule }     from '@angular/material/chips';
import { MatTabsModule }      from '@angular/material/tabs';
import { MatDialog }          from '@angular/material/dialog';
import { MatSnackBar }        from '@angular/material/snack-bar';
import { RecrutementApiService, Resume, CandidateAnalysis } from '../../core/api/recruitment.service';
import { RecrutementFormComponent } from './recrutement-form/recrutement-form.component';
import { HireDialogComponent }      from './hire-dialog/hire-dialog.component';
import type { Recrutement, Employe } from '../../core/models';

@Component({
  selector: 'app-recrutements',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule, MatPaginatorModule,
    MatButtonModule, MatIconModule, MatTooltipModule,
    MatProgressBarModule, MatCardModule, MatChipsModule, MatTabsModule,
  ],
  templateUrl: './recrutements.component.html',
  styleUrl:    './recrutements.component.scss',
})
export class RecrutementsComponent implements OnInit, AfterViewInit {
  private api    = inject(RecrutementApiService);
  private dialog = inject(MatDialog);
  private snack  = inject(MatSnackBar);
  private router = inject(Router);

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  // Recruitment tab
  displayedColumns = ['posteCible', 'statut', 'responsableNom', 'actions'];
  dataSource    = new MatTableDataSource<Recrutement>();
  totalElements = 0;
  pageSize      = 20;
  loading       = false;

  // Resumes tab
  resumesColumns = ['fullName', 'email', 'phone', 'senderEmail', 'receivedAt', 'actions'];
  resumesDataSource = new MatTableDataSource<Resume>();
  resumesLoading = false;
  resumesCount = 0;

  // Candidate Analysis tab
  analysisColumns = ['candidateName', 'email', 'matchScore', 'overallScore', 'recommendation', 'rankPosition', 'actions'];
  analysisDataSource = new MatTableDataSource<CandidateAnalysis>();
  analysisLoading = false;
  analysisCount = 0;

  selectedTabIndex = 0;

  ngOnInit(): void { 
    this.load();
    this.loadResumes();
    this.loadCandidateAnalyses();
  }

  ngAfterViewInit(): void { 
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

  loadResumes(): void {
    this.resumesLoading = true;
    this.api.getAllResumes().subscribe({
      next: (resumes) => {
        this.resumesDataSource.data = resumes;
        this.resumesCount = resumes.length;
        this.resumesLoading = false;
      },
      error: () => { this.resumesLoading = false; },
    });
  }

  loadCandidateAnalyses(): void {
    this.analysisLoading = true;
    this.api.getAllCandidateAnalyses().subscribe({
      next: (analyses) => {
        this.analysisDataSource.data = analyses;
        this.analysisCount = analyses.length;
        this.analysisLoading = false;
      },
      error: () => { this.analysisLoading = false; },
    });
  }

  deleteResume(id: number): void {
    if (confirm('Are you sure you want to delete this resume?')) {
      this.api.deleteResume(id).subscribe({
        next: () => {
          this.snack.open('Resume deleted successfully', 'Close', { duration: 3000 });
          this.loadResumes();
        },
        error: () => {
          this.snack.open('Error deleting resume', 'Close', { duration: 3000 });
        },
      });
    }
  }

  deleteCandidateAnalysis(id: number): void {
    if (confirm('Are you sure you want to delete this analysis?')) {
      this.api.deleteCandidateAnalysis(id).subscribe({
        next: () => {
          this.snack.open('Analysis deleted successfully', 'Close', { duration: 3000 });
          this.loadCandidateAnalyses();
        },
        error: () => {
          this.snack.open('Error deleting analysis', 'Close', { duration: 3000 });
        },
      });
    }
  }

  openForm(r?: Recrutement): void {
    this.dialog.open(RecrutementFormComponent, { width: '500px', data: r ?? null })
      .afterClosed().subscribe(saved => { if (saved) this.load(); });
  }

  openHire(r: Recrutement): void {
    this.dialog.open(HireDialogComponent, { width: '640px', data: r, disableClose: false })
      .afterClosed().subscribe((emp: Employe | undefined) => {
        if (!emp) return;
        this.load();
        this.snack.open(emp.nom + ' ' + emp.prenom + ' embauché(e) !', 'Voir', { duration: 7000 })
          .onAction().subscribe(() => this.router.navigate(['/employees', emp.id]));
      });
  }

  statusClass(s: string): string {
    return 'chip-' + s.toLowerCase().replace('_', '-');
  }

  recommendationClass(recommendation?: string): string {
    if (!recommendation) return 'chip-neutral';
    return 'chip-' + recommendation.toLowerCase().replace('_', '-');
  }
}

