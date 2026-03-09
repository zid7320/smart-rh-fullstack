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
import { EmployeApiService }   from '../../../core/api/employee.service';
import { PosteApiService }     from '../../../core/api/post.service';
import type { Employe, Poste } from '../../../core/models';

@Component({
  selector: 'app-employee-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule,
    MatFormFieldModule, MatInputModule, MatSelectModule,
    MatButtonModule, MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Modifier employé' : 'Nouvel employé' }}</h2>
    <mat-dialog-content class="form-content">
      <form [formGroup]="form" id="empForm" (ngSubmit)="submit()" autocomplete="off">
        <mat-form-field appearance="outline" class="full">
          <mat-label>Nom</mat-label>
          <input matInput formControlName="nom">
          @if (form.get('nom')?.touched && form.get('nom')?.hasError('required')) {
            <mat-error>Le nom est requis.</mat-error>
          }
          @if (form.get('nom')?.hasError('maxlength')) {
            <mat-error>100 caractères maximum.</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>Prénom</mat-label>
          <input matInput formControlName="prenom">
          @if (form.get('prenom')?.touched && form.get('prenom')?.hasError('required')) {
            <mat-error>Le prénom est requis.</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>E-mail</mat-label>
          <input matInput formControlName="email" type="email">
          @if (form.get('email')?.touched && form.get('email')?.hasError('required')) {
            <mat-error>L'e-mail est requis.</mat-error>
          }
          @if (form.get('email')?.touched && form.get('email')?.hasError('email')) {
            <mat-error>Format e-mail invalide.</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>Intitulé du poste (libre)</mat-label>
          <input matInput formControlName="posteLibelle" placeholder="ex: Développeur Senior">
        </mat-form-field>

        <mat-form-field appearance="outline" class="full">
          <mat-label>Lier à un poste</mat-label>
          <mat-select formControlName="posteId">
            <mat-option [value]="null">— Aucun —</mat-option>
            @for (p of posts; track p.id) {
              <mat-option [value]="p.id">{{ p.titre }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        @if (serverError) {
          <p class="srv-err">{{ serverError }}</p>
        }
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Annuler</button>
      <button mat-raised-button color="primary" form="empForm" type="submit" [disabled]="saving">
        @if (saving) { <mat-spinner diameter="18"></mat-spinner> }
        @else { {{ isEdit ? 'Enregistrer' : 'Créer' }} }
      </button>
    </mat-dialog-actions>
  `,
  styles: [`.full{width:100%;margin-bottom:4px}.form-content{min-width:420px}.srv-err{color:#c62828;font-size:.85rem}`],
})
export class EmployeeFormComponent implements OnInit {
  data   = inject<Employe | null>(MAT_DIALOG_DATA);
  ref    = inject(MatDialogRef<EmployeeFormComponent>);
  private api     = inject(EmployeApiService);
  private postApi = inject(PosteApiService);
  private fb      = inject(FormBuilder);
  private snack   = inject(MatSnackBar);

  isEdit      = !!this.data;
  saving      = false;
  serverError = '';
  posts: Poste[] = [];

  form = this.fb.group({
    nom:          [this.data?.nom          ?? '', [Validators.required, Validators.maxLength(100)]],
    prenom:       [this.data?.prenom       ?? '', [Validators.required, Validators.maxLength(100)]],
    email:        [this.data?.email        ?? '', [Validators.required, Validators.email, Validators.maxLength(255)]],
    posteLibelle: [this.data?.posteLibelle ?? '',  Validators.maxLength(150)],
    posteId:      [this.data?.posteId      ?? null],
  });

  ngOnInit(): void {
    this.postApi.getAll({ page: 0, size: 100 }).subscribe({ next: pg => this.posts = pg.content });
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.serverError = '';
    const body = this.form.value;
    const op$  = this.isEdit
      ? this.api.update(this.data!.id, body as any)
      : this.api.create(body as any);
    op$.subscribe({
      next: () => {
        this.snack.open(this.isEdit ? 'Employé mis à jour' : 'Employé créé', 'OK', { duration: 3000 });
        this.ref.close(true);
      },
      error: (e: HttpErrorResponse) => {
        this.saving = false;
        this.serverError = (e.error as { message?: string })?.message ?? 'Erreur serveur.';
      },
    });
  }
}
