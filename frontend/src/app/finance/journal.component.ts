import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Account, JournalEntry, JournalEntrySummary } from '../core/models';
import { errorMessage, today } from '../core/util';
import { ErrorAlertComponent } from '../shared/error-alert.component';
import { ModalComponent } from '../shared/modal.component';
import { PageHeaderComponent } from '../shared/page-header.component';
import { StatusBadgeComponent } from '../shared/status-badge.component';
import { FinanceService } from './finance.service';

interface LineDraft {
  accountId: number | null;
  description: string;
  debit: number;
  credit: number;
}

@Component({
  selector: 'app-journal',
  standalone: true,
  imports: [FormsModule, DatePipe, DecimalPipe, PageHeaderComponent, ModalComponent, ErrorAlertComponent, StatusBadgeComponent],
  template: `
    <app-page-header title="Journal" subtitle="Every posting. Invoices and payments post here automatically.">
      <button class="btn btn-primary" (click)="openForm()">Manual entry</button>
    </app-page-header>
    <app-error-alert [message]="error" />

    <div class="card overflow-hidden">
      <table class="data-table">
        <thead><tr><th>Number</th><th>Date</th><th>Description</th><th>Source</th><th class="text-right">Amount</th></tr></thead>
        <tbody>
          @for (entry of entries; track entry.id) {
            <tr class="cursor-pointer hover:bg-slate-50" (click)="toggle(entry)">
              <td class="whitespace-nowrap font-medium text-indigo-600">{{ entry.number }}</td>
              <td class="whitespace-nowrap">{{ entry.entryDate | date: 'mediumDate' }}</td>
              <td>{{ entry.description }}</td>
              <td><app-status-badge [status]="entry.sourceType" /> <span class="ml-1 text-xs text-slate-500">{{ entry.sourceReference }}</span></td>
              <td class="text-right tabular-nums">{{ entry.amount | number: '1.2-2' }}</td>
            </tr>
            @if (expanded?.id === entry.id) {
              <tr>
                <td colspan="5" class="bg-slate-50">
                  <table class="w-full text-sm">
                    <thead>
                      <tr class="text-xs text-slate-500"><th class="py-1 text-left">Account</th><th class="text-left">Description</th><th class="text-right">Debit</th><th class="text-right">Credit</th></tr>
                    </thead>
                    <tbody>
                      @for (line of expanded!.lines; track line.id) {
                        <tr>
                          <td class="py-1"><span class="font-mono text-xs">{{ line.accountCode }}</span> {{ line.accountName }}</td>
                          <td class="text-slate-500">{{ line.description }}</td>
                          <td class="text-right tabular-nums">{{ line.debit ? (line.debit | number: '1.2-2') : '' }}</td>
                          <td class="text-right tabular-nums">{{ line.credit ? (line.credit | number: '1.2-2') : '' }}</td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </td>
              </tr>
            }
          } @empty {
            <tr><td colspan="5" class="py-10 text-center text-slate-500">No entries yet. Posting an invoice creates the first one.</td></tr>
          }
        </tbody>
      </table>
    </div>

    <app-modal [open]="showForm" title="Manual journal entry" size="lg" (closed)="showForm = false">
      <form class="space-y-4" (ngSubmit)="save()">
        <app-error-alert [message]="formError" />
        <div class="grid grid-cols-3 gap-4">
          <label class="block"><span class="label">Date</span><input class="input" type="date" name="date" [(ngModel)]="entryDate" /></label>
          <label class="col-span-2 block"><span class="label">Description</span><input class="input" name="description" [(ngModel)]="description" required /></label>
        </div>
        <table class="data-table rounded-lg border border-slate-200">
          <thead><tr><th>Account</th><th>Description</th><th class="w-32">Debit</th><th class="w-32">Credit</th><th class="w-16"></th></tr></thead>
          <tbody>
            @for (line of lines; let i = $index; track i) {
              <tr>
                <td>
                  <select class="input" [name]="'account' + i" [(ngModel)]="line.accountId">
                    <option [ngValue]="null" disabled>Choose…</option>
                    @for (account of accounts; track account.id) {
                      <option [ngValue]="account.id">{{ account.code }} · {{ account.name }}</option>
                    }
                  </select>
                </td>
                <td><input class="input" [name]="'lineDescription' + i" [(ngModel)]="line.description" /></td>
                <td><input class="input" type="number" min="0" step="0.01" [name]="'debit' + i" [(ngModel)]="line.debit" /></td>
                <td><input class="input" type="number" min="0" step="0.01" [name]="'credit' + i" [(ngModel)]="line.credit" /></td>
                <td class="text-right"><button type="button" class="btn-link text-rose-600" [disabled]="lines.length <= 2" (click)="lines.splice(i, 1)">Remove</button></td>
              </tr>
            }
          </tbody>
          <tfoot>
            <tr class="font-medium">
              <td colspan="2" class="text-right">Totals</td>
              <td class="tabular-nums">{{ totalDebit | number: '1.2-2' }}</td>
              <td class="tabular-nums">{{ totalCredit | number: '1.2-2' }}</td>
              <td></td>
            </tr>
          </tfoot>
        </table>
        <div class="flex items-center justify-between">
          <button type="button" class="btn btn-secondary" (click)="addLine()">Add line</button>
          @if (!balanced) {
            <span class="text-sm text-rose-600">Debits and credits must be equal.</span>
          }
        </div>
        <div class="flex justify-end gap-2 border-t border-slate-100 pt-4">
          <button type="button" class="btn btn-secondary" (click)="showForm = false">Cancel</button>
          <button class="btn btn-primary" [disabled]="saving || !balanced">Post entry</button>
        </div>
      </form>
    </app-modal>
  `,
})
export class JournalComponent implements OnInit {
  private readonly finance = inject(FinanceService);

  entries: JournalEntrySummary[] = [];
  expanded: JournalEntry | null = null;
  accounts: Account[] = [];
  error: string | null = null;
  showForm = false;
  entryDate = today();
  description = '';
  lines: LineDraft[] = [];
  formError: string | null = null;
  saving = false;

  ngOnInit(): void {
    this.load();
    this.finance.accounts().subscribe((accounts) => (this.accounts = accounts.filter((a) => a.active)));
  }

  get totalDebit(): number {
    return this.lines.reduce((sum, l) => sum + (l.debit || 0), 0);
  }

  get totalCredit(): number {
    return this.lines.reduce((sum, l) => sum + (l.credit || 0), 0);
  }

  get balanced(): boolean {
    return this.totalDebit > 0 && Math.abs(this.totalDebit - this.totalCredit) < 0.005;
  }

  toggle(entry: JournalEntrySummary): void {
    if (this.expanded?.id === entry.id) {
      this.expanded = null;
      return;
    }
    this.finance.journalEntry(entry.id).subscribe({ next: (e) => (this.expanded = e), error: (e: unknown) => (this.error = errorMessage(e)) });
  }

  openForm(): void {
    this.entryDate = today();
    this.description = '';
    this.lines = [this.emptyLine(), this.emptyLine()];
    this.formError = null;
    this.showForm = true;
  }

  addLine(): void {
    this.lines.push(this.emptyLine());
  }

  save(): void {
    if (this.lines.some((l) => l.accountId === null)) {
      this.formError = 'Choose an account on every line.';
      return;
    }
    this.saving = true;
    this.finance
      .createJournalEntry({
        entryDate: this.entryDate || null,
        description: this.description,
        lines: this.lines.map((l) => ({ accountId: l.accountId as number, description: l.description, debit: l.debit || 0, credit: l.credit || 0 })),
      })
      .subscribe({
        next: () => {
          this.saving = false;
          this.showForm = false;
          this.load();
        },
        error: (e: unknown) => {
          this.saving = false;
          this.formError = errorMessage(e);
        },
      });
  }

  private load(): void {
    this.finance.journalEntries().subscribe({ next: (e) => (this.entries = e), error: (e: unknown) => (this.error = errorMessage(e)) });
  }

  private emptyLine(): LineDraft {
    return { accountId: null, description: '', debit: 0, credit: 0 };
  }
}
