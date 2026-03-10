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
import { PlanningApiService } from '../../../core/api/planning.service';
import { EmployeApiService }  from '../../../core/api/employee.service';
import type { Planning, Employe } from '../../../core/models';

export interface PlanningFormData { planning?: Planning; }

const TYPES = ['Travail', 'Conge', 'Formation', 'Reunion', 'Teletravail', 'Autre'];

@Component({
  selector: 'app-planning-form',
  standalone: true,
  imports: [
    CommonModule, MatDialogModule, MatButtonModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatProgressSpinnerModule, ReactiveFormsModule,
  ],
  template: `
    <h2 mat-dialog-title>{{ data.planning ? 'Modifier' : 'Nouvelle entree' }} — Planning</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="planning-form">
        <mat-form-field appearance="outline" class="full">
          <mat-label>Employe</mat-label>
          <mat-select formControlName="employeId">
            @for (e of employes; track e.id) {
              <mat-option [value]="e.id">{{ e.nom }} {{ e.prenom }}</mat-option>
            }
          </mat-select>
          @if (form.get('employeId')?.invalid && form.get('employeId')?.touched) {
            <mat-error>Employe requis.</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>Type</mat-label>
          <mat-select formControlName="type">
            @for (t of types; track t) {
              <mat-option [value]="t">{{ t }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>Horaires</mat-label>
          <input matInput formControlName="horaires" placeholder="ex: 09:00-17:00">
        </mat-form-field>

        <mat-form-field appearance="outline" class="half">
          <mat-label>Date debut</mat-label>
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
    .planning-form { display: flex; flex-wrap: wrap; gap: 8px; min-width: 480px; padding-top: 8px; }
    .full { width: 100%; }
    .half { width: calc(50% - 4px); }
    .server-error { color: #f44336; font-size: 12px; width: 100%; margin: 4px 0 0; }
  `]
})
export class PlanningFormComponent implements OnInit {
  data    = inject<PlanningFormData>(MAT_DIALOG_DATA);
  ref     = inject(MatDialogRef<PlanningFormComponent>);
  private api    = inject(PlanningApiService);
  private empApi = inject(EmployeApiService);
  private fb     = inject(FormBuilder);
  private snack  = inject(MatSnackBar);

  types    = TYPES;
  employes: Employe[] = [];
  saving      = false;
  serverError = '';

  form = this.fb.group({
    employeId: [null as number | null, Validators.required],
    type:      [''],
    horaires:  [''],
    dateDebut: [''],
    dateFin:   [''],
  });

  ngOnInit(): void {
    this.empApi.getAll({ size: 200 }).subscribe(p => { this.employes = p.content; });
    if (this.data.planning) {
      const p = this.data.planning;
      this.form.patchValue({
        employeId: p.employeId ?? null,
        type:      p.type      ?? '',
        horaires:  p.horaires  ?? '',
        dateDebut: p.dateDebut ?? '',
        dateFin:   p.dateFin   ?? '',
      });
    }
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.serverError = '';
    const body = this.form.value;
    const op$ = this.data.planning
      ? this.api.update(this.data.planning.id, body as any)
      : this.api.create(body as any);
    op$.subscribe({
      next: saved => {
        this.saving = false;
        this.snack.open('Planning enregistre.', 'OK', { duration: 3000 });
        this.ref.close(saved);
      },
      error: (err: any) => {
        this.saving = false;
        this.serverError = err?.error?.message ?? 'Une erreur est survenue.';
      }
    });
  }
}
