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
import { ContratApiService }  from '../../../core/api/contract.service';
import { EmployeApiService }  from '../../../core/api/employee.service';
import { applyServerErrors }  from '../../../core/utils/form-error.util';
import type { Contrat, Employe } from '../../../core/models';

export interface ContractFormData { contrat?: Contrat; }

const CONTRACT_TYPES = ['CDI', 'CDD', 'STAGE', 'ALTERNANCE'];

@Component({
  selector: 'app-contract-form',
  imports: [
    CommonModule, MatDialogModule, MatButtonModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatProgressSpinnerModule, ReactiveFormsModule,
  ],
  template: `
    <h2 mat-dialog-title>{{ data.contrat ? 'Modifier le contrat' : 'Nouveau contrat' }}</h2>
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

        <mat-form-field appearance="outline" class="half">
          <mat-label>Type de contrat</mat-label>
          <mat-select formControlName="type">
            @for (t of types; track t) {
              <mat-option [value]="t">{{ t }}</mat-option>
            }
          </mat-select>
          @if (form.get('type')?.invalid && form.get('type')?.touched) {
            <mat-error>Type requis.</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="half">
          <mat-label>Salaire (MAD)</mat-label>
          <input matInput type="number" formControlName="salaire" min="0">
          @if (form.get('salaire')?.invalid && form.get('salaire')?.touched) {
            <mat-error>Salaire requis.</mat-error>
          }
          @if (form.get('salaire')?.errors?.['server']) {
            <mat-error>{{ form.get('salaire')!.errors!['server'] }}</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="half">
          <mat-label>Date début</mat-label>
          <input matInput type="date" formControlName="dateDebut">
          @if (form.get('dateDebut')?.invalid && form.get('dateDebut')?.touched) {
            <mat-error>Date début requise.</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="half">
          <mat-label>Date fin (optionnel)</mat-label>
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
    .full  { width: 100%; }
    .half  { width: calc(50% - 4px); }
    .server-error { color: #f44336; font-size: 12px; width: 100%; margin: 4px 0 0; }
  `]
})
export class ContractFormComponent implements OnInit {
  data    = inject<ContractFormData>(MAT_DIALOG_DATA);
  ref     = inject(MatDialogRef<ContractFormComponent>);
  private api    = inject(ContratApiService);
  private empApi = inject(EmployeApiService);
  private fb     = inject(FormBuilder);
  private snack  = inject(MatSnackBar);

  readonly types = CONTRACT_TYPES;

  form = this.fb.group({
    employeId: [null as number | null, Validators.required],
    type:      ['', Validators.required],
    salaire:   [null as number | null, [Validators.required, Validators.min(0)]],
    dateDebut: ['', Validators.required],
    dateFin:   [''],
  });

  employes:   Employe[] = [];
  saving      = false;
  serverError = '';

  ngOnInit(): void {
    this.empApi.getAll({ size: 200 }).subscribe(p => { this.employes = p.content; });
    if (this.data.contrat) {
      const c = this.data.contrat;
      this.form.patchValue({
        employeId: c.employeId ?? null,
        type:      c.type      ?? '',
        salaire:   c.salaire   ?? null,
        dateDebut: c.dateDebut ?? '',
        dateFin:   c.dateFin   ?? '',
      });
    }
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.serverError = '';
    const op$ = this.data.contrat
      ? this.api.update(this.data.contrat.id, this.form.value as any)
      : this.api.create(this.form.value as any);
    op$.subscribe({
      next: saved => {
        this.saving = false;
        this.snack.open('Contrat enregistré.', 'OK', { duration: 3000 });
        this.ref.close(saved);
      },
      error: err => {
        this.saving = false;
        this.serverError = applyServerErrors(err, this.form);
      }
    });
  }
}
