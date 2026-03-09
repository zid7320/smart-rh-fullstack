import { Component, inject, OnInit } from '@angular/core';
import { CommonModule }        from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatListModule }       from '@angular/material/list';
import { MatButtonModule }     from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar }         from '@angular/material/snack-bar';
import { MatDividerModule }    from '@angular/material/divider';
import { PosteApiService }     from '../../../core/api/post.service';
import { CompetenceApiService } from '../../../core/api/competence.service';
import type { Poste, Competence } from '../../../core/models';

@Component({
  selector: 'app-post-competences',
  standalone: true,
  imports: [
    CommonModule, MatDialogModule, MatListModule,
    MatButtonModule, MatProgressSpinnerModule, MatDividerModule,
  ],
  template: `
    <h2 mat-dialog-title>Compétences &mdash; {{ post.titre }}</h2>
    <mat-dialog-content>
      @if (loading) {
        <div class="center"><mat-spinner diameter="36"></mat-spinner></div>
      } @else if (allCompetences.length === 0) {
        <p class="empty">Aucune compétence créée. Ajoutez-en d'abord dans le module Compétences.</p>
      } @else {
        <mat-selection-list>
          @for (c of allCompetences; track c.id) {
            <mat-list-option
              [value]="c.id"
              [selected]="linkedIds.has(c.id)"
              (selectedChange)="toggle(c, $event)">
              {{ c.nom }}
              @if (c.niveau) { <em class="niveau">&nbsp;· {{ c.niveau }}</em> }
            </mat-list-option>
          }
        </mat-selection-list>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-raised-button color="primary" [mat-dialog-close]="changed">Fermer</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .center { display:flex; justify-content:center; padding:32px; }
    .empty  { padding:16px; color:rgba(0,0,0,.45); }
    .niveau { color:rgba(0,0,0,.5); font-style:italic; }
  `],
})
export class PostCompetencesComponent implements OnInit {
  post           = inject<Poste>(MAT_DIALOG_DATA);
  ref            = inject(MatDialogRef<PostCompetencesComponent>);
  private postApi = inject(PosteApiService);
  private compApi = inject(CompetenceApiService);
  private snack   = inject(MatSnackBar);

  loading        = true;
  allCompetences: Competence[] = [];
  linkedIds      = new Set<number>();
  changed        = false;

  ngOnInit(): void {
    this.linkedIds = new Set(this.post.competenceIds ?? []);
    this.compApi.getAll({ page: 0, size: 200 }).subscribe({
      next: pg => { this.allCompetences = pg.content; this.loading = false; },
      error: ()  => { this.loading = false; },
    });
  }

  toggle(comp: Competence, selected: boolean): void {
    const op$ = selected
      ? this.postApi.attachCompetence(this.post.id, comp.id)
      : this.postApi.detachCompetence(this.post.id, comp.id);

    op$.subscribe({
      next: updated => {
        this.linkedIds = new Set(updated.competenceIds ?? []);
        this.changed = true;
      },
      error: () => {
        // Revert visual state by creating new Set without the failed change
        const reverted = new Set(this.linkedIds);
        if (selected) reverted.delete(comp.id); else reverted.add(comp.id);
        this.linkedIds = reverted;
        this.snack.open('Erreur lors de la mise à jour', 'OK', { duration: 3000 });
      },
    });
  }
}
