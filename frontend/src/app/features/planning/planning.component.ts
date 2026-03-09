import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-planning',
  imports: [MatIconModule, MatCardModule],
  template: `
    <div class="planning-placeholder">
      <mat-card class="info-card">
        <mat-card-content>
          <mat-icon class="big-icon">calendar_month</mat-icon>
          <h1>Planning</h1>
          <p>
            Le module Planning n'est pas encore disponible dans cette version —
            aucun endpoint backend n'expose les données de planning pour l'instant.
          </p>
          <p class="sub">Cette fonctionnalité sera implémentée dans une prochaine release.</p>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .planning-placeholder {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 60vh;
      padding: 24px;
    }
    .info-card {
      max-width: 520px;
      width: 100%;
      text-align: center;
    }
    .big-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: #9e9e9e;
      margin-bottom: 16px;
    }
    h1 { margin: 0 0 12px; color: #3f51b5; }
    p  { color: rgba(0,0,0,0.6); margin: 0 0 8px; }
    .sub { font-size: 12px; color: rgba(0,0,0,0.4); margin: 0; }
  `]
})
export class PlanningComponent {}
