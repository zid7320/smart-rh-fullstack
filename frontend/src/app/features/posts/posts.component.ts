import { Component, ViewChild, OnInit, AfterViewInit, inject } from '@angular/core';
import { CommonModule }       from '@angular/common';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule }   from '@angular/material/paginator';
import { MatButtonModule }    from '@angular/material/button';
import { MatIconModule }      from '@angular/material/icon';
import { MatTooltipModule }   from '@angular/material/tooltip';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatCardModule }      from '@angular/material/card';
import { MatChipsModule }     from '@angular/material/chips';
import { MatDialog }          from '@angular/material/dialog';
import { MatSnackBar }        from '@angular/material/snack-bar';
import { PosteApiService }    from '../../core/api/post.service';
import { PostFormComponent }       from './post-form/post-form.component';
import { PostCompetencesComponent } from './post-competences/post-competences.component';
import { ConfirmDialogComponent }  from '../../shared/components/confirm-dialog/confirm-dialog.component';
import type { Poste }         from '../../core/models';

@Component({
  selector: 'app-posts',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule, MatPaginatorModule,
    MatButtonModule, MatIconModule, MatTooltipModule,
    MatProgressBarModule, MatCardModule, MatChipsModule,
  ],
  templateUrl: './posts.component.html',
  styleUrl:    './posts.component.scss',
})
export class PostsComponent implements OnInit, AfterViewInit {
  private api    = inject(PosteApiService);
  private dialog = inject(MatDialog);
  private snack  = inject(MatSnackBar);

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  displayedColumns = ['titre', 'competencesRequises', 'competenceNoms', 'actions'];
  dataSource    = new MatTableDataSource<Poste>();
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

  openForm(post?: Poste): void {
    this.dialog.open(PostFormComponent, { width: '560px', data: post ?? null })
      .afterClosed().subscribe(saved => { if (saved) this.load(); });
  }

  manageCompetences(post: Poste): void {
    this.dialog.open(PostCompetencesComponent, { width: '480px', data: post })
      .afterClosed().subscribe(changed => { if (changed) this.load(); });
  }

  delete(post: Poste): void {
    this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Supprimer poste', message: 'Supprimer « ' + post.titre + ' » ?', confirmLabel: 'Supprimer' }
    }).afterClosed().subscribe(ok => {
      if (!ok) return;
      this.api.delete(post.id).subscribe({
        next: () => { this.snack.open('Poste supprimé', 'OK', { duration: 3000 }); this.load(); },
        error: ()  => this.snack.open('Erreur lors de la suppression', 'OK', { duration: 4000 }),
      });
    });
  }
}
