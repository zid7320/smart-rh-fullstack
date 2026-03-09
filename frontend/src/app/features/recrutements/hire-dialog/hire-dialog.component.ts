import { Component, inject, OnInit } from '@angular/core';
import { CommonModule }        from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule }  from '@angular/material/form-field';
import { MatInputModule }      from '@angular/material/input';
import { MatSelectModule }     from '@angular/material/select';
import { MatButtonModule }     from '@angular/material/button';
import { MatTableModule }      from '@angular/material/table';
import { MatIconModule }       from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule }    from '@angular/material/divider';
import { MatSnackBar }         from '@angular/material/snack-bar';
import { HttpErrorResponse }   from '@angular/common/http';
import { RecrutementApiService } from '../../../core/api/recruitment.service';
import { PosteApiService }     from '../../../core/api/post.service';
import type { Recrutement, Candidate, Poste } from '../../../core/models';

@Component({
  selector: 'app-hire-dialog',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule,
    MatFormFieldModule, MatInputModule, MatSelectModule,
    MatButtonModule, MatTableModule, MatIconModule, MatProgressSpinnerModule, MatDividerModule,
  ],
  template: `
    <h2 mat-dialog-title>Embaucher — {{ recr.posteCible }}</h2>
    <mat-dialog-content class="hire-content">

      <!-- Candidates section -->
      <h3 class="section-title">Candidats</h3>
      @if (loadingCandidates) {
        <div class="center"><mat-spinner diameter="28"></mat-spinner></div>
      } @else if (candidates.length === 0) {
        <p class="empty-msg">Aucun candidat enregistré pour ce recrutement.</p>
      } @else {
        <table mat-table [dataSource]="candidates" class="candidates-table">
          <ng-container matColumnDef="nom">
            <th mat-header-cell *matHeaderCellDef>Nom</th>
            <td mat-cell *matCellDef="let c">{{ c.nom }} {{ c.prenom }}</td>
          </ng-container>
          <ng-container matColumnDef="email">
            <th mat-header-cell *matHeaderCellDef>E-mail</th>
            <td mat-cell *matCellDef="let c">{{ c.email }}</td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="['nom','email']"></tr>
          <tr mat-row *matRowDef="let row; columns: ['nom','email'];"></tr>
        </table>
      }

      <!-- Add candidate -->
      <div class="add-candidate-section">
        @if (!showAddForm) {
          <button mat-stroked-button (click)="showAddForm = true">
            <mat-icon>person_add</mat-icon> Ajouter un candidat
          </button>
        } @else {
          <form [formGroup]="candidateForm" (ngSubmit)="addCandidate()" class="inline-form">
            <mat-form-field appearance="outline" class="f-sm">
              <mat-label>Nom</mat-label><input matInput formControlName="nom">
            </mat-form-field>
            <mat-form-field appearance="outline" class="f-sm">
              <mat-label>Prénom</mat-label><input matInput formControlName="prenom">
            </mat-form-field>
            <mat-form-field appearance="outline" class="f-md">
              <mat-label>E-mail</mat-label><input matInput formControlName="email" type="email">
            </mat-form-field>
            <button mat-icon-button color="primary" type="submit" [disabled]="candidateForm.invalid" matTooltip="Ajouter">
              <mat-icon>check</mat-icon>
            </button>
            <button mat-icon-button type="button" (click)="showAddForm = false"><mat-icon>close</mat-icon></button>
          </form>
        }
      </div>

      <mat-divider class="divider"></mat-divider>

      <!-- Hire form -->
      <h3 class="section-title">Formulaire d'embauche</h3>
      <form [formGroup]="hireForm" id="hireForm" (ngSubmit)="hire()" autocomplete="off">
        <mat-form-field appearance="outline" class="full">
          <mat-label>Candidat à embaucher</mat-label>
          <mat-select formControlName="candidateId">
            @for (c of candidates; track c.id) {
              <mat-option [value]="c.id">{{ c.nom }} {{ c.prenom }} &lt;{{ c.email }}&gt;</mat-option>
            }
          </mat-select>
          @if (hireForm.get('candidateId')?.touched && hireForm.get('candidateId')?.hasError('required')) {
            <mat-error>Sélectionnez un candidat.</mat-error>
          }
        </mat-form-field>
        <mat-form-field appearance="outline" class="full">
          <mat-label>Lier à un poste (optionnel)</mat-label>
          <mat-select formControlName="posteId">
            <mat-option [value]="null">— Aucun —</mat-option>
            @for (p of posts; track p.id) { <mat-option [value]="p.id">{{ p.titre }}</mat-option> }
          </mat-select>
        </mat-form-field>
        @if (serverError) { <p class="srv-err">{{ serverError }}</p> }
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Annuler</button>
      <button mat-raised-button color="primary" form="hireForm" type="submit"
              [disabled]="hiring || candidates.length === 0">
        @if (hiring) { <mat-spinner diameter="18"></mat-spinner> }
        @else { <ng-container><mat-icon>how_to_reg</mat-icon> Embaucher</ng-container> }
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .hire-content   { min-width:540px; max-height:70vh; overflow-y:auto; }
    .section-title  { font-size:1rem; font-weight:500; margin:12px 0 8px; }
    .candidates-table { width:100%; margin-bottom:8px; }
    .add-candidate-section { margin:8px 0; }
    .inline-form    { display:flex; gap:8px; align-items:flex-start; flex-wrap:wrap; }
    .f-sm           { width:130px; }
    .f-md           { width:200px; }
    .full           { width:100%; margin-bottom:4px; }
    .center         { display:flex; justify-content:center; padding:16px; }
    .empty-msg      { color:rgba(0,0,0,.45); font-style:italic; padding:8px 0; }
    .divider        { margin:16px 0; }
    .srv-err        { color:#c62828; font-size:.85rem; }
  `],
})
export class HireDialogComponent implements OnInit {
  recr           = inject<Recrutement>(MAT_DIALOG_DATA);
  ref            = inject(MatDialogRef<HireDialogComponent>);
  private api    = inject(RecrutementApiService);
  private postApi = inject(PosteApiService);
  private fb     = inject(FormBuilder);
  private snack  = inject(MatSnackBar);

  loadingCandidates = true;
  candidates: Candidate[] = [];
  posts:      Poste[]     = [];
  showAddForm = false;
  hiring      = false;
  serverError = '';

  hireForm = this.fb.group({
    candidateId: [null as number | null, Validators.required],
    posteId:     [null as number | null],
  });

  candidateForm = this.fb.group({
    nom:    ['', Validators.required],
    prenom: ['', Validators.required],
    email:  ['', [Validators.required, Validators.email]],
  });

  ngOnInit(): void {
    this.loadCandidates();
    this.postApi.getAll({ page: 0, size: 100 }).subscribe({ next: pg => this.posts = pg.content });
  }

  loadCandidates(): void {
    this.loadingCandidates = true;
    this.api.getCandidates(this.recr.id, { size: 100 }).subscribe({
      next: pg => { this.candidates = pg.content; this.loadingCandidates = false; },
      error: ()  => { this.loadingCandidates = false; },
    });
  }

  addCandidate(): void {
    if (this.candidateForm.invalid) return;
    const body = this.candidateForm.value as { nom: string; prenom: string; email: string };
    this.api.addCandidate(this.recr.id, body).subscribe({
      next: c => {
        this.candidates = [...this.candidates, c];
        this.candidateForm.reset();
        this.showAddForm = false;
        this.snack.open('Candidat ajouté', 'OK', { duration: 2500 });
      },
      error: (e: HttpErrorResponse) =>
        this.snack.open((e.error as { message?: string })?.message ?? 'Erreur', 'OK', { duration: 3000 }),
    });
  }

  hire(): void {
    if (this.hireForm.invalid) { this.hireForm.markAllAsTouched(); return; }
    this.hiring = true;
    this.serverError = '';
    const { candidateId, posteId } = this.hireForm.value;
    this.api.hire(this.recr.id, { candidateId: candidateId!, posteId }).subscribe({
      next: emp  => { this.ref.close(emp); },
      error: (e: HttpErrorResponse) => {
        this.hiring = false;
        this.serverError = (e.error as { message?: string })?.message ?? "Erreur lors de l'embauche.";
      },
    });
  }
}
