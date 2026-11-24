import { DecimalPipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../core/auth.service';
import { Item, ItemRequest } from '../core/models';
import { WAREHOUSE } from '../core/roles';
import { errorMessage } from '../core/util';
import { ErrorAlertComponent } from '../shared/error-alert.component';
import { ModalComponent } from '../shared/modal.component';
import { PageHeaderComponent } from '../shared/page-header.component';
import { InventoryService } from './inventory.service';

@Component({
  selector: 'app-items',
  standalone: true,
  imports: [FormsModule, DecimalPipe, PageHeaderComponent, ModalComponent, ErrorAlertComponent],
  template: `
    <app-page-header title="Items" subtitle="What you sell. Stock is summed over all warehouses.">
      @if (canEdit) {
        <button class="btn btn-primary" (click)="openForm(null)">New item</button>
      }
    </app-page-header>
    <app-error-alert [message]="error" />

    <div class="card overflow-hidden">
      <table class="data-table">
        <thead>
          <tr>
            <th>SKU</th><th>Name</th><th>Unit</th>
            <th class="text-right">Price</th><th class="text-right">On hand</th><th class="text-right">Available</th>
            <th>Status</th><th></th>
          </tr>
        </thead>
        <tbody>
          @for (item of items; track item.id) {
            <tr>
              <td class="font-mono text-xs">{{ item.sku }}</td>
              <td class="font-medium">{{ item.name }}</td>
              <td class="text-slate-500">{{ item.unit }}</td>
              <td class="text-right tabular-nums">{{ item.salePrice | number: '1.2-2' }}</td>
              <td class="text-right tabular-nums">{{ item.onHand | number: '1.0-3' }}</td>
              <td class="text-right tabular-nums" [class.text-rose-600]="item.available <= 0">{{ item.available | number: '1.0-3' }}</td>
              <td>{{ item.active ? 'Active' : 'Inactive' }}</td>
              <td class="text-right">
                @if (canEdit) {
                  <button class="btn-link" (click)="openForm(item)">Edit</button>
                }
              </td>
            </tr>
          } @empty {
            <tr><td colspan="8" class="py-10 text-center text-slate-500">No items yet.</td></tr>
          }
        </tbody>
      </table>
    </div>

    <app-modal [open]="showForm" [title]="editing ? 'Edit ' + editing.sku : 'New item'" (closed)="showForm = false">
      <form class="space-y-4" (ngSubmit)="save()">
        <app-error-alert [message]="formError" />
        <div class="grid grid-cols-3 gap-4">
          <label class="block"><span class="label">SKU</span><input class="input" name="sku" [(ngModel)]="form.sku" required /></label>
          <label class="col-span-2 block"><span class="label">Name</span><input class="input" name="name" [(ngModel)]="form.name" required /></label>
        </div>
        <label class="block"><span class="label">Description</span><textarea class="input" rows="2" name="description" [(ngModel)]="form.description"></textarea></label>
        <div class="grid grid-cols-3 gap-4">
          <label class="block"><span class="label">Unit</span><input class="input" name="unit" [(ngModel)]="form.unit" required /></label>
          <label class="block"><span class="label">Sale price</span><input class="input" type="number" min="0" step="0.01" name="salePrice" [(ngModel)]="form.salePrice" required /></label>
          <label class="flex items-end gap-2 pb-2 text-sm"><input type="checkbox" name="active" [(ngModel)]="form.active" /> Active</label>
        </div>
        <div class="flex justify-end gap-2 pt-2">
          <button type="button" class="btn btn-secondary" (click)="showForm = false">Cancel</button>
          <button class="btn btn-primary" [disabled]="saving">Save</button>
        </div>
      </form>
    </app-modal>
  `,
})
export class ItemsComponent implements OnInit {
  private readonly inventory = inject(InventoryService);
  readonly canEdit = inject(AuthService).hasAnyRole(WAREHOUSE);

  items: Item[] = [];
  error: string | null = null;
  showForm = false;
  editing: Item | null = null;
  form: ItemRequest = this.blankForm();
  formError: string | null = null;
  saving = false;

  ngOnInit(): void {
    this.load();
  }

  openForm(item: Item | null): void {
    this.editing = item;
    this.form = item
      ? { sku: item.sku, name: item.name, description: item.description ?? '', unit: item.unit, salePrice: item.salePrice, active: item.active }
      : this.blankForm();
    this.formError = null;
    this.showForm = true;
  }

  save(): void {
    this.saving = true;
    const request = this.editing ? this.inventory.updateItem(this.editing.id, this.form) : this.inventory.createItem(this.form);
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
    this.inventory.items().subscribe({ next: (items) => (this.items = items), error: (e: unknown) => (this.error = errorMessage(e)) });
  }

  private blankForm(): ItemRequest {
    return { sku: '', name: '', description: '', unit: 'pcs', salePrice: 0, active: true };
  }
}
