import { Component, inject } from '@angular/core';
import { CommonModule }        from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule }  from '@angular/material/form-field';
import { MatInputModule }      from '@angular/material/input';
import { MatButtonModule }     from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar }         from '@angular/material/snack-bar';
import { HttpErrorResponse }   from '@angular/common/http';
import { CompetenceApiService } from '../../../core/api/competence.service';
import type { Competence }     from '../../../core/models';

@Component({
  selector: 'app-competence-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule,
    MatFormFieldModule, MatInputModule, MatButtonModule, MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Modifier compétence' : 'Nouvelle compétence' }}</h2>
    <mat-dialog-content class="form-content">
      <form [formGroup]="form" id="compForm" (ngSubmit)="submit()" autocomplete="off">
        <mat-form-field appearance="outline" class="full">
          <mat-label>Nom</mat-label>
          <input matInput formControlName="nom">
          @if (form.get('nom')?.touched && form.get('nom')?.hasError('required')) {
            <mat-error>Le nom est requis.</mat-error>
          }
          @if (form.get('nom')?.hasError('maxlength')) {
            <mat-error>100 caractères maximum.</mat-error>
          }
        </mat-form-field>
        <mat-form-field appearance="outline" class="full">
          <mat-label>Niveau</mat-label>
          <input matInput formControlName="niveau" placeholder="ex : Débutant / Intermédiaire / Expert">
          @if (form.get('niveau')?.hasError('maxlength')) {
            <mat-error>50 caractères maximum.</mat-error>
          }
        </mat-form-field>
        @if (serverError) { <p class="srv-err">{{ serverError }}</p> }
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Annuler</button>
      <button mat-raised-button color="primary" form="compForm" type="submit" [disabled]="saving">
        @if (saving) { <mat-spinner diameter="18"></mat-spinner> }
        @else { {{ isEdit ? 'Enregistrer' : 'Créer' }} }
      </button>
    </mat-dialog-actions>
  `,
  styles: [`.full{width:100%;margin-bottom:4px}.form-content{min-width:360px}.srv-err{color:#c62828;font-size:.85rem}`],
})
export class CompetenceFormComponent {
  data   = inject<Competence | null>(MAT_DIALOG_DATA);
  ref    = inject(MatDialogRef<CompetenceFormComponent>);
  private api   = inject(CompetenceApiService);
  private fb    = inject(FormBuilder);
  private snack = inject(MatSnackBar);

  isEdit      = !!this.data;
  saving      = false;
  serverError = '';

  form = this.fb.group({
    nom:    [this.data?.nom    ?? '', [Validators.required, Validators.maxLength(100)]],
    niveau: [this.data?.niveau ?? '',  Validators.maxLength(50)],
  });

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    const body = this.form.value;
    const op$  = this.isEdit
      ? this.api.update(this.data!.id, body as any)
      : this.api.create(body as any);
    op$.subscribe({
      next: () => {
        this.snack.open(this.isEdit ? 'Compétence mise à jour' : 'Compétence créée', 'OK', { duration: 3000 });
        this.ref.close(true);
      },
      error: (e: HttpErrorResponse) => {
        this.saving = false;
        this.serverError = (e.error as { message?: string })?.message ?? 'Erreur serveur.';
      },
    });
  }
}
