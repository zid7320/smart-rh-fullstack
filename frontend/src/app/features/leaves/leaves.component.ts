import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-leaves',
  imports: [MatIconModule],
  template: `
    <div class="placeholder-page">
      <mat-icon class="placeholder-icon">beach_access</mat-icon>
      <h1>Congés</h1>
      <p>Module en cours de développement.</p>
    </div>
  `,
  styles: [`
    .placeholder-page { display:flex; flex-direction:column; align-items:center; justify-content:center;
      min-height:60vh; gap:16px; color:rgba(0,0,0,.5);
      .placeholder-icon { font-size:72px; width:72px; height:72px; opacity:.3; }
      h1 { margin:0; color:#3f51b5; } p { margin:0; font-size:14px; }
    }
  `]
})
export class LeavesComponent {}
