import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AuthService } from '../core/auth.service';
import { SalesOrder } from '../core/models';
import { SALES, SALES_OR_FINANCE, WAREHOUSE } from '../core/roles';
import { errorMessage, today } from '../core/util';
import { ErrorAlertComponent } from '../shared/error-alert.component';
import { ModalComponent } from '../shared/modal.component';
import { PageHeaderComponent } from '../shared/page-header.component';
import { StatusBadgeComponent } from '../shared/status-badge.component';
import { DocumentLinesComponent } from './document-lines.component';
import { SalesService } from './sales.service';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    DatePipe,
    PageHeaderComponent,
    ErrorAlertComponent,
    ModalComponent,
    StatusBadgeComponent,
    DocumentLinesComponent,
  ],
  template: `
    <a routerLink="/sales/orders" class="btn-link mb-3 inline-block">&larr; Orders</a>
    <app-error-alert [message]="error" />
    @if (order; as o) {
      <app-page-header [title]="'Order ' + o.number" [subtitle]="o.customerName">
        @if (canSell && (o.status === 'DRAFT' || o.status === 'CONFIRMED')) {
          <button class="btn btn-secondary" [disabled]="busy" (click)="act('cancel')">Cancel order</button>
        }
        @if (canSell && o.status === 'DRAFT') {
          <button class="btn btn-primary" [disabled]="busy" (click)="act('confirm')">Confirm and reserve stock</button>
        }
        @if (canShip && o.status === 'CONFIRMED') {
          <button class="btn btn-primary" [disabled]="busy" (click)="act('ship')">Ship</button>
        }
        @if (canInvoice && o.status === 'SHIPPED') {
          <button class="btn btn-primary" [disabled]="busy" (click)="openInvoiceForm()">Create invoice</button>
        }
        @if (o.invoiceId && canSeeInvoices) {
          <a class="btn btn-secondary" [routerLink]="['/sales/invoices', o.invoiceId]">Open invoice</a>
        }
      </app-page-header>
      <dl class="mb-6 grid gap-4 sm:grid-cols-4">
        <div class="card p-4"><dt class="text-xs text-slate-500">Status</dt><dd class="mt-1"><app-status-badge [status]="o.status" /></dd></div>
        <div class="card p-4"><dt class="text-xs text-slate-500">Order date</dt><dd class="mt-1 font-medium">{{ o.orderDate | date: 'mediumDate' }}</dd></div>
        <div class="card p-4"><dt class="text-xs text-slate-500">Customer</dt><dd class="mt-1 font-medium">{{ o.customerName }}</dd></div>
        <div class="card p-4">
          <dt class="text-xs text-slate-500">From quote</dt>
          <dd class="mt-1 font-medium">
            @if (o.quoteId && canSell) {
              <a class="text-indigo-600 hover:text-indigo-500" [routerLink]="['/sales/quotes', o.quoteId]">Open quote</a>
            } @else {
              —
            }
          </dd>
        </div>
      </dl>
      <app-document-lines [doc]="o" />
    }

    <app-modal [open]="showInvoiceForm" title="Create invoice" (closed)="showInvoiceForm = false">
      <form class="space-y-4" (ngSubmit)="invoice()">
        <div class="grid grid-cols-2 gap-4">
          <label class="block"><span class="label">Issue date</span><input class="input" type="date" name="issueDate" [(ngModel)]="invoiceForm.issueDate" /></label>
          <label class="block"><span class="label">Due date</span><input class="input" type="date" name="dueDate" [(ngModel)]="invoiceForm.dueDate" /></label>
        </div>
        <p class="text-xs text-slate-500">Leave the due date empty for 30 days after the issue date.</p>
        <div class="flex justify-end gap-2">
          <button type="button" class="btn btn-secondary" (click)="showInvoiceForm = false">Cancel</button>
          <button class="btn btn-primary" [disabled]="busy">Create draft invoice</button>
        </div>
      </form>
    </app-modal>
  `,
})
export class OrderDetailComponent implements OnInit {
  private readonly sales = inject(SalesService);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));

  readonly canSell = this.auth.hasAnyRole(SALES);
  readonly canShip = this.auth.hasAnyRole(WAREHOUSE);
  readonly canInvoice = this.auth.hasAnyRole(SALES_OR_FINANCE);
  readonly canSeeInvoices = this.auth.hasAnyRole(SALES_OR_FINANCE);

  order: SalesOrder | null = null;
  error: string | null = null;
  busy = false;
  showInvoiceForm = false;
  invoiceForm = { issueDate: today(), dueDate: '' };

  ngOnInit(): void {
    this.sales.order(this.id).subscribe({ next: (o) => (this.order = o), error: (e: unknown) => (this.error = errorMessage(e)) });
  }

  act(action: 'confirm' | 'ship' | 'cancel'): void {
    this.busy = true;
    this.error = null;
    this.sales.orderAction(this.id, action).subscribe({
      next: (order) => {
        this.order = order;
        this.busy = false;
      },
      error: (e: unknown) => this.fail(e),
    });
  }

  openInvoiceForm(): void {
    this.invoiceForm = { issueDate: today(), dueDate: '' };
    this.showInvoiceForm = true;
  }

  invoice(): void {
    this.busy = true;
    this.sales
      .invoiceOrder(this.id, { issueDate: this.invoiceForm.issueDate || null, dueDate: this.invoiceForm.dueDate || null })
      .subscribe({
        next: (invoice) => void this.router.navigate(['/sales/invoices', invoice.id]),
        error: (e: unknown) => {
          this.showInvoiceForm = false;
          this.fail(e);
        },
      });
  }

  private fail(e: unknown): void {
    this.error = errorMessage(e);
    this.busy = false;
  }
}
