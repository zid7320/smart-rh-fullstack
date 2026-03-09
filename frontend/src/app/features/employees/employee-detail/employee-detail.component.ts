import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink }     from '@angular/router';
import { CommonModule }                   from '@angular/common';
import { MatTabsModule }                  from '@angular/material/tabs';
import { MatCardModule }                  from '@angular/material/card';
import { MatIconModule }                  from '@angular/material/icon';
import { MatButtonModule }                from '@angular/material/button';
import { MatTableModule }                 from '@angular/material/table';
import { MatChipsModule }                 from '@angular/material/chips';
import { MatProgressSpinnerModule }       from '@angular/material/progress-spinner';
import { MatTooltipModule }               from '@angular/material/tooltip';
import { MatSnackBar }                    from '@angular/material/snack-bar';
import { EmployeApiService }              from '../../../core/api/employee.service';
import { ContratApiService }              from '../../../core/api/contract.service';
import { CongeApiService }                from '../../../core/api/leave.service';
import { PaieApiService }                 from '../../../core/api/payroll.service';
import { EvaluationApiService }           from '../../../core/api/evaluation.service';
import { FormationApiService }            from '../../../core/api/training.service';
import { AttendanceApiService }           from '../../../core/api/attendance.service';
import { DossierRhApiService }            from '../../../core/api/dossier.service';
import type { Employe, Contrat, Conge, Paie, Evaluation, Formation, DossierRH } from '../../../core/models';
import type { AttendanceRecord }          from '../../../core/models/attendance.model';

const MOIS = ['','Jan','Fév','Mar','Avr','Mai','Jun','Jul','Aoû','Sep','Oct','Nov','Déc'];

@Component({
  selector: 'app-employee-detail',
  standalone: true,
  imports: [
    CommonModule, RouterLink,
    MatTabsModule, MatCardModule, MatIconModule, MatButtonModule,
    MatTableModule, MatChipsModule, MatProgressSpinnerModule, MatTooltipModule,
  ],
  templateUrl: './employee-detail.component.html',
  styleUrl:    './employee-detail.component.scss',
})
export class EmployeeDetailComponent {
  private route       = inject(ActivatedRoute);
  private api         = inject(EmployeApiService);
  private contratApi  = inject(ContratApiService);
  private congeApi    = inject(CongeApiService);
  private paieApi     = inject(PaieApiService);
  private evalApi     = inject(EvaluationApiService);
  private formApi     = inject(FormationApiService);
  private attendApi   = inject(AttendanceApiService);
  private dossierApi  = inject(DossierRhApiService);
  private snack       = inject(MatSnackBar);

  employe     = signal<Employe | null>(null);
  loading     = signal(true);
  error       = signal('');
  dossier     = signal<DossierRH | null>(null);
  contrats    = signal<Contrat[]>([]);
  conges      = signal<Conge[]>([]);
  paies       = signal<Paie[]>([]);
  evaluations = signal<Evaluation[]>([]);
  formations  = signal<Formation[]>([]);
  attendances = signal<AttendanceRecord[]>([]);

  readonly MOIS = MOIS;

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.api.getById(id).subscribe({
      next: e  => { this.employe.set(e); this.loading.set(false); this.loadSubData(id); },
      error: () => { this.error.set('Employé introuvable.'); this.loading.set(false); },
    });
  }

  private loadSubData(id: number): void {
    this.dossierApi.getByEmployee(id).subscribe({ next: d => this.dossier.set(d), error: () => {} });
    this.contratApi.getByEmployee(id, { size: 10 }).subscribe({ next: p => this.contrats.set(p.content), error: () => {} });
    this.congeApi.getByEmployee(id, { size: 10 }).subscribe({ next: p => this.conges.set(p.content), error: () => {} });
    this.paieApi.getByEmployee(id, { size: 12 }).subscribe({ next: p => this.paies.set(p.content), error: () => {} });
    this.evalApi.getByEmployee(id, { size: 10 }).subscribe({ next: p => this.evaluations.set(p.content), error: () => {} });
    this.formApi.getByEmployee(id, { size: 10 }).subscribe({ next: p => this.formations.set(p.content), error: () => {} });
    this.attendApi.getByEmployee(id, { size: 20 }).subscribe({ next: p => this.attendances.set(p.content), error: () => {} });
  }

  statutColor(s: string): string {
    if (s === 'APPROVED') return 'primary';
    if (s === 'REJECTED') return 'warn';
    return 'accent';
  }

  downloadPdf(paie: Paie): void {
    this.paieApi.downloadPdf(paie.id).subscribe({
      next: (blob: Blob) => {
        const url  = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'bulletin-' + MOIS[paie.mois] + '-' + paie.annee + '.pdf';
        document.body.appendChild(link); link.click(); document.body.removeChild(link);
        URL.revokeObjectURL(url);
      },
      error: () => { this.snack.open('Erreur lors du téléchargement.', 'OK', { duration: 3000 }); }
    });
  }

  typeColor(type: string): string { return type === 'IN' ? 'primary' : 'warn'; }
}
