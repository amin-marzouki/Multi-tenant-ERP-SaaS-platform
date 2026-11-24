import { Routes } from '@angular/router';

import { LoginComponent } from './auth/login.component';
import { SignupComponent } from './auth/signup.component';
import { authGuard, roleGuard } from './core/auth.guard';
import { ADMIN, FINANCE, SALES, SALES_OR_FINANCE, WAREHOUSE } from './core/roles';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ShellComponent } from './layout/shell.component';

// Feature pages load on demand, one chunk per page.
export const routes: Routes = [
  { path: 'login', component: LoginComponent, title: 'Sign in · OrbitERP' },
  { path: 'signup', component: SignupComponent, title: 'Create a workspace · OrbitERP' },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    canActivateChild: [roleGuard],
    children: [
      { path: '', component: DashboardComponent, title: 'OrbitERP' },

      { path: 'sales/customers', data: { roles: SALES_OR_FINANCE }, title: 'Customers', loadComponent: () => import('./sales/customers.component').then((m) => m.CustomersComponent) },
      { path: 'sales/quotes', data: { roles: SALES }, title: 'Quotes', loadComponent: () => import('./sales/quotes.component').then((m) => m.QuotesComponent) },
      { path: 'sales/quotes/:id', data: { roles: SALES }, title: 'Quote', loadComponent: () => import('./sales/quote-detail.component').then((m) => m.QuoteDetailComponent) },
      { path: 'sales/orders', title: 'Orders', loadComponent: () => import('./sales/orders.component').then((m) => m.OrdersComponent) },
      { path: 'sales/orders/:id', title: 'Order', loadComponent: () => import('./sales/order-detail.component').then((m) => m.OrderDetailComponent) },
      { path: 'sales/invoices', data: { roles: SALES_OR_FINANCE }, title: 'Invoices', loadComponent: () => import('./sales/invoices.component').then((m) => m.InvoicesComponent) },
      { path: 'sales/invoices/:id', data: { roles: SALES_OR_FINANCE }, title: 'Invoice', loadComponent: () => import('./sales/invoice-detail.component').then((m) => m.InvoiceDetailComponent) },

      { path: 'inventory/items', title: 'Items', loadComponent: () => import('./inventory/items.component').then((m) => m.ItemsComponent) },
      { path: 'inventory/stock', data: { roles: WAREHOUSE }, title: 'Stock', loadComponent: () => import('./inventory/stock.component').then((m) => m.StockComponent) },
      { path: 'inventory/warehouses', data: { roles: WAREHOUSE }, title: 'Warehouses', loadComponent: () => import('./inventory/warehouses.component').then((m) => m.WarehousesComponent) },

      { path: 'finance/receivables', data: { roles: FINANCE }, title: 'Receivables', loadComponent: () => import('./finance/receivables.component').then((m) => m.ReceivablesComponent) },
      { path: 'finance/journal', data: { roles: FINANCE }, title: 'Journal', loadComponent: () => import('./finance/journal.component').then((m) => m.JournalComponent) },
      { path: 'finance/accounts', data: { roles: FINANCE }, title: 'Chart of accounts', loadComponent: () => import('./finance/accounts.component').then((m) => m.AccountsComponent) },
      { path: 'finance/trial-balance', data: { roles: FINANCE }, title: 'Trial balance', loadComponent: () => import('./finance/trial-balance.component').then((m) => m.TrialBalanceComponent) },

      { path: 'admin/users', data: { roles: ADMIN }, title: 'Users', loadComponent: () => import('./admin/users.component').then((m) => m.UsersComponent) },
    ],
  },
  { path: '**', redirectTo: '' },
];
