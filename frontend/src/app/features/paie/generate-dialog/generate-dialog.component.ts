import { Component, inject } from '@angular/core';
import { CommonModule }        from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule }  from '@angular/material/form-field';
import { MatInputModule }      from '@angular/material/input';
import { MatSelectModule }     from '@angular/material/select';
import { MatButtonModule }     from '@angular/material/button';
import { MatIconModule }       from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar }         from '@angular/material/snack-bar';
import { HttpErrorResponse }   from '@angular/common/http';
import { PaieApiService }      from '../../../core/api/payroll.service';

const MOIS_OPTIONS = [
  { value: 1,  label: 'Janvier'   }, { value: 2,  label: 'Février'   },
  { value: 3,  label: 'Mars'      }, { value: 4,  label: 'Avril'     },
  { value: 5,  label: 'Mai'       }, { value: 6,  label: 'Juin'      },
  { value: 7,  label: 'Juillet'   }, { value: 8,  label: 'Août'      },
  { value: 9,  label: 'Septembre' }, { value: 10, label: 'Octobre'   },
  { value: 11, label: 'Novembre'  }, { value: 12, label: 'Décembre'  },
];

@Component({
  selector: 'app-generate-dialog',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule,
    MatFormFieldModule, MatInputModule, MatSelectModule,
    MatButtonModule, MatIconModule, MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>Générer la paie du mois</h2>
    <mat-dialog-content class="form-content">
      <p class="hint">La paie sera générée pour tous les employés actifs.</p>
      <form [formGroup]="form" id="genForm" (ngSubmit)="submit()" autocomplete="off">
        <mat-form-field appearance="outline" class="full">
          <mat-label>Mois</mat-label>
          <mat-select formControlName="mois">
            @for (m of moisOptions; track m.value) {
              <mat-option [value]="m.value">{{ m.label }}</mat-option>
            }
          </mat-select>
          @if (form.get('mois')?.touched && form.get('mois')?.hasError('required')) {
            <mat-error>Sélectionnez un mois.</mat-error>
          }
        </mat-form-field>
        <mat-form-field appearance="outline" class="full">
          <mat-label>Année</mat-label>
          <input matInput type="number" formControlName="annee" placeholder="2025">
          @if (form.get('annee')?.touched && form.get('annee')?.hasError('required')) {
            <mat-error>Année requise.</mat-error>
          }
          @if (form.get('annee')?.hasError('min')) {
            <mat-error>Année invalide (min 2000).</mat-error>
          }
        </mat-form-field>
        @if (serverError) { <p class="srv-err">{{ serverError }}</p> }
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Annuler</button>
      <button mat-raised-button color="primary" form="genForm" type="submit" [disabled]="generating">
        @if (generating) { <mat-spinner diameter="18"></mat-spinner> }
        @else { <ng-container><mat-icon>payments</mat-icon> Générer</ng-container> }
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .form-content { min-width:340px; }
    .full         { width:100%; margin-bottom:4px; }
    .hint         { color:rgba(0,0,0,.6); font-size:.9rem; margin-bottom:12px; }
    .srv-err      { color:#c62828; font-size:.85rem; }
  `],
})
export class GenerateDialogComponent {
  ref            = inject(MatDialogRef<GenerateDialogComponent>);
  private api    = inject(PaieApiService);
  private fb     = inject(FormBuilder);
  private snack  = inject(MatSnackBar);

  generating  = false;
  serverError = '';
  moisOptions = MOIS_OPTIONS;

  form = this.fb.group({
    mois:  [new Date().getMonth() + 1, [Validators.required, Validators.min(1), Validators.max(12)]],
    annee: [new Date().getFullYear(),  [Validators.required, Validators.min(2000)]],
  });

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.generating = true;
    const { mois, annee } = this.form.value;
    this.api.generate({ mois: mois!, annee: annee! }).subscribe({
      next: records => { this.ref.close(records.length); },
      error: (e: HttpErrorResponse) => {
        this.generating = false;
        this.serverError = (e.error as { message?: string })?.message ?? 'Erreur lors de la génération.';
      },
    });
  }
}
