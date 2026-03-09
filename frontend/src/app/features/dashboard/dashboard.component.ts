import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
  imports: [MatCardModule, MatIconModule]
})
export class DashboardComponent {
  kpis = [
    { label: 'Employés actifs',    icon: 'people',       value: '–', color: '#3f51b5' },
    { label: 'Congés en attente',  icon: 'beach_access', value: '–', color: '#ff9800' },
    { label: 'Recrutements ouverts', icon: 'person_add', value: '–', color: '#4caf50' },
    { label: 'Formations planifiées',icon: 'school',     value: '–', color: '#9c27b0' }
  ];
}
