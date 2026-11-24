import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Item, StockLevel, StockMove, StockMoveType, Warehouse } from '../core/models';
import { errorMessage } from '../core/util';
import { ErrorAlertComponent } from '../shared/error-alert.component';
import { ModalComponent } from '../shared/modal.component';
import { PageHeaderComponent } from '../shared/page-header.component';
import { StatusBadgeComponent } from '../shared/status-badge.component';
import { InventoryService } from './inventory.service';

interface MoveForm {
  type: StockMoveType;
  itemId: number | null;
  warehouseId: number | null;
  quantity: number;
  reference: string;
  note: string;
}

@Component({
  selector: 'app-stock',
  standalone: true,
  imports: [FormsModule, DatePipe, DecimalPipe, PageHeaderComponent, ModalComponent, ErrorAlertComponent, StatusBadgeComponent],
  template: `
    <app-page-header title="Stock" subtitle="Reserved stock is promised to confirmed orders; available = on hand - reserved.">
      <button class="btn btn-primary" (click)="openForm()">Record movement</button>
    </app-page-header>
    <app-error-alert [message]="error" />

    <h2 class="mb-2 text-sm font-semibold uppercase tracking-wide text-slate-500">Levels</h2>
    <div class="card mb-8 overflow-hidden">
      <table class="data-table">
        <thead>
          <tr><th>SKU</th><th>Item</th><th>Warehouse</th><th class="text-right">On hand</th><th class="text-right">Reserved</th><th class="text-right">Available</th></tr>
        </thead>
        <tbody>
          @for (level of levels; track level.id) {
            <tr>
              <td class="font-mono text-xs">{{ level.sku }}</td>
              <td>{{ level.itemName }}</td>
              <td>{{ level.warehouseCode }}</td>
              <td class="text-right tabular-nums">{{ level.onHand | number: '1.0-3' }}</td>
              <td class="text-right tabular-nums text-amber-700">{{ level.reserved | number: '1.0-3' }}</td>
              <td class="text-right font-medium tabular-nums">{{ level.available | number: '1.0-3' }}</td>
            </tr>
          } @empty {
            <tr><td colspan="6" class="py-10 text-center text-slate-500">No stock yet. Record a receipt to add some.</td></tr>
          }
        </tbody>
      </table>
    </div>

    <h2 class="mb-2 text-sm font-semibold uppercase tracking-wide text-slate-500">Recent movements</h2>
    <div class="card overflow-hidden">
      <table class="data-table">
        <thead>
          <tr><th>When</th><th>Type</th><th>Item</th><th>Warehouse</th><th class="text-right">Quantity</th><th>Reference</th><th>By</th></tr>
        </thead>
        <tbody>
          @for (move of moves; track move.id) {
            <tr>
              <td class="whitespace-nowrap text-slate-500">{{ move.movedAt | date: 'short' }}</td>
              <td><app-status-badge [status]="move.type" /></td>
              <td><span class="font-mono text-xs">{{ move.sku }}</span> {{ move.itemName }}</td>
              <td>{{ move.warehouseCode }}</td>
              <td class="text-right tabular-nums" [class.text-rose-600]="move.quantity < 0">{{ move.quantity | number: '1.0-3' }}</td>
              <td>{{ move.reference }}</td>
              <td class="text-slate-500">{{ move.createdBy }}</td>
            </tr>
          } @empty {
            <tr><td colspan="7" class="py-10 text-center text-slate-500">No movements yet.</td></tr>
          }
        </tbody>
      </table>
    </div>

    <app-modal [open]="showForm" title="Record stock movement" (closed)="showForm = false">
      <form class="space-y-4" (ngSubmit)="save()">
        <app-error-alert [message]="formError" />
        <div class="grid grid-cols-2 gap-4">
          <label class="block">
            <span class="label">Type</span>
            <select class="input" name="type" [(ngModel)]="form.type">
              <option value="RECEIPT">Receipt (goods in)</option>
              <option value="ADJUSTMENT">Adjustment (count correction)</option>
            </select>
          </label>
          <label class="block">
            <span class="label">Warehouse</span>
            <select class="input" name="warehouse" [(ngModel)]="form.warehouseId">
              @for (warehouse of warehouses; track warehouse.id) {
                <option [ngValue]="warehouse.id">{{ warehouse.code }} · {{ warehouse.name }}</option>
              }
            </select>
          </label>
        </div>
        <label class="block">
          <span class="label">Item</span>
          <select class="input" name="item" [(ngModel)]="form.itemId" required>
            <option [ngValue]="null" disabled>Choose an item</option>
            @for (item of items; track item.id) {
              <option [ngValue]="item.id">{{ item.sku }} · {{ item.name }}</option>
            }
          </select>
        </label>
        <div class="grid grid-cols-2 gap-4">
          <label class="block">
            <span class="label">Quantity</span>
            <input class="input" type="number" step="any" name="quantity" [(ngModel)]="form.quantity" required />
            @if (form.type === 'ADJUSTMENT') {
              <span class="mt-1 block text-xs text-slate-500">Negative to remove stock.</span>
            }
          </label>
          <label class="block"><span class="label">Reference</span><input class="input" name="reference" [(ngModel)]="form.reference" placeholder="e.g. PO-1042" /></label>
        </div>
        <label class="block"><span class="label">Note</span><input class="input" name="note" [(ngModel)]="form.note" /></label>
        <div class="flex justify-end gap-2 pt-2">
          <button type="button" class="btn btn-secondary" (click)="showForm = false">Cancel</button>
          <button class="btn btn-primary" [disabled]="saving">Record</button>
        </div>
      </form>
    </app-modal>
  `,
})
export class StockComponent implements OnInit {
  private readonly inventory = inject(InventoryService);

  levels: StockLevel[] = [];
  moves: StockMove[] = [];
  items: Item[] = [];
  warehouses: Warehouse[] = [];
  error: string | null = null;
  showForm = false;
  form: MoveForm = this.blankForm();
  formError: string | null = null;
  saving = false;

  ngOnInit(): void {
    this.load();
    this.inventory.items().subscribe((items) => (this.items = items));
    this.inventory.warehouses().subscribe((warehouses) => (this.warehouses = warehouses));
  }

  openForm(): void {
    this.form = this.blankForm();
    this.formError = null;
    this.showForm = true;
  }

  save(): void {
    if (this.form.itemId === null) {
      this.formError = 'Choose an item.';
      return;
    }
    this.saving = true;
    this.inventory
      .recordMove({
        type: this.form.type,
        itemId: this.form.itemId,
        warehouseId: this.form.warehouseId,
        quantity: this.form.quantity,
        reference: this.form.reference,
        note: this.form.note,
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
    this.inventory.stockLevels().subscribe({ next: (l) => (this.levels = l), error: (e: unknown) => (this.error = errorMessage(e)) });
    this.inventory.stockMoves().subscribe({ next: (m) => (this.moves = m), error: (e: unknown) => (this.error = errorMessage(e)) });
  }

  private blankForm(): MoveForm {
    const defaultWarehouse = this.warehouses.find((w) => w.defaultWarehouse) ?? null;
    return { type: 'RECEIPT', itemId: null, warehouseId: defaultWarehouse?.id ?? null, quantity: 1, reference: '', note: '' };
  }
}
