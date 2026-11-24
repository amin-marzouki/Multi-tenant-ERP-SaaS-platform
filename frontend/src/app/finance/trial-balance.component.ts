import { DecimalPipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';

import { TrialBalance } from '../core/models';
import { errorMessage } from '../core/util';
import { ErrorAlertComponent } from '../shared/error-alert.component';
import { PageHeaderComponent } from '../shared/page-header.component';
import { FinanceService } from './finance.service';

@Component({
  selector: 'app-trial-balance',
  standalone: true,
  imports: [DecimalPipe, PageHeaderComponent, ErrorAlertComponent],
  template: `
    <app-page-header title="Trial balance" subtitle="Total debits and credits per account. Both columns must add up to the same amount." />
    <app-error-alert [message]="error" />
    @if (balance; as tb) {
      <div class="card overflow-hidden">
        <table class="data-table">
          <thead>
            <tr><th class="w-24">Code</th><th>Account</th><th class="text-right">Debit</th><th class="text-right">Credit</th><th class="text-right">Balance</th></tr>
          </thead>
          <tbody>
            @for (row of tb.rows; track row.accountId) {
              <tr>
                <td class="font-mono">{{ row.code }}</td>
                <td>{{ row.name }}</td>
                <td class="text-right tabular-nums">{{ row.debit | number: '1.2-2' }}</td>
                <td class="text-right tabular-nums">{{ row.credit | number: '1.2-2' }}</td>
                <td class="text-right font-medium tabular-nums">
                  {{ abs(row.balance) | number: '1.2-2' }} <span class="text-xs text-slate-500">{{ row.balance >= 0 ? 'Dr' : 'Cr' }}</span>
                </td>
              </tr>
            } @empty {
              <tr><td colspan="5" class="py-10 text-center text-slate-500">No postings yet.</td></tr>
            }
          </tbody>
          <tfoot>
            <tr class="font-semibold">
              <td colspan="2" class="text-right">Totals</td>
              <td class="text-right tabular-nums">{{ tb.totalDebit | number: '1.2-2' }}</td>
              <td class="text-right tabular-nums">{{ tb.totalCredit | number: '1.2-2' }}</td>
              <td class="text-right" [class.text-emerald-700]="tb.totalDebit === tb.totalCredit" [class.text-rose-700]="tb.totalDebit !== tb.totalCredit">
                {{ tb.totalDebit === tb.totalCredit ? 'Balanced' : 'Out of balance' }}
              </td>
            </tr>
          </tfoot>
        </table>
      </div>
    }
  `,
})
export class TrialBalanceComponent implements OnInit {
  private readonly finance = inject(FinanceService);

  balance: TrialBalance | null = null;
  error: string | null = null;

  ngOnInit(): void {
    this.finance.trialBalance().subscribe({ next: (tb) => (this.balance = tb), error: (e: unknown) => (this.error = errorMessage(e)) });
  }

  abs(value: number): number {
    return Math.abs(value);
  }
}
