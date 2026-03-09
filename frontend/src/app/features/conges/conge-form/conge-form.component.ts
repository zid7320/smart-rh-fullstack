import { Component, inject, OnInit } from '@angular/core';
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
import { CongeApiService }     from '../../../core/api/leave.service';
import { EmployeApiService }   from '../../../core/api/employee.service';
import type { Employe }        from '../../../core/models';

const TYPES = ['Congé payé', 'RTT', 'Maladie', 'Maternité/Paternité', 'Sans solde', 'Autre'];

@Component({
  selector: 'app-conge-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule,
    MatFormFieldModule, MatInputModule, MatSelectModule,
    MatButtonModule, MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>Demande de congé</h2>
    <mat-dialog-content class="form-content">
      <form [formGroup]="form" id="congeForm" (ngSubmit)="submit()" autocomplete="off">

        @if (isRhAdmin) {
          <mat-form-field appearance="outline" class="full">
            <mat-label>Employé</mat-label>
            <mat-select formControlName="employeId">
              @for (e of employees; track e.id) {
                <mat-option [value]="e.id">{{ e.nom }} {{ e.prenom }}</mat-option>
              }
            </mat-select>
            @if (form.get('employeId')?.touched && form.get('employeId')?.hasError('required')) {
              <mat-error>Sélectionnez un employé.</mat-error>
            }
          </mat-form-field>
        }

        <mat-form-field appearance="outline" class="full">
          <mat-label>Type de congé</mat-label>
          <mat-select formControlName="type">
            @for (t of types; track t) { <mat-option [value]="t">{{ t }}</mat-option> }
          </mat-select>
          @if (form.get('type')?.touched && form.get('type')?.hasError('required')) {
            <mat-error>Sélectionnez un type.</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>Date de début</mat-label>
          <input matInput type="date" formControlName="dateDebut">
          @if (form.get('dateDebut')?.touched && form.get('dateDebut')?.hasError('required')) {
            <mat-error>Date requise.</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>Date de fin</mat-label>
          <input matInput type="date" formControlName="dateFin">
          @if (form.get('dateFin')?.touched && form.get('dateFin')?.hasError('required')) {
            <mat-error>Date requise.</mat-error>
          }
        </mat-form-field>

        @if (serverError) { <p class="srv-err">{{ serverError }}</p> }
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Annuler</button>
      <button mat-raised-button color="primary" form="congeForm" type="submit" [disabled]="saving">
        @if (saving) { <mat-spinner diameter="18"></mat-spinner> }
        @else { Soumettre }
      </button>
    </mat-dialog-actions>
  `,
  styles: [`.full{width:100%;margin-bottom:4px}.form-content{min-width:400px}.srv-err{color:#c62828;font-size:.85rem}`],
})
export class CongeFormComponent implements OnInit {
  data       = inject<{ currentEmpId: number | null; isRhAdmin: boolean }>(MAT_DIALOG_DATA);
  ref        = inject(MatDialogRef<CongeFormComponent>);
  private api    = inject(CongeApiService);
  private empApi = inject(EmployeApiService);
  private fb     = inject(FormBuilder);
  private snack  = inject(MatSnackBar);

  isRhAdmin   = this.data.isRhAdmin;
  saving      = false;
  serverError = '';
  types       = TYPES;
  employees: Employe[] = [];

  form = this.fb.group({
    employeId: [this.data.currentEmpId, Validators.required],
    type:      ['', Validators.required],
    dateDebut: ['', Validators.required],
    dateFin:   ['', Validators.required],
  });

  ngOnInit(): void {
    if (this.isRhAdmin) {
      this.empApi.getAll({ size: 200 }).subscribe({ next: pg => this.employees = pg.content });
    }
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.value;
    if (v.dateDebut && v.dateFin && v.dateFin < v.dateDebut) {
      this.serverError = 'La date de fin doit être après la date de début.'; return;
    }
    this.saving = true;
    this.api.create(this.form.value as any).subscribe({
      next: () => {
        this.snack.open('Demande soumise', 'OK', { duration: 3000 });
        this.ref.close(true);
      },
      error: (e: HttpErrorResponse) => {
        this.saving = false;
        this.serverError = (e.error as { message?: string })?.message ?? 'Erreur serveur.';
      },
    });
  }
}
