import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { Quote } from '../core/models';
import { errorMessage } from '../core/util';
import { ErrorAlertComponent } from '../shared/error-alert.component';
import { PageHeaderComponent } from '../shared/page-header.component';
import { StatusBadgeComponent } from '../shared/status-badge.component';
import { DocumentLinesComponent } from './document-lines.component';
import { SalesService } from './sales.service';

@Component({
  selector: 'app-quote-detail',
  standalone: true,
  imports: [RouterLink, DatePipe, PageHeaderComponent, ErrorAlertComponent, StatusBadgeComponent, DocumentLinesComponent],
  template: `
    <a routerLink="/sales/quotes" class="btn-link mb-3 inline-block">&larr; Quotes</a>
    <app-error-alert [message]="error" />
    @if (quote; as q) {
      <app-page-header [title]="'Quote ' + q.number" [subtitle]="q.customerName">
        @if (q.status === 'DRAFT') {
          <button class="btn btn-secondary" [disabled]="busy" (click)="reject(q)">Reject</button>
          <button class="btn btn-primary" [disabled]="busy" (click)="convert(q)">Convert to order</button>
        }
        @if (q.orderId) {
          <a class="btn btn-secondary" [routerLink]="['/sales/orders', q.orderId]">Open order</a>
        }
      </app-page-header>
      <dl class="mb-6 grid gap-4 sm:grid-cols-4">
        <div class="card p-4"><dt class="text-xs text-slate-500">Status</dt><dd class="mt-1"><app-status-badge [status]="q.status" /></dd></div>
        <div class="card p-4"><dt class="text-xs text-slate-500">Quote date</dt><dd class="mt-1 font-medium">{{ q.quoteDate | date: 'mediumDate' }}</dd></div>
        <div class="card p-4"><dt class="text-xs text-slate-500">Valid until</dt><dd class="mt-1 font-medium">{{ q.validUntil ? (q.validUntil | date: 'mediumDate') : '—' }}</dd></div>
        <div class="card p-4"><dt class="text-xs text-slate-500">Customer</dt><dd class="mt-1 font-medium">{{ q.customerName }}</dd></div>
      </dl>
      <app-document-lines [doc]="q" />
    }
  `,
})
export class QuoteDetailComponent implements OnInit {
  private readonly sales = inject(SalesService);
  private readonly router = inject(Router);
  private readonly id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));

  quote: Quote | null = null;
  error: string | null = null;
  busy = false;

  ngOnInit(): void {
    this.sales.quote(this.id).subscribe({ next: (q) => (this.quote = q), error: (e: unknown) => (this.error = errorMessage(e)) });
  }

  convert(quote: Quote): void {
    this.busy = true;
    this.sales.convertQuote(quote.id).subscribe({
      next: (order) => void this.router.navigate(['/sales/orders', order.id]),
      error: (e: unknown) => this.fail(e),
    });
  }

  reject(quote: Quote): void {
    this.busy = true;
    this.sales.rejectQuote(quote.id).subscribe({
      next: (q) => {
        this.quote = q;
        this.busy = false;
      },
      error: (e: unknown) => this.fail(e),
    });
  }

  private fail(e: unknown): void {
    this.error = errorMessage(e);
    this.busy = false;
  }
}
