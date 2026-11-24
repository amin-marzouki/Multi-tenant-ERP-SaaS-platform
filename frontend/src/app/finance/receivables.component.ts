import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { AgingReport, PAYMENT_METHODS, Payment, PaymentMethod, Receivable } from '../core/models';
import { errorMessage, today } from '../core/util';
import { ErrorAlertComponent } from '../shared/error-alert.component';
import { ModalComponent } from '../shared/modal.component';
import { PageHeaderComponent } from '../shared/page-header.component';
import { StatusBadgeComponent } from '../shared/status-badge.component';
import { FinanceService } from './finance.service';

@Component({
  selector: 'app-receivables',
  standalone: true,
  imports: [FormsModule, RouterLink, DatePipe, DecimalPipe, PageHeaderComponent, ModalComponent, ErrorAlertComponent, StatusBadgeComponent],
  template: `
    <app-page-header title="Receivables" subtitle="What customers owe on posted invoices." />
    <app-error-alert [message]="error" />

    @if (aging; as report) {
      <h2 class="mb-2 text-sm font-semibold uppercase tracking-wide text-slate-500">Aging on {{ report.asOf | date: 'mediumDate' }}</h2>
      <div class="card mb-8 overflow-hidden">
        <table class="data-table">
          <thead>
            <tr>
              <th>Customer</th><th class="text-right">Not due</th><th class="text-right">1-30</th><th class="text-right">31-60</th>
              <th class="text-right">61-90</th><th class="text-right">Over 90</th><th class="text-right">Total</th>
            </tr>
          </thead>
          <tbody>
            @for (row of report.rows; track row.customerId) {
              <tr>
                <td class="font-medium">{{ row.customerName }}</td>
                <td class="text-right tabular-nums">{{ row.buckets.current | number: '1.2-2' }}</td>
                <td class="text-right tabular-nums">{{ row.buckets.days1to30 | number: '1.2-2' }}</td>
                <td class="text-right tabular-nums">{{ row.buckets.days31to60 | number: '1.2-2' }}</td>
                <td class="text-right tabular-nums">{{ row.buckets.days61to90 | number: '1.2-2' }}</td>
                <td class="text-right tabular-nums text-rose-700">{{ row.buckets.over90 | number: '1.2-2' }}</td>
                <td class="text-right font-medium tabular-nums">{{ row.buckets.total | number: '1.2-2' }}</td>
              </tr>
            } @empty {
              <tr><td colspan="7" class="py-8 text-center text-slate-500">Nothing is owed right now.</td></tr>
            }
          </tbody>
          @if (report.rows.length) {
            <tfoot>
              <tr class="font-semibold">
                <td>Total</td>
                <td class="text-right tabular-nums">{{ report.totals.current | number: '1.2-2' }}</td>
                <td class="text-right tabular-nums">{{ report.totals.days1to30 | number: '1.2-2' }}</td>
                <td class="text-right tabular-nums">{{ report.totals.days31to60 | number: '1.2-2' }}</td>
                <td class="text-right tabular-nums">{{ report.totals.days61to90 | number: '1.2-2' }}</td>
                <td class="text-right tabular-nums">{{ report.totals.over90 | number: '1.2-2' }}</td>
                <td class="text-right tabular-nums">{{ report.totals.total | number: '1.2-2' }}</td>
              </tr>
            </tfoot>
          }
        </table>
      </div>
    }

    <h2 class="mb-2 text-sm font-semibold uppercase tracking-wide text-slate-500">Invoices</h2>
    <div class="card mb-8 overflow-hidden">
      <table class="data-table">
        <thead>
          <tr>
            <th>Invoice</th><th>Customer</th><th>Due</th><th class="text-right">Amount</th><th class="text-right">Open</th><th>Status</th><th></th>
          </tr>
        </thead>
        <tbody>
          @for (r of receivables; track r.id) {
            <tr>
              <td><a class="font-medium text-indigo-600 hover:text-indigo-500" [routerLink]="['/sales/invoices', r.invoiceId]">{{ r.invoiceNumber }}</a></td>
              <td>{{ r.customerName }}</td>
              <td class="whitespace-nowrap">
                {{ r.dueDate | date: 'mediumDate' }}
                @if (r.daysOverdue > 0) {
                  <span class="ml-1 text-xs text-rose-600">{{ r.daysOverdue }} days late</span>
                }
              </td>
              <td class="text-right tabular-nums">{{ r.amount | number: '1.2-2' }}</td>
              <td class="text-right font-medium tabular-nums">{{ r.openAmount | number: '1.2-2' }}</td>
              <td><app-status-badge [status]="r.status" /></td>
              <td class="text-right">
                @if (r.status === 'OPEN') {
                  <button class="btn-link" (click)="openPayment(r)">Record payment</button>
                }
              </td>
            </tr>
          } @empty {
            <tr><td colspan="7" class="py-8 text-center text-slate-500">No posted invoices yet.</td></tr>
          }
        </tbody>
      </table>
    </div>

    <h2 class="mb-2 text-sm font-semibold uppercase tracking-wide text-slate-500">Recent payments</h2>
    <div class="card overflow-hidden">
      <table class="data-table">
        <thead><tr><th>Date</th><th>Invoice</th><th>Customer</th><th>Method</th><th>Reference</th><th class="text-right">Amount</th></tr></thead>
        <tbody>
          @for (p of payments; track p.id) {
            <tr>
              <td>{{ p.paymentDate | date: 'mediumDate' }}</td>
              <td>{{ p.invoiceNumber }}</td>
              <td>{{ p.customerName }}</td>
              <td>{{ methodLabel(p.method) }}</td>
              <td class="text-slate-500">{{ p.reference }}</td>
              <td class="text-right tabular-nums">{{ p.amount | number: '1.2-2' }}</td>
            </tr>
          } @empty {
            <tr><td colspan="6" class="py-8 text-center text-slate-500">No payments yet.</td></tr>
          }
        </tbody>
      </table>
    </div>

    <app-modal [open]="paying !== null" [title]="'Payment for ' + (paying?.invoiceNumber ?? '')" (closed)="paying = null">
      @if (paying; as r) {
        <form class="space-y-4" (ngSubmit)="savePayment(r)">
          <app-error-alert [message]="formError" />
          <p class="text-sm text-slate-600">{{ r.customerName }} still owes <span class="font-semibold">{{ r.openAmount | number: '1.2-2' }}</span>.</p>
          <div class="grid grid-cols-2 gap-4">
            <label class="block"><span class="label">Amount</span><input class="input" type="number" min="0.01" step="0.01" name="amount" [(ngModel)]="form.amount" required /></label>
            <label class="block"><span class="label">Date</span><input class="input" type="date" name="date" [(ngModel)]="form.paymentDate" /></label>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <label class="block">
              <span class="label">Method</span>
              <select class="input" name="method" [(ngModel)]="form.method">
                @for (method of methods; track method) {
                  <option [value]="method">{{ methodLabel(method) }}</option>
                }
              </select>
            </label>
            <label class="block"><span class="label">Reference</span><input class="input" name="reference" [(ngModel)]="form.reference" /></label>
          </div>
          <div class="flex justify-end gap-2 pt-2">
            <button type="button" class="btn btn-secondary" (click)="paying = null">Cancel</button>
            <button class="btn btn-primary" [disabled]="saving">Record payment</button>
          </div>
        </form>
      }
    </app-modal>
  `,
})
export class ReceivablesComponent implements OnInit {
  private readonly finance = inject(FinanceService);

  readonly methods = PAYMENT_METHODS;
  receivables: Receivable[] = [];
  payments: Payment[] = [];
  aging: AgingReport | null = null;
  error: string | null = null;
  paying: Receivable | null = null;
  form = { amount: 0, paymentDate: today(), method: 'BANK_TRANSFER' as PaymentMethod, reference: '' };
  formError: string | null = null;
  saving = false;

  ngOnInit(): void {
    this.load();
  }

  methodLabel(method: PaymentMethod): string {
    return { CASH: 'Cash', BANK_TRANSFER: 'Bank transfer', CHECK: 'Check', CARD: 'Card' }[method];
  }

  openPayment(receivable: Receivable): void {
    this.paying = receivable;
    this.form = { amount: receivable.openAmount, paymentDate: today(), method: 'BANK_TRANSFER', reference: '' };
    this.formError = null;
  }

  savePayment(receivable: Receivable): void {
    this.saving = true;
    this.finance
      .recordPayment({
        receivableId: receivable.id,
        amount: this.form.amount,
        paymentDate: this.form.paymentDate || null,
        method: this.form.method,
        reference: this.form.reference,
      })
      .subscribe({
        next: () => {
          this.saving = false;
          this.paying = null;
          this.load();
        },
        error: (e: unknown) => {
          this.saving = false;
          this.formError = errorMessage(e);
        },
      });
  }

  private load(): void {
    const fail = (e: unknown) => (this.error = errorMessage(e));
    this.finance.receivables().subscribe({ next: (r) => (this.receivables = r), error: fail });
    this.finance.aging().subscribe({ next: (a) => (this.aging = a), error: fail });
    this.finance.payments().subscribe({ next: (p) => (this.payments = p), error: fail });
  }
}
