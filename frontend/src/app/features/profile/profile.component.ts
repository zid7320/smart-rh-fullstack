import { Component, OnInit, inject } from '@angular/core';
import { CommonModule }      from '@angular/common';
import { MatCardModule }     from '@angular/material/card';
import { MatIconModule }     from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDividerModule }  from '@angular/material/divider';
import { MatChipsModule }    from '@angular/material/chips';
import { AuthService }       from '../../core/services/auth.service';
import { EmployeApiService } from '../../core/api/employee.service';
import type { Employe }      from '../../core/models';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule, MatIconModule, MatProgressBarModule,
    MatDividerModule, MatChipsModule,
  ],
  templateUrl: './profile.component.html',
  styleUrl:    './profile.component.scss',
})
export class ProfileComponent implements OnInit {
  private auth   = inject(AuthService);
  private empApi = inject(EmployeApiService);

  readonly user = this.auth.currentUser;

  employee: Employe | null = null;
  loading  = true;
  notLinked = false;

  ngOnInit(): void {
    const email = this.user()?.email ?? '';
    if (!email) { this.loading = false; this.notLinked = true; return; }

    this.empApi.getAll({ search: email, size: 5 }).subscribe({
      next: pg => {
        this.employee = pg.content.find(e => e.email === email) ?? null;
        this.notLinked = !this.employee;
        this.loading = false;
      },
      error: () => { this.loading = false; this.notLinked = true; },
    });
  }

  roleLabel(role: string | undefined): string {
    switch (role) {
      case 'ADMIN':    return 'Administrateur';
      case 'RH':       return 'Responsable RH';
      case 'EMPLOYEE': return 'Employé';
      default:         return role ?? '—';
    }
  }
}
