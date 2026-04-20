import { Component, OnInit, inject } from '@angular/core';
import { CommonModule }   from '@angular/common';
import { HttpClient }     from '@angular/common/http';
import { MatCardModule }  from '@angular/material/card';
import { MatIconModule }  from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { BaseChartDirective } from 'ng2-charts';
import { Chart, ChartData, ChartOptions, registerables } from 'chart.js';
import { environment }    from '../../../environments/environment';

let chartRegistered = false;
if (!chartRegistered) {
  Chart.register(...registerables);
  chartRegistered = true;
}

interface DashboardStats {
  totalEmployes:        number;
  congesEnAttente:      number;
  recrutementOuverts:   number;
  formationsPlanifiees: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatProgressSpinnerModule, BaseChartDirective],
  templateUrl: './dashboard.component.html',
  styleUrl:    './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private http = inject(HttpClient);

  loading = true;
  chartsNoData = false;

  kpis = [
    { label: 'Employes actifs',     icon: 'people',       value: '-', color: '#3f51b5', key: 'totalEmployes' },
    { label: 'Conges en attente',   icon: 'beach_access', value: '-', color: '#ff9800', key: 'congesEnAttente' },
    { label: 'Recrutements ouverts',icon: 'person_add',   value: '-', color: '#4caf50', key: 'recrutementOuverts' },
    { label: 'Formations planifiees',icon: 'school',      value: '-', color: '#9c27b0', key: 'formationsPlanifiees' },
  ];

  readonly barChartOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        labels: {
          color: '#3f5b6a',
          font: { family: 'Manrope' }
        }
      }
    }
  };

  readonly doughnutChartOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        labels: {
          color: '#3f5b6a',
          font: { family: 'Manrope' }
        }
      }
    }
  };

  trendChartData: ChartData<'bar', number[], string> = {
    labels: ['Employés', 'Congés', 'Recrutements', 'Formations'],
    datasets: [
      {
        data: [0, 0, 0, 0],
        label: 'Niveau actuel',
        backgroundColor: ['#0b6a5f', '#f4a62a', '#2788c7', '#6a7de0'],
        borderRadius: 8,
      }
    ]
  };

  distributionChartData: ChartData<'doughnut', number[], string> = {
    labels: ['Employés', 'Congés en attente', 'Recrutements', 'Formations'],
    datasets: [
      {
        data: [0, 0, 0, 0],
        backgroundColor: ['#0b6a5f', '#f4a62a', '#2788c7', '#6a7de0'],
        hoverOffset: 8,
      }
    ]
  };

  ngOnInit(): void {
    this.http.get<DashboardStats>(environment.apiBaseUrl + '/api/dashboard/stats').subscribe({
      next: stats => {
        this.kpis = this.kpis.map(k => ({
          ...k,
          value: String((stats as any)[k.key] ?? '-'),
        }));
        this.updateCharts(stats);
        this.loading = false;
      },
      error: () => {
        this.kpis = this.kpis.map(k => ({ ...k, value: '0' }));
        this.setNoDataCharts();
        this.loading = false;
      },
    });
  }

  private updateCharts(stats: DashboardStats): void {
    const values = [
      this.toNumber(stats.totalEmployes),
      this.toNumber(stats.congesEnAttente),
      this.toNumber(stats.recrutementOuverts),
      this.toNumber(stats.formationsPlanifiees),
    ];
    const total = values.reduce((acc, v) => acc + v, 0);
    this.chartsNoData = total === 0;

    if (this.chartsNoData) {
      this.setNoDataCharts();
      return;
    }

    this.trendChartData = {
      labels: ['Employés', 'Congés', 'Recrutements', 'Formations'],
      datasets: [{
        data: values,
        label: 'Niveau actuel',
        backgroundColor: ['#0b6a5f', '#f4a62a', '#2788c7', '#6a7de0'],
        borderRadius: 8,
      }],
    };

    this.distributionChartData = {
      labels: ['Employés', 'Congés en attente', 'Recrutements', 'Formations'],
      datasets: [{
        data: values,
        backgroundColor: ['#0b6a5f', '#f4a62a', '#2788c7', '#6a7de0'],
        hoverOffset: 8,
      }],
    };
  }

  private setNoDataCharts(): void {
    this.chartsNoData = true;

    this.trendChartData = {
      labels: ['Aucune donnée'],
      datasets: [{
        data: [1],
        label: 'Niveau actuel',
        backgroundColor: ['#c8d6de'],
        borderRadius: 8,
      }],
    };

    this.distributionChartData = {
      labels: ['Aucune donnée'],
      datasets: [{
        data: [1],
        backgroundColor: ['#c8d6de'],
        hoverOffset: 8,
      }],
    };
  }

  private toNumber(value: unknown): number {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : 0;
  }
}
