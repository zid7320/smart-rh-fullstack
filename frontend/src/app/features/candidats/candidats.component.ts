import { Component, OnInit, inject } from '@angular/core';
import { CommonModule }            from '@angular/common';
import { MatCardModule }           from '@angular/material/card';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatButtonModule }         from '@angular/material/button';
import { MatIconModule }           from '@angular/material/icon';
import { MatSelectModule }         from '@angular/material/select';
import { MatFormFieldModule }      from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule }        from '@angular/material/tooltip';
import { MatDialog }               from '@angular/material/dialog';
import { MatSnackBar }             from '@angular/material/snack-bar';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { ReactiveFormsModule as RxForms, FormBuilder, Validators } from '@angular/forms';
import { RecrutementApiService }   from '../../core/api/recruitment.service';
import type { Recrutement, Candidate } from '../../core/models';

// ── Add-candidate dialog (inline) ────────────────────────────────────────────
import { Component as Comp2, inject as inj2 } from '@angular/core';

@Comp2({
  selector: 'app-add-candidate-dialog',
  imports: [MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, RxForms, MatProgressSpinnerModule],
  template: `
    <h2 mat-dialog-title>Ajouter un candidat</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="cand-form">
        <mat-form-field appearance="outline" class="full">
          <mat-label>Nom</mat-label>
          <input matInput formControlName="nom">
          @if (form.get('nom')?.invalid && form.get('nom')?.touched) {
            <mat-error>Nom requis.</mat-error>
          }
        </mat-form-field>
        <mat-form-field appearance="outline" class="full">
          <mat-label>Prénom</mat-label>
          <input matInput formControlName="prenom">
        </mat-form-field>
        <mat-form-field appearance="outline" class="full">
          <mat-label>E-mail</mat-label>
          <input matInput type="email" formControlName="email">
          @if (form.get('email')?.invalid && form.get('email')?.touched) {
            <mat-error>E-mail valide requis.</mat-error>
          }
        </mat-form-field>
        @if (serverError) { <p class="server-error">{{ serverError }}</p> }
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Annuler</button>
      <button mat-raised-button color="primary" (click)="save()" [disabled]="saving">
        @if (saving) { <mat-spinner diameter="18"></mat-spinner> } @else { Ajouter }
      </button>
    </mat-dialog-actions>
  `,
  styles: [`.cand-form{display:flex;flex-direction:column;gap:8px;min-width:380px;padding-top:8px}
  .full{width:100%}.server-error{color:#f44336;font-size:12px;margin:4px 0 0}`]
})
export class AddCandidateDialogComponent {
  recrutementId = inj2<number>(MAT_DIALOG_DATA);
  ref    = inj2(MatDialogRef<AddCandidateDialogComponent>);
  private api   = inj2(RecrutementApiService);
  private fb    = inj2(FormBuilder);
  form = this.fb.group({
    nom:    ['', Validators.required],
    prenom: [''],
    email:  ['', [Validators.required, Validators.email]],
  });
  saving = false; serverError = '';
  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.api.addCandidate(this.recrutementId, this.form.value as any).subscribe({
      next: c => { this.saving = false; this.ref.close(c); },
      error: () => { this.saving = false; this.serverError = 'Erreur lors de l\'ajout.'; }
    });
  }
}

// ── Main component ────────────────────────────────────────────────────────────
@Component({
  selector: 'app-candidats',
  imports: [
    CommonModule, MatCardModule, MatTableModule, MatButtonModule, MatIconModule,
    MatSelectModule, MatFormFieldModule, MatProgressSpinnerModule, MatTooltipModule,
    ReactiveFormsModule,
  ],
  template: `
    <div class="page-header">
      <h2>Candidats</h2>
    </div>

    <mat-card class="filter-card">
      <mat-card-content>
        <mat-form-field appearance="outline" class="recr-select">
          <mat-label>Sélectionner un recrutement</mat-label>
          <mat-select [formControl]="recrutementCtrl" (selectionChange)="onSelectRecrutement($event.value)">
            @for (r of recrutements; track r.id) {
              <mat-option [value]="r.id">
                #{{ r.id }} — {{ r.posteCible }} ({{ r.statut }})
              </mat-option>
            }
          </mat-select>
        </mat-form-field>
        @if (selectedId) {
          <button mat-raised-button color="accent" (click)="addCandidate()" class="add-btn">
            <mat-icon>person_add</mat-icon> Ajouter un candidat
          </button>
        }
      </mat-card-content>
    </mat-card>

    @if (loading) {
      <div class="center"><mat-spinner></mat-spinner></div>
    } @else if (selectedId) {
      <mat-card>
        <mat-card-content>
          <table mat-table [dataSource]="dataSource" class="full-table">

            <ng-container matColumnDef="nom">
              <th mat-header-cell *matHeaderCellDef>Nom</th>
              <td mat-cell *matCellDef="let c">{{ c.nom }}</td>
            </ng-container>

            <ng-container matColumnDef="prenom">
              <th mat-header-cell *matHeaderCellDef>Prénom</th>
              <td mat-cell *matCellDef="let c">{{ c.prenom }}</td>
            </ng-container>

            <ng-container matColumnDef="email">
              <th mat-header-cell *matHeaderCellDef>E-mail</th>
              <td mat-cell *matCellDef="let c">{{ c.email }}</td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedCols"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedCols;"></tr>
            <tr class="mat-row" *matNoDataRow>
              <td class="mat-cell no-data" [attr.colspan]="displayedCols.length">Aucun candidat pour ce recrutement.</td>
            </tr>
          </table>
        </mat-card-content>
      </mat-card>
    }
  `,
  styles: [`
    .page-header { display: flex; align-items: center; margin-bottom: 16px; h2 { margin: 0; } }
    .filter-card { margin-bottom: 16px; }
    mat-card-content { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; }
    .recr-select { min-width: 320px; }
    .add-btn { height: 56px; }
    .center { display: flex; justify-content: center; padding: 48px; }
    .full-table { width: 100%; }
    .no-data { text-align: center; padding: 24px; color: rgba(0,0,0,0.4); }
  `]
})
export class CandidatsComponent implements OnInit {
  private api    = inject(RecrutementApiService);
  private dialog = inject(MatDialog);

  recrutements   : Recrutement[] = [];
  recrutementCtrl = new FormControl<number | null>(null);
  selectedId     = 0;
  loading        = false;
  dataSource     = new MatTableDataSource<Candidate>();
  displayedCols  = ['nom', 'prenom', 'email'];

  ngOnInit(): void {
    this.api.getAll({ size: 100 }).subscribe(p => { this.recrutements = p.content; });
  }

  onSelectRecrutement(id: number): void {
    this.selectedId = id;
    this.loading    = true;
    this.api.getCandidates(id, { size: 50 }).subscribe({
      next: p => { this.dataSource.data = p.content; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  addCandidate(): void {
    this.dialog.open(AddCandidateDialogComponent, { width: '420px', data: this.selectedId })
      .afterClosed().subscribe((candidate: Candidate | undefined) => {
        if (!candidate) return;
        this.dataSource.data = [candidate, ...this.dataSource.data];
      });
  }
}
