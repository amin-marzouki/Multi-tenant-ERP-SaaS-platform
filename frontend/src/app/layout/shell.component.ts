import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../core/auth.service';
import { ROLE_LABELS, Role } from '../core/models';
import { ADMIN, ANY_ROLE, FINANCE, SALES, SALES_OR_FINANCE, WAREHOUSE } from '../core/roles';

interface NavLink {
  label: string;
  path: string;
  roles: Role[];
}

interface NavSection {
  title: string;
  links: NavLink[];
}

const NAVIGATION: NavSection[] = [
  {
    title: 'Sales',
    links: [
      { label: 'Customers', path: '/sales/customers', roles: SALES_OR_FINANCE },
      { label: 'Quotes', path: '/sales/quotes', roles: SALES },
      { label: 'Orders', path: '/sales/orders', roles: ANY_ROLE },
      { label: 'Invoices', path: '/sales/invoices', roles: SALES_OR_FINANCE },
    ],
  },
  {
    title: 'Inventory',
    links: [
      { label: 'Items', path: '/inventory/items', roles: ANY_ROLE },
      { label: 'Stock', path: '/inventory/stock', roles: WAREHOUSE },
      { label: 'Warehouses', path: '/inventory/warehouses', roles: WAREHOUSE },
    ],
  },
  {
    title: 'Finance',
    links: [
      { label: 'Receivables', path: '/finance/receivables', roles: FINANCE },
      { label: 'Journal', path: '/finance/journal', roles: FINANCE },
      { label: 'Chart of accounts', path: '/finance/accounts', roles: FINANCE },
      { label: 'Trial balance', path: '/finance/trial-balance', roles: FINANCE },
    ],
  },
  {
    title: 'Admin',
    links: [{ label: 'Users', path: '/admin/users', roles: ADMIN }],
  },
];

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="flex min-h-screen">
      <aside class="flex w-60 shrink-0 flex-col bg-slate-900 text-slate-200">
        <div class="border-b border-slate-800 px-5 py-5">
          <div class="text-lg font-semibold text-white">OrbitERP</div>
          <div class="truncate text-xs text-slate-400">{{ user?.tenantName }}</div>
        </div>
        <nav class="flex-1 space-y-5 overflow-y-auto px-3 py-4">
          <a routerLink="/" routerLinkActive="nav-active" [routerLinkActiveOptions]="{ exact: true }" class="nav-link">Dashboard</a>
          @for (section of sections; track section.title) {
            <div>
              <div class="mb-1 px-2 text-[11px] font-semibold uppercase tracking-wider text-slate-500">{{ section.title }}</div>
              @for (link of section.links; track link.path) {
                <a [routerLink]="link.path" routerLinkActive="nav-active" class="nav-link">{{ link.label }}</a>
              }
            </div>
          }
        </nav>
        <div class="border-t border-slate-800 px-5 py-4 text-sm">
          <div class="truncate text-white">{{ user?.fullName || user?.username }}</div>
          <div class="truncate text-xs text-slate-400">{{ roleLabels }}</div>
          <button type="button" class="mt-3 text-xs text-slate-300 hover:text-white" (click)="logout()">Sign out</button>
        </div>
      </aside>
      <main class="min-w-0 flex-1 p-8">
        <router-outlet />
      </main>
    </div>
  `,
})
export class ShellComponent {
  private readonly auth = inject(AuthService);
  readonly user = this.auth.user;
  readonly sections = NAVIGATION.map((section) => ({
    ...section,
    links: section.links.filter((link) => this.auth.hasAnyRole(link.roles)),
  })).filter((section) => section.links.length > 0);

  get roleLabels(): string {
    return (this.user?.roles ?? []).map((role) => ROLE_LABELS[role]).join(', ');
  }

  logout(): void {
    this.auth.logout();
  }
}
