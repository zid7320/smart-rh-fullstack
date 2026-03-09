import { Component, inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule }  from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule }   from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatSnackBar }      from '@angular/material/snack-bar';
import { DossierRhApiService } from '../../../core/api/dossier.service';
import { applyServerErrors }   from '../../../core/utils/form-error.util';
import type { DossierRH }      from '../../../core/models';

@Component({
  selector: 'app-dossier-edit-dialog',
  imports: [
    MatDialogModule, MatButtonModule, MatFormFieldModule,
    MatInputModule, MatProgressSpinnerModule, ReactiveFormsModule,
  ],
  template: `
    <h2 mat-dialog-title>
      Modifier le dossier — {{ data.employeNomComplet ?? ('Employé #' + data.employeId) }}
    </h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="dossier-form">
        <mat-form-field appearance="outline" class="full">
          <mat-label>Informations personnelles</mat-label>
          <textarea matInput formControlName="infosPerso" rows="4"
                    placeholder="Coordonnées, situation familiale…"></textarea>
          @if (form.get('infosPerso')?.errors?.['server']) {
            <mat-error>{{ form.get('infosPerso')!.errors!['server'] }}</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>Diplômes & certifications</mat-label>
          <textarea matInput formControlName="diplomes" rows="4"
                    placeholder="Liste des diplômes obtenus…"></textarea>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>Documents RH</mat-label>
          <textarea matInput formControlName="documents" rows="4"
                    placeholder="Références aux documents stockés…"></textarea>
        </mat-form-field>

        @if (serverError) {
          <p class="server-error">{{ serverError }}</p>
        }
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Annuler</button>
      <button mat-raised-button color="primary" (click)="save()" [disabled]="saving">
        @if (saving) { <mat-spinner diameter="18"></mat-spinner> } @else { Enregistrer }
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .dossier-form { display: flex; flex-direction: column; gap: 8px; min-width: 480px; padding-top: 8px; }
    .full { width: 100%; }
    .server-error { color: #f44336; font-size: 12px; margin: 4px 0 0; }
  `]
})
export class DossierEditDialogComponent implements OnInit {
  data    = inject<DossierRH>(MAT_DIALOG_DATA);
  ref     = inject(MatDialogRef<DossierEditDialogComponent>);
  private api   = inject(DossierRhApiService);
  private fb    = inject(FormBuilder);
  private snack = inject(MatSnackBar);

  form = this.fb.group({
    infosPerso: ['', Validators.required],
    diplomes:   [''],
    documents:  [''],
  });

  saving      = false;
  serverError = '';

  ngOnInit(): void {
    this.form.patchValue({
      infosPerso: this.data.infosPerso ?? '',
      diplomes:   this.data.diplomes   ?? '',
      documents:  this.data.documents  ?? '',
    });
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.serverError = '';
    this.api.update(this.data.id, this.form.value as any).subscribe({
      next: updated => {
        this.saving = false;
        this.snack.open('Dossier mis à jour.', 'OK', { duration: 3000 });
        this.ref.close(updated);
      },
      error: err => {
        this.saving = false;
        this.serverError = applyServerErrors(err, this.form);
      }
    });
  }
}
