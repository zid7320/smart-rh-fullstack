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
import { PosteApiService }     from '../../../core/api/post.service';
import type { Poste }          from '../../../core/models';

@Component({
  selector: 'app-post-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule,
    MatFormFieldModule, MatInputModule, MatButtonModule, MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Modifier poste' : 'Nouveau poste' }}</h2>
    <mat-dialog-content class="form-content">
      <form [formGroup]="form" id="postForm" (ngSubmit)="submit()" autocomplete="off">
        <mat-form-field appearance="outline" class="full">
          <mat-label>Titre</mat-label>
          <input matInput formControlName="titre">
          @if (form.get('titre')?.touched && form.get('titre')?.hasError('required')) {
            <mat-error>Le titre est requis.</mat-error>
          }
          @if (form.get('titre')?.hasError('maxlength')) {
            <mat-error>150 caractères maximum.</mat-error>
          }
        </mat-form-field>
        <mat-form-field appearance="outline" class="full">
          <mat-label>Description des compétences requises</mat-label>
          <textarea matInput formControlName="competencesRequises" rows="4"></textarea>
        </mat-form-field>
        @if (serverError) { <p class="srv-err">{{ serverError }}</p> }
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Annuler</button>
      <button mat-raised-button color="primary" form="postForm" type="submit" [disabled]="saving">
        @if (saving) { <mat-spinner diameter="18"></mat-spinner> }
        @else { {{ isEdit ? 'Enregistrer' : 'Créer' }} }
      </button>
    </mat-dialog-actions>
  `,
  styles: [`.full{width:100%;margin-bottom:4px}.form-content{min-width:400px}.srv-err{color:#c62828;font-size:.85rem}`],
})
export class PostFormComponent {
  data   = inject<Poste | null>(MAT_DIALOG_DATA);
  ref    = inject(MatDialogRef<PostFormComponent>);
  private api   = inject(PosteApiService);
  private fb    = inject(FormBuilder);
  private snack = inject(MatSnackBar);

  isEdit      = !!this.data;
  saving      = false;
  serverError = '';

  form = this.fb.group({
    titre:               [this.data?.titre               ?? '', [Validators.required, Validators.maxLength(150)]],
    competencesRequises: [this.data?.competencesRequises ?? ''],
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
        this.snack.open(this.isEdit ? 'Poste mis à jour' : 'Poste créé', 'OK', { duration: 3000 });
        this.ref.close(true);
      },
      error: (e: HttpErrorResponse) => {
        this.saving = false;
        this.serverError = (e.error as { message?: string })?.message ?? 'Erreur serveur.';
      },
    });
  }
}
