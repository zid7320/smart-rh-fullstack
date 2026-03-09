import { Component, inject, OnInit } from '@angular/core';
import { CommonModule }        from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule }  from '@angular/material/form-field';
import { MatInputModule }      from '@angular/material/input';
import { MatSelectModule }     from '@angular/material/select';
import { MatButtonModule }     from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar }         from '@angular/material/snack-bar';
import { HttpErrorResponse }   from '@angular/common/http';
import { AttendanceApiService } from '../../../core/api/attendance.service';
import { EmployeApiService }    from '../../../core/api/employee.service';
import type { Employe }         from '../../../core/models';

@Component({
  selector: 'app-simulate-dialog',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule,
    MatFormFieldModule, MatInputModule, MatSelectModule,
    MatButtonModule, MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>Simuler un événement de présence</h2>
    <mat-dialog-content class="form-content">
      <p class="hint">
        Déclenche un enregistrement via <code>POST /api/attendance/recognition</code>.<br>
        Le backend publie l'événement sur le topic WebSocket — la table se met à jour en temps réel.
      </p>
      <form [formGroup]="form" id="simForm" (ngSubmit)="submit()" autocomplete="off">
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

        <mat-form-field appearance="outline" class="full">
          <mat-label>Type d'événement</mat-label>
          <mat-select formControlName="type">
            <mat-option value="IN">Entrée (IN)</mat-option>
            <mat-option value="OUT">Sortie (OUT)</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>Confiance IA (0.00 – 1.00)</mat-label>
          <input matInput type="number" formControlName="confidence"
                 min="0" max="1" step="0.01" placeholder="0.99">
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>ID Site (optionnel)</mat-label>
          <input matInput formControlName="siteId" placeholder="ex : SITE-A">
        </mat-form-field>

        @if (serverError) { <p class="srv-err">{{ serverError }}</p> }
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Annuler</button>
      <button mat-raised-button color="accent" form="simForm" type="submit" [disabled]="submitting">
        @if (submitting) { <mat-spinner diameter="18"></mat-spinner> }
        @else { Déclencher }
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .form-content { min-width: 380px; }
    .full  { width: 100%; margin-bottom: 4px; }
    .hint  { font-size: .85rem; color: rgba(0,0,0,.55); margin-bottom: 12px; line-height: 1.5; }
    code   { background: #f5f5f5; padding: 1px 4px; border-radius: 3px; font-size: .82rem; }
    .srv-err { color: #c62828; font-size: .85rem; }
  `],
})
export class SimulateDialogComponent implements OnInit {
  ref            = inject(MatDialogRef<SimulateDialogComponent>);
  private api    = inject(AttendanceApiService);
  private empApi = inject(EmployeApiService);
  private fb     = inject(FormBuilder);
  private snack  = inject(MatSnackBar);

  submitting  = false;
  serverError = '';
  employees: Employe[] = [];

  form = this.fb.group({
    employeId:  [null as number | null, Validators.required],
    type:       ['IN' as 'IN' | 'OUT',  Validators.required],
    confidence: [0.99, [Validators.min(0), Validators.max(1)]],
    siteId:     [''],
  });

  ngOnInit(): void {
    this.empApi.getAll({ size: 200 }).subscribe({ next: pg => this.employees = pg.content });
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.submitting = true;
    this.serverError = '';
    const v = this.form.value;
    this.api.recognize({
      employeId:  v.employeId!,
      type:       v.type as 'IN' | 'OUT',
      confidence: v.confidence ?? undefined,
      siteId:     v.siteId || undefined,
    }).subscribe({
      next: () => {
        this.snack.open('Événement envoyé — vérifiez la table en temps réel', 'OK', { duration: 4000 });
        this.ref.close(true);
      },
      error: (e: HttpErrorResponse) => {
        this.submitting = false;
        this.serverError = (e.error as { message?: string })?.message ?? 'Erreur serveur.';
      },
    });
  }
}
