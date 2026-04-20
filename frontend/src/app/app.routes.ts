import { Routes } from '@angular/router';
import { authGuard }          from './core/guards/auth.guard';
import { roleGuard }          from './core/guards/role.guard';
import { LayoutComponent }    from './layout/layout.component';

const RH_ADMIN = roleGuard(['ADMIN', 'RH']);

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then((m) => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register.component').then((m) => m.RegisterComponent)
  },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },

      { path: 'dashboard', loadComponent: () =>
          import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent) },

      { path: 'profile', loadComponent: () =>
          import('./features/profile/profile.component').then((m) => m.ProfileComponent) },

      { path: 'employees', canActivate: [RH_ADMIN], loadComponent: () =>
          import('./features/employees/employees.component').then((m) => m.EmployeesComponent) },

      { path: 'employees/:id', canActivate: [RH_ADMIN], loadComponent: () =>
          import('./features/employees/employee-detail/employee-detail.component')
            .then((m) => m.EmployeeDetailComponent) },

      { path: 'posts', canActivate: [RH_ADMIN], loadComponent: () =>
          import('./features/posts/posts.component').then((m) => m.PostsComponent) },

      { path: 'competences', canActivate: [RH_ADMIN], loadComponent: () =>
          import('./features/competences/competences.component').then((m) => m.CompetencesComponent) },

      { path: 'recruitments', loadComponent: () =>
          import('./features/recrutements/recrutements.component').then((m) => m.RecrutementsComponent) },

      { path: 'leaves', loadComponent: () =>
          import('./features/conges/conges.component').then((m) => m.CongesComponent) },

      { path: 'payroll', loadComponent: () =>
          import('./features/paie/paie.component').then((m) => m.PaieComponent) },

      { path: 'evaluations', loadComponent: () =>
          import('./features/evaluations/evaluations.component').then((m) => m.EvaluationsComponent) },

      { path: 'trainings', loadComponent: () =>
          import('./features/formations/formations.component').then((m) => m.FormationsComponent) },

      { path: 'attendance', loadComponent: () =>
          import('./features/attendance/attendance.component').then((m) => m.AttendanceComponent) },

      { path: 'planning', canActivate: [RH_ADMIN], loadComponent: () =>
          import('./features/planning/planning.component').then((m) => m.PlanningComponent) },

      { path: 'dossiers-rh', canActivate: [RH_ADMIN], loadComponent: () =>
          import('./features/dossiers-rh/dossiers-rh.component').then((m) => m.DossiersRhComponent) },

      { path: 'contracts', canActivate: [RH_ADMIN], loadComponent: () =>
          import('./features/contracts/contracts.component').then((m) => m.ContractsComponent) },

      { path: 'candidats', canActivate: [RH_ADMIN], loadComponent: () =>
          import('./features/candidats/candidats.component').then((m) => m.CandidatsComponent) },

      { path: 'bi', canActivate: [RH_ADMIN], loadComponent: () =>
          import('./features/bi/bi-dashboard.component').then((m) => m.BiDashboardComponent) },
    ]
  },
  { path: '**', redirectTo: '/login' }
];
