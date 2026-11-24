import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Warehouse, WarehouseRequest } from '../core/models';
import { errorMessage } from '../core/util';
import { ErrorAlertComponent } from '../shared/error-alert.component';
import { ModalComponent } from '../shared/modal.component';
import { PageHeaderComponent } from '../shared/page-header.component';
import { InventoryService } from './inventory.service';

@Component({
  selector: 'app-warehouses',
  standalone: true,
  imports: [FormsModule, PageHeaderComponent, ModalComponent, ErrorAlertComponent],
  template: `
    <app-page-header title="Warehouses" subtitle="Sales orders reserve and ship from the default warehouse.">
      <button class="btn btn-primary" (click)="openForm()">New warehouse</button>
    </app-page-header>
    <app-error-alert [message]="error" />

    <div class="card overflow-hidden">
      <table class="data-table">
        <thead><tr><th>Code</th><th>Name</th><th></th></tr></thead>
        <tbody>
          @for (warehouse of warehouses; track warehouse.id) {
            <tr>
              <td class="font-mono text-xs">{{ warehouse.code }}</td>
              <td class="font-medium">{{ warehouse.name }}</td>
              <td>
                @if (warehouse.defaultWarehouse) {
                  <span class="inline-flex rounded-full bg-indigo-100 px-2 py-0.5 text-xs font-medium text-indigo-700">Default</span>
                }
              </td>
            </tr>
          } @empty {
            <tr><td colspan="3" class="py-10 text-center text-slate-500">No warehouses yet.</td></tr>
          }
        </tbody>
      </table>
    </div>

    <app-modal [open]="showForm" title="New warehouse" (closed)="showForm = false">
      <form class="space-y-4" (ngSubmit)="save()">
        <app-error-alert [message]="formError" />
        <div class="grid grid-cols-3 gap-4">
          <label class="block"><span class="label">Code</span><input class="input" name="code" [(ngModel)]="form.code" required /></label>
          <label class="col-span-2 block"><span class="label">Name</span><input class="input" name="name" [(ngModel)]="form.name" required /></label>
        </div>
        <label class="flex items-center gap-2 text-sm"><input type="checkbox" name="default" [(ngModel)]="form.defaultWarehouse" /> Make this the default warehouse</label>
        <div class="flex justify-end gap-2 pt-2">
          <button type="button" class="btn btn-secondary" (click)="showForm = false">Cancel</button>
          <button class="btn btn-primary" [disabled]="saving">Save</button>
        </div>
      </form>
    </app-modal>
  `,
})
export class WarehousesComponent implements OnInit {
  private readonly inventory = inject(InventoryService);

  warehouses: Warehouse[] = [];
  error: string | null = null;
  showForm = false;
  form: WarehouseRequest = { code: '', name: '', defaultWarehouse: false };
  formError: string | null = null;
  saving = false;

  ngOnInit(): void {
    this.load();
  }

  openForm(): void {
    this.form = { code: '', name: '', defaultWarehouse: false };
    this.formError = null;
    this.showForm = true;
  }

  save(): void {
    this.saving = true;
    this.inventory.createWarehouse(this.form).subscribe({
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
    this.inventory.warehouses().subscribe({ next: (w) => (this.warehouses = w), error: (e: unknown) => (this.error = errorMessage(e)) });
  }
}
