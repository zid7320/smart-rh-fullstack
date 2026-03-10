import { Component, OnInit, inject } from '@angular/core';
import { CommonModule }   from '@angular/common';
import { HttpClient }     from '@angular/common/http';
import { MatCardModule }  from '@angular/material/card';
import { MatIconModule }  from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { environment }    from '../../../environments/environment';

interface DashboardStats {
  totalEmployes:        number;
  congesEnAttente:      number;
  recrutementOuverts:   number;
  formationsPlanifiees: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './dashboard.component.html',
  styleUrl:    './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private http = inject(HttpClient);

  loading = true;

  kpis = [
    { label: 'Employes actifs',     icon: 'people',       value: '-', color: '#3f51b5', key: 'totalEmployes' },
    { label: 'Conges en attente',   icon: 'beach_access', value: '-', color: '#ff9800', key: 'congesEnAttente' },
    { label: 'Recrutements ouverts',icon: 'person_add',   value: '-', color: '#4caf50', key: 'recrutementOuverts' },
    { label: 'Formations planifiees',icon: 'school',      value: '-', color: '#9c27b0', key: 'formationsPlanifiees' },
  ];

  ngOnInit(): void {
    this.http.get<DashboardStats>(environment.apiBaseUrl + '/api/dashboard/stats').subscribe({
      next: stats => {
        this.kpis = this.kpis.map(k => ({
          ...k,
          value: String((stats as any)[k.key] ?? '-'),
        }));
        this.loading = false;
      },
      error: () => { this.loading = false; },
    });
  }
}
