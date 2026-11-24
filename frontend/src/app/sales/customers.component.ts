import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../core/auth.service';
import { Customer, CustomerRequest } from '../core/models';
import { SALES } from '../core/roles';
import { errorMessage } from '../core/util';
import { ErrorAlertComponent } from '../shared/error-alert.component';
import { ModalComponent } from '../shared/modal.component';
import { PageHeaderComponent } from '../shared/page-header.component';
import { SalesService } from './sales.service';

@Component({
  selector: 'app-customers',
  standalone: true,
  imports: [FormsModule, PageHeaderComponent, ModalComponent, ErrorAlertComponent],
  template: `
    <app-page-header title="Customers" subtitle="The companies and people you sell to.">
      @if (canEdit) {
        <button class="btn btn-primary" (click)="openForm(null)">New customer</button>
      }
    </app-page-header>
    <app-error-alert [message]="error" />

    <div class="card overflow-hidden">
      <table class="data-table">
        <thead>
          <tr><th>Code</th><th>Name</th><th>Email</th><th>Phone</th><th>Address</th><th></th></tr>
        </thead>
        <tbody>
          @for (customer of customers; track customer.id) {
            <tr>
              <td class="font-mono text-xs text-slate-500">{{ customer.code }}</td>
              <td class="font-medium">{{ customer.name }}</td>
              <td>{{ customer.email }}</td>
              <td>{{ customer.phone }}</td>
              <td class="text-slate-500">{{ customer.address }}</td>
              <td class="text-right">
                @if (canEdit) {
                  <button class="btn-link" (click)="openForm(customer)">Edit</button>
                }
              </td>
            </tr>
          } @empty {
            <tr><td colspan="6" class="py-10 text-center text-slate-500">No customers yet.</td></tr>
          }
        </tbody>
      </table>
    </div>

    <app-modal [open]="showForm" [title]="editing ? 'Edit ' + editing.name : 'New customer'" (closed)="showForm = false">
      <form class="space-y-4" (ngSubmit)="save()">
        <app-error-alert [message]="formError" />
        <label class="block"><span class="label">Name</span><input class="input" name="name" [(ngModel)]="form.name" required /></label>
        <div class="grid grid-cols-2 gap-4">
          <label class="block"><span class="label">Email</span><input class="input" type="email" name="email" [(ngModel)]="form.email" /></label>
          <label class="block"><span class="label">Phone</span><input class="input" name="phone" [(ngModel)]="form.phone" /></label>
        </div>
        <label class="block"><span class="label">Address</span><textarea class="input" rows="2" name="address" [(ngModel)]="form.address"></textarea></label>
        <div class="flex justify-end gap-2 pt-2">
          <button type="button" class="btn btn-secondary" (click)="showForm = false">Cancel</button>
          <button class="btn btn-primary" [disabled]="saving">Save</button>
        </div>
      </form>
    </app-modal>
  `,
})
export class CustomersComponent implements OnInit {
  private readonly sales = inject(SalesService);
  readonly canEdit = inject(AuthService).hasAnyRole(SALES);

  customers: Customer[] = [];
  error: string | null = null;
  showForm = false;
  editing: Customer | null = null;
  form: CustomerRequest = { name: '', email: '', phone: '', address: '' };
  formError: string | null = null;
  saving = false;

  ngOnInit(): void {
    this.load();
  }

  openForm(customer: Customer | null): void {
    this.editing = customer;
    this.form = {
      name: customer?.name ?? '',
      email: customer?.email ?? '',
      phone: customer?.phone ?? '',
      address: customer?.address ?? '',
    };
    this.formError = null;
    this.showForm = true;
  }

  save(): void {
    this.saving = true;
    const request = this.editing
      ? this.sales.updateCustomer(this.editing.id, this.form)
      : this.sales.createCustomer(this.form);
    request.subscribe({
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
    this.sales.customers().subscribe({
      next: (customers) => (this.customers = customers),
      error: (e: unknown) => (this.error = errorMessage(e)),
    });
  }
}
