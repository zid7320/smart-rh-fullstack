import { Component, inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule }    from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule }     from '@angular/material/input';
import { MatSelectModule }    from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatSnackBar }        from '@angular/material/snack-bar';
import { CommonModule }       from '@angular/common';
import { FormationApiService } from '../../../core/api/training.service';
import { EmployeApiService }   from '../../../core/api/employee.service';
import { applyServerErrors }   from '../../../core/utils/form-error.util';
import type { Formation, Employe } from '../../../core/models';

export interface FormationFormData { formation?: Formation; }

@Component({
  selector: 'app-formation-form',
  imports: [
    CommonModule, MatDialogModule, MatButtonModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatProgressSpinnerModule, ReactiveFormsModule,
  ],
  template: `
    <h2 mat-dialog-title>{{ data.formation ? 'Modifier' : 'Nouvelle' }} formation</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="form-grid">
        <mat-form-field appearance="outline" class="full">
          <mat-label>Employé</mat-label>
          <mat-select formControlName="employeId">
            @for (e of employes; track e.id) {
              <mat-option [value]="e.id">{{ e.nom }} {{ e.prenom }}</mat-option>
            }
          </mat-select>
          @if (form.get('employeId')?.invalid && form.get('employeId')?.touched) {
            <mat-error>Employé requis.</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>Titre</mat-label>
          <input matInput formControlName="titre" placeholder="ex: Formation Angular 17">
          @if (form.get('titre')?.invalid && form.get('titre')?.touched) {
            <mat-error>Titre requis.</mat-error>
          }
          @if (form.get('titre')?.errors?.['server']) {
            <mat-error>{{ form.get('titre')!.errors!['server'] }}</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>Certification obtenue</mat-label>
          <input matInput formControlName="certification">
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>Organisme</mat-label>
          <input matInput formControlName="organisme">
        </mat-form-field>

        <mat-form-field appearance="outline" class="half">
          <mat-label>Date début</mat-label>
          <input matInput type="date" formControlName="dateDebut">
        </mat-form-field>

        <mat-form-field appearance="outline" class="half">
          <mat-label>Date fin</mat-label>
          <input matInput type="date" formControlName="dateFin">
        </mat-form-field>

        @if (serverError) { <p class="server-error">{{ serverError }}</p> }
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
    .form-grid { display: flex; flex-wrap: wrap; gap: 8px; min-width: 480px; padding-top: 8px; }
    .full { width: 100%; }
    .half { width: calc(50% - 4px); }
    .server-error { color: #f44336; font-size: 12px; width: 100%; margin: 4px 0 0; }
  `]
})
export class FormationFormComponent implements OnInit {
  data    = inject<FormationFormData>(MAT_DIALOG_DATA);
  ref     = inject(MatDialogRef<FormationFormComponent>);
  private api       = inject(FormationApiService);
  private empApi    = inject(EmployeApiService);
  private fb        = inject(FormBuilder);
  private snack     = inject(MatSnackBar);

  form = this.fb.group({
    employeId:    [null as number | null, Validators.required],
    titre:        ['', Validators.required],
    certification:[''],
    organisme:    [''],
    dateDebut:    [''],
    dateFin:      [''],
  });

  employes:   Employe[] = [];
  saving      = false;
  serverError = '';

  ngOnInit(): void {
    this.empApi.getAll({ size: 200 }).subscribe(p => { this.employes = p.content; });
    if (this.data.formation) {
      const f = this.data.formation;
      this.form.patchValue({
        employeId:    f.employeId    ?? null,
        titre:        f.titre        ?? '',
        certification:f.certification ?? '',
        organisme:    f.organisme    ?? '',
        dateDebut:    f.dateDebut    ?? '',
        dateFin:      f.dateFin      ?? '',
      });
    }
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.serverError = '';
    const op$ = this.data.formation
      ? this.api.update(this.data.formation.id, this.form.value as any)
      : this.api.create(this.form.value as any);
    op$.subscribe({
      next: saved => {
        this.saving = false;
        this.snack.open('Formation enregistrée.', 'OK', { duration: 3000 });
        this.ref.close(saved);
      },
      error: err => {
        this.saving = false;
        this.serverError = applyServerErrors(err, this.form);
      }
    });
  }
}
