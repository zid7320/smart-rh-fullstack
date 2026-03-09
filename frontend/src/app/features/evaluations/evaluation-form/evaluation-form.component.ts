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
import { EvaluationApiService } from '../../../core/api/evaluation.service';
import { EmployeApiService }    from '../../../core/api/employee.service';
import { applyServerErrors }    from '../../../core/utils/form-error.util';
import type { Evaluation, Employe } from '../../../core/models';

export interface EvalFormData { evaluation?: Evaluation; }

@Component({
  selector: 'app-evaluation-form',
  imports: [
    CommonModule, MatDialogModule, MatButtonModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatProgressSpinnerModule, ReactiveFormsModule,
  ],
  template: `
    <h2 mat-dialog-title>{{ data.evaluation ? 'Modifier' : 'Nouvelle' }} évaluation</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="eval-form">
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
          <mat-label>Objectifs</mat-label>
          <textarea matInput formControlName="objectifs" rows="3"></textarea>
          @if (form.get('objectifs')?.invalid && form.get('objectifs')?.touched) {
            <mat-error>Objectifs requis.</mat-error>
          }
          @if (form.get('objectifs')?.errors?.['server']) {
            <mat-error>{{ form.get('objectifs')!.errors!['server'] }}</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>KPI</mat-label>
          <textarea matInput formControlName="kpi" rows="3"></textarea>
          @if (form.get('kpi')?.invalid && form.get('kpi')?.touched) {
            <mat-error>KPI requis.</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="half">
          <mat-label>Score (0–100)</mat-label>
          <input matInput type="number" formControlName="score" min="0" max="100">
        </mat-form-field>

        <mat-form-field appearance="outline" class="half">
          <mat-label>Date d'évaluation</mat-label>
          <input matInput type="date" formControlName="dateEvaluation">
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
    .eval-form { display: flex; flex-wrap: wrap; gap: 8px; min-width: 480px; padding-top: 8px; }
    .full { width: 100%; }
    .half { width: calc(50% - 4px); }
    .server-error { color: #f44336; font-size: 12px; width: 100%; margin: 4px 0 0; }
  `]
})
export class EvaluationFormComponent implements OnInit {
  data    = inject<EvalFormData>(MAT_DIALOG_DATA);
  ref     = inject(MatDialogRef<EvaluationFormComponent>);
  private api       = inject(EvaluationApiService);
  private empApi    = inject(EmployeApiService);
  private fb        = inject(FormBuilder);
  private snack     = inject(MatSnackBar);

  form = this.fb.group({
    employeId:      [null as number | null, Validators.required],
    objectifs:      ['', Validators.required],
    kpi:            ['', Validators.required],
    score:          [null as number | null],
    dateEvaluation: [''],
  });

  employes:    Employe[] = [];
  saving       = false;
  serverError  = '';

  ngOnInit(): void {
    this.empApi.getAll({ size: 200 }).subscribe(p => { this.employes = p.content; });
    if (this.data.evaluation) {
      const e = this.data.evaluation;
      this.form.patchValue({
        employeId:      e.employeId      ?? null,
        objectifs:      e.objectifs      ?? '',
        kpi:            e.kpi            ?? '',
        score:          e.score          ?? null,
        dateEvaluation: e.dateEvaluation ?? '',
      });
    }
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.serverError = '';
    const body = this.form.value;
    const op$ = this.data.evaluation
      ? this.api.update(this.data.evaluation.id, body as any)
      : this.api.create(body as any);
    op$.subscribe({
      next: saved => {
        this.saving = false;
        this.snack.open('Évaluation enregistrée.', 'OK', { duration: 3000 });
        this.ref.close(saved);
      },
      error: err => {
        this.saving = false;
        this.serverError = applyServerErrors(err, this.form);
      }
    });
  }
}
