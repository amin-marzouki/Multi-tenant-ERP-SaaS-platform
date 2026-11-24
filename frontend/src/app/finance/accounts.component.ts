import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ACCOUNT_TYPES, Account, AccountRequest } from '../core/models';
import { errorMessage } from '../core/util';
import { ErrorAlertComponent } from '../shared/error-alert.component';
import { ModalComponent } from '../shared/modal.component';
import { PageHeaderComponent } from '../shared/page-header.component';
import { FinanceService } from './finance.service';

@Component({
  selector: 'app-accounts',
  standalone: true,
  imports: [FormsModule, PageHeaderComponent, ModalComponent, ErrorAlertComponent],
  template: `
    <app-page-header title="Chart of accounts" subtitle="Invoices post to 1100, 4000 and 2100; payments to 1000 and 1100.">
      <button class="btn btn-primary" (click)="openForm()">New account</button>
    </app-page-header>
    <app-error-alert [message]="error" />

    <div class="card overflow-hidden">
      <table class="data-table">
        <thead><tr><th class="w-32">Code</th><th>Name</th><th>Type</th><th>Status</th></tr></thead>
        <tbody>
          @for (account of accounts; track account.id) {
            <tr>
              <td class="font-mono">{{ account.code }}</td>
              <td class="font-medium">{{ account.name }}</td>
              <td>{{ typeLabel(account.type) }}</td>
              <td>{{ account.active ? 'Active' : 'Inactive' }}</td>
            </tr>
          } @empty {
            <tr><td colspan="4" class="py-10 text-center text-slate-500">No accounts.</td></tr>
          }
        </tbody>
      </table>
    </div>

    <app-modal [open]="showForm" title="New account" (closed)="showForm = false">
      <form class="space-y-4" (ngSubmit)="save()">
        <app-error-alert [message]="formError" />
        <div class="grid grid-cols-3 gap-4">
          <label class="block"><span class="label">Code</span><input class="input" name="code" [(ngModel)]="form.code" required /></label>
          <label class="col-span-2 block"><span class="label">Name</span><input class="input" name="name" [(ngModel)]="form.name" required /></label>
        </div>
        <label class="block">
          <span class="label">Type</span>
          <select class="input" name="type" [(ngModel)]="form.type">
            @for (type of types; track type) {
              <option [value]="type">{{ typeLabel(type) }}</option>
            }
          </select>
        </label>
        <div class="flex justify-end gap-2 pt-2">
          <button type="button" class="btn btn-secondary" (click)="showForm = false">Cancel</button>
          <button class="btn btn-primary" [disabled]="saving">Save</button>
        </div>
      </form>
    </app-modal>
  `,
})
export class AccountsComponent implements OnInit {
  private readonly finance = inject(FinanceService);

  readonly types = ACCOUNT_TYPES;
  accounts: Account[] = [];
  error: string | null = null;
  showForm = false;
  form: AccountRequest = { code: '', name: '', type: 'EXPENSE' };
  formError: string | null = null;
  saving = false;

  ngOnInit(): void {
    this.load();
  }

  typeLabel(type: string): string {
    return type.charAt(0) + type.slice(1).toLowerCase();
  }

  openForm(): void {
    this.form = { code: '', name: '', type: 'EXPENSE' };
    this.formError = null;
    this.showForm = true;
  }

  save(): void {
    this.saving = true;
    this.finance.createAccount(this.form).subscribe({
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
    this.finance.accounts().subscribe({ next: (a) => (this.accounts = a), error: (e: unknown) => (this.error = errorMessage(e)) });
  }
}
