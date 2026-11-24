import { DecimalPipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AuthService } from '../core/auth.service';
import { AgingBuckets, Role } from '../core/models';
import { ANY_ROLE, FINANCE, SALES, WAREHOUSE } from '../core/roles';
import { FinanceService } from '../finance/finance.service';
import { PageHeaderComponent } from '../shared/page-header.component';

interface Shortcut {
  title: string;
  text: string;
  path: string;
  roles: Role[];
}

const SHORTCUTS: Shortcut[] = [
  { title: 'New quote', text: 'Price a deal for a customer.', path: '/sales/quotes', roles: SALES },
  { title: 'Sales orders', text: 'Confirm, ship and invoice orders.', path: '/sales/orders', roles: ANY_ROLE },
  { title: 'Stock', text: 'Receive goods and check what is available.', path: '/inventory/stock', roles: WAREHOUSE },
  { title: 'Receivables', text: 'Record payments and follow overdue invoices.', path: '/finance/receivables', roles: FINANCE },
  { title: 'Journal', text: 'Every posting, automatic or manual.', path: '/finance/journal', roles: FINANCE },
  { title: 'Items', text: 'What you sell, with prices and stock.', path: '/inventory/items', roles: ANY_ROLE },
];

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, DecimalPipe, PageHeaderComponent],
  template: `
    <app-page-header [title]="'Welcome, ' + name" [subtitle]="user?.tenantName ?? ''" />

    @if (aging) {
      <div class="mb-8 grid gap-4 sm:grid-cols-3">
        <div class="card p-5">
          <div class="text-sm text-slate-500">Open receivables</div>
          <div class="mt-1 text-2xl font-semibold">{{ aging.total | number: '1.2-2' }}</div>
        </div>
        <div class="card p-5">
          <div class="text-sm text-slate-500">Not yet due</div>
          <div class="mt-1 text-2xl font-semibold text-emerald-700">{{ aging.current | number: '1.2-2' }}</div>
        </div>
        <div class="card p-5">
          <div class="text-sm text-slate-500">Overdue</div>
          <div class="mt-1 text-2xl font-semibold text-rose-700">{{ aging.total - aging.current | number: '1.2-2' }}</div>
        </div>
      </div>
    }

    <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
      @for (shortcut of shortcuts; track shortcut.path) {
        <a [routerLink]="shortcut.path" class="card block p-5 transition hover:border-indigo-300 hover:shadow">
          <div class="font-semibold text-slate-900">{{ shortcut.title }}</div>
          <p class="mt-1 text-sm text-slate-500">{{ shortcut.text }}</p>
        </a>
      }
    </div>
  `,
})
export class DashboardComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly finance = inject(FinanceService);

  readonly user = this.auth.user;
  readonly name = this.user?.fullName || this.user?.username || '';
  readonly shortcuts = SHORTCUTS.filter((s) => this.auth.hasAnyRole(s.roles));
  aging: AgingBuckets | null = null;

  ngOnInit(): void {
    if (this.auth.hasAnyRole(FINANCE)) {
      this.finance.aging().subscribe((report) => (this.aging = report.totals));
    }
  }
}
