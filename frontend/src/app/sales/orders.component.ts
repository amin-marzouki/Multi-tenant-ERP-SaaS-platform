import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../core/auth.service';
import { DocumentSummary } from '../core/models';
import { SALES } from '../core/roles';
import { errorMessage } from '../core/util';
import { ErrorAlertComponent } from '../shared/error-alert.component';
import { ModalComponent } from '../shared/modal.component';
import { PageHeaderComponent } from '../shared/page-header.component';
import { DocumentDraft, DocumentFormComponent } from './document-form.component';
import { DocumentListComponent } from './document-list.component';
import { SalesService } from './sales.service';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [PageHeaderComponent, ModalComponent, ErrorAlertComponent, DocumentFormComponent, DocumentListComponent],
  template: `
    <app-page-header title="Sales orders" subtitle="Confirming reserves stock, shipping issues it, then the order is invoiced.">
      @if (canCreate) {
        <button class="btn btn-primary" (click)="showForm = true; formError = null">New order</button>
      }
    </app-page-header>
    <app-error-alert [message]="error" />
    <app-document-list [documents]="orders" basePath="/sales/orders" dateLabel="Order date" [loading]="loading" emptyText="No orders yet." />

    <app-modal [open]="showForm" title="New sales order" size="xl" (closed)="showForm = false">
      @if (showForm) {
        <app-document-form
          dateLabel="Order date"
          submitLabel="Create order"
          [saving]="saving"
          [error]="formError"
          (submitted)="create($event)"
          (cancelled)="showForm = false"
        />
      }
    </app-modal>
  `,
})
export class OrdersComponent implements OnInit {
  private readonly sales = inject(SalesService);
  private readonly router = inject(Router);
  readonly canCreate = inject(AuthService).hasAnyRole(SALES);

  orders: DocumentSummary[] = [];
  loading = true;
  error: string | null = null;
  showForm = false;
  saving = false;
  formError: string | null = null;

  ngOnInit(): void {
    this.sales.orders().subscribe({
      next: (orders) => {
        this.orders = orders;
        this.loading = false;
      },
      error: (e: unknown) => {
        this.error = errorMessage(e);
        this.loading = false;
      },
    });
  }

  create(draft: DocumentDraft): void {
    this.saving = true;
    this.sales.createOrder({ customerId: draft.customerId, orderDate: draft.date, notes: draft.notes, lines: draft.lines }).subscribe({
      next: (order) => void this.router.navigate(['/sales/orders', order.id]),
      error: (e: unknown) => {
        this.saving = false;
        this.formError = errorMessage(e);
      },
    });
  }
}
