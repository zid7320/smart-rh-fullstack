import { Component, OnInit, OnDestroy, inject, computed } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs/operators';
import { AuthService } from '../core/services/auth.service';
import { WebsocketService } from '../core/websocket/websocket.service';

interface NavItem {
  label: string;
  icon:  string;
  route: string;
  roles: string[];
}

// ─────────────────────────────────────────────────────────────────────────────
// All navigation items.
// roles: which AppRole values may see this item.
//   'ADMIN' → everything
//   'RH'    → management modules
//   'EMPLOYEE' → own data only
// ─────────────────────────────────────────────────────────────────────────────
const ALL_NAV_ITEMS: NavItem[] = [
  { label: 'Tableau de bord', icon: 'dashboard',     route: '/dashboard',    roles: ['ADMIN', 'RH', 'EMPLOYEE'] },
  { label: 'Employés',        icon: 'people',         route: '/employees',    roles: ['ADMIN', 'RH'] },
  { label: 'Postes',          icon: 'work',           route: '/posts',        roles: ['ADMIN', 'RH'] },
  { label: 'Compétences',     icon: 'military_tech',  route: '/competences',  roles: ['ADMIN', 'RH'] },
  { label: 'Recrutements',    icon: 'person_add',     route: '/recruitments', roles: ['ADMIN', 'RH'] },
  { label: 'Congés',          icon: 'beach_access',   route: '/leaves',       roles: ['ADMIN', 'RH', 'EMPLOYEE'] },
  { label: 'Paie',            icon: 'payments',       route: '/payroll',      roles: ['ADMIN', 'RH', 'EMPLOYEE'] },
  { label: 'Évaluations',     icon: 'star_rate',      route: '/evaluations',  roles: ['ADMIN', 'RH', 'EMPLOYEE'] },
  { label: 'Formations',      icon: 'school',         route: '/trainings',    roles: ['ADMIN', 'RH', 'EMPLOYEE'] },
  { label: 'Présences',       icon: 'fingerprint',    route: '/attendance',   roles: ['ADMIN', 'RH', 'EMPLOYEE'] },
  { label: 'Reporting BI',    icon: 'analytics',      route: '/bi',           roles: ['ADMIN', 'RH'] },
  { label: 'Planning',        icon: 'calendar_month', route: '/planning',     roles: ['ADMIN', 'RH'] },
  { label: 'Contrats',         icon: 'description',    route: '/contracts',    roles: ['ADMIN', 'RH'] },
  { label: 'Dossiers RH',     icon: 'folder_shared',  route: '/dossiers-rh',  roles: ['ADMIN', 'RH'] },
  { label: 'Candidats',       icon: 'contact_page',   route: '/candidats',    roles: ['ADMIN', 'RH'] },
  { label: 'Mon Profil',      icon: 'account_circle', route: '/profile',      roles: ['ADMIN', 'RH', 'EMPLOYEE'] },
];

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.scss',
  imports: [
    RouterOutlet, RouterLink, RouterLinkActive,
    MatToolbarModule, MatSidenavModule, MatListModule,
    MatIconModule, MatButtonModule
  ]
})
export class LayoutComponent implements OnInit, OnDestroy {
  private auth      = inject(AuthService);
  private ws        = inject(WebsocketService);
  private bpObs     = inject(BreakpointObserver);

  // ── Responsive: collapse sidenav on mobile ──────────────────────────────
  readonly isMobile = toSignal(
    this.bpObs.observe(Breakpoints.Handset).pipe(map((r) => r.matches)),
    { initialValue: false }
  );

  // ── Role-based navigation (computed — re-evaluates when role signal changes)
  // Derives visible items from auth.currentUserRole() which is a signal read.
  // Angular's computed() tracks the signal dependency automatically.
  readonly visibleNavItems = computed(() => {
    const role = this.auth.currentUserRole();
    // If role is null (should not happen inside the auth guard) show nothing
    if (!role) return [];
    return ALL_NAV_ITEMS.filter((item) => item.roles.includes(role));
  });

  // ── Current user for toolbar display ────────────────────────────────────
  readonly currentUser = this.auth.currentUser;

  // ── Lifecycle ────────────────────────────────────────────────────────────
  ngOnInit(): void {
    // Connect WebSocket after entering the authenticated shell.
    // Token is already in memory at this point (APP_INITIALIZER ran first).
    this.ws.connect();
  }

  ngOnDestroy(): void {
    // Disconnect when navigating away from the authenticated shell
    // (e.g., after logout, router replaces LayoutComponent with LoginComponent).
    this.ws.disconnect();
  }

  logout(): void {
    this.auth.logout();
    // ngOnDestroy() will fire and call ws.disconnect() automatically.
  }
}
