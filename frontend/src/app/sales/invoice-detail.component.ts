import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { AuthService } from '../core/auth.service';
import { Invoice } from '../core/models';
import { FINANCE } from '../core/roles';
import { errorMessage } from '../core/util';
import { ErrorAlertComponent } from '../shared/error-alert.component';
import { PageHeaderComponent } from '../shared/page-header.component';
import { StatusBadgeComponent } from '../shared/status-badge.component';
import { DocumentLinesComponent } from './document-lines.component';
import { SalesService } from './sales.service';

@Component({
  selector: 'app-invoice-detail',
  standalone: true,
  imports: [RouterLink, DatePipe, PageHeaderComponent, ErrorAlertComponent, StatusBadgeComponent, DocumentLinesComponent],
  template: `
    <a routerLink="/sales/invoices" class="btn-link mb-3 inline-block">&larr; Invoices</a>
    <app-error-alert [message]="error" />
    @if (invoice; as i) {
      <app-page-header [title]="'Invoice ' + i.number" [subtitle]="i.customerName">
        @if (i.orderId) {
          <a class="btn btn-secondary" [routerLink]="['/sales/orders', i.orderId]">Open order</a>
        }
        @if (canPost && i.status === 'DRAFT') {
          <button class="btn btn-primary" [disabled]="busy" (click)="post()">Post invoice</button>
        }
        @if (canPost && i.status === 'POSTED') {
          <a class="btn btn-primary" routerLink="/finance/receivables">Record payment</a>
        }
      </app-page-header>
      <dl class="mb-6 grid gap-4 sm:grid-cols-4">
        <div class="card p-4"><dt class="text-xs text-slate-500">Status</dt><dd class="mt-1"><app-status-badge [status]="i.status" /></dd></div>
        <div class="card p-4"><dt class="text-xs text-slate-500">Issue date</dt><dd class="mt-1 font-medium">{{ i.issueDate | date: 'mediumDate' }}</dd></div>
        <div class="card p-4"><dt class="text-xs text-slate-500">Due date</dt><dd class="mt-1 font-medium">{{ i.dueDate | date: 'mediumDate' }}</dd></div>
        <div class="card p-4"><dt class="text-xs text-slate-500">Customer</dt><dd class="mt-1 font-medium">{{ i.customerName }}</dd></div>
      </dl>
      @if (i.status === 'DRAFT') {
        <p class="mb-4 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
          Draft: posting books revenue and tax and opens a receivable for the customer.
        </p>
      }
      <app-document-lines [doc]="i" />
    }
  `,
})
export class InvoiceDetailComponent implements OnInit {
  private readonly sales = inject(SalesService);
  private readonly id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));
  readonly canPost = inject(AuthService).hasAnyRole(FINANCE);

  invoice: Invoice | null = null;
  error: string | null = null;
  busy = false;

  ngOnInit(): void {
    this.sales.invoice(this.id).subscribe({ next: (i) => (this.invoice = i), error: (e: unknown) => (this.error = errorMessage(e)) });
  }

  post(): void {
    this.busy = true;
    this.error = null;
    this.sales.postInvoice(this.id).subscribe({
      next: (invoice) => {
        this.invoice = invoice;
        this.busy = false;
      },
      error: (e: unknown) => {
        this.error = errorMessage(e);
        this.busy = false;
      },
    });
  }
}
