import { Component, inject } from '@angular/core';
import { CommonModule }        from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule }  from '@angular/material/form-field';
import { MatInputModule }      from '@angular/material/input';
import { MatSelectModule }     from '@angular/material/select';
import { MatButtonModule }     from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar }         from '@angular/material/snack-bar';
import { HttpErrorResponse }   from '@angular/common/http';
import { RecrutementApiService } from '../../../core/api/recruitment.service';
import type { Recrutement }    from '../../../core/models';

const STATUTS = ['OUVERT', 'EN_COURS', 'CLOS', 'EMBAUCHE'];

@Component({
  selector: 'app-recrutement-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule,
    MatFormFieldModule, MatInputModule, MatSelectModule,
    MatButtonModule, MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Modifier recrutement' : 'Nouveau recrutement' }}</h2>
    <mat-dialog-content class="form-content">
      <form [formGroup]="form" id="recrForm" (ngSubmit)="submit()" autocomplete="off">
        <mat-form-field appearance="outline" class="full">
          <mat-label>Poste cible</mat-label>
          <input matInput formControlName="posteCible" placeholder="ex : Développeur Java Senior">
          @if (form.get('posteCible')?.touched && form.get('posteCible')?.hasError('required')) {
            <mat-error>Le poste cible est requis.</mat-error>
          }
        </mat-form-field>
        <mat-form-field appearance="outline" class="full">
          <mat-label>Statut</mat-label>
          <mat-select formControlName="statut">
            @for (s of statuts; track s) { <mat-option [value]="s">{{ s }}</mat-option> }
          </mat-select>
        </mat-form-field>
        @if (serverError) { <p class="srv-err">{{ serverError }}</p> }
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Annuler</button>
      <button mat-raised-button color="primary" form="recrForm" type="submit" [disabled]="saving">
        @if (saving) { <mat-spinner diameter="18"></mat-spinner> }
        @else { {{ isEdit ? 'Enregistrer' : 'Créer' }} }
      </button>
    </mat-dialog-actions>
  `,
  styles: [`.full{width:100%;margin-bottom:4px}.form-content{min-width:380px}.srv-err{color:#c62828;font-size:.85rem}`],
})
export class RecrutementFormComponent {
  data   = inject<Recrutement | null>(MAT_DIALOG_DATA);
  ref    = inject(MatDialogRef<RecrutementFormComponent>);
  private api   = inject(RecrutementApiService);
  private fb    = inject(FormBuilder);
  private snack = inject(MatSnackBar);

  isEdit      = !!this.data;
  saving      = false;
  serverError = '';
  statuts     = STATUTS;

  form = this.fb.group({
    posteCible: [this.data?.posteCible ?? '', [Validators.required, Validators.maxLength(150)]],
    statut:     [this.data?.statut     ?? 'OUVERT'],
  });

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    const op$ = this.isEdit
      ? this.api.update(this.data!.id, this.form.value as any)
      : this.api.create(this.form.value as any);
    op$.subscribe({
      next: () => {
        this.snack.open(this.isEdit ? 'Mis à jour' : 'Recrutement créé', 'OK', { duration: 3000 });
        this.ref.close(true);
      },
      error: (e: HttpErrorResponse) => {
        this.saving = false;
        this.serverError = (e.error as { message?: string })?.message ?? 'Erreur serveur.';
      },
    });
  }
}
