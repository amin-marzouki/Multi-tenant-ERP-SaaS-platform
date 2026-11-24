import { DecimalPipe } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Customer, Item, LineRequest } from '../core/models';
import { errorMessage, today } from '../core/util';
import { InventoryService } from '../inventory/inventory.service';
import { ErrorAlertComponent } from '../shared/error-alert.component';
import { SalesService } from './sales.service';

interface LineDraft {
  itemId: number | null;
  description: string;
  quantity: number;
  unitPrice: number;
  taxRate: number;
}

/** What the quote and order forms produce. */
export interface DocumentDraft {
  customerId: number;
  date: string | null;
  validUntil: string | null;
  notes: string;
  lines: LineRequest[];
}

const DEFAULT_TAX_RATE = 19;

/** Customer, date and line items: the form shared by new quotes and new orders. */
@Component({
  selector: 'app-document-form',
  standalone: true,
  imports: [FormsModule, DecimalPipe, ErrorAlertComponent],
  template: `
    <form class="space-y-5" (ngSubmit)="submit()">
      <app-error-alert [message]="error" />
      <div class="grid gap-4 sm:grid-cols-3">
        <label class="block sm:col-span-1">
          <span class="label">Customer</span>
          <select class="input" name="customer" [(ngModel)]="customerId" required>
            <option [ngValue]="null" disabled>Choose a customer</option>
            @for (customer of customers; track customer.id) {
              <option [ngValue]="customer.id">{{ customer.name }}</option>
            }
          </select>
        </label>
        <label class="block">
          <span class="label">{{ dateLabel }}</span>
          <input class="input" type="date" name="date" [(ngModel)]="date" />
        </label>
        @if (withValidUntil) {
          <label class="block">
            <span class="label">Valid until</span>
            <input class="input" type="date" name="validUntil" [(ngModel)]="validUntil" />
          </label>
        }
      </div>

      <div class="overflow-x-auto rounded-lg border border-slate-200">
        <table class="data-table">
          <thead>
            <tr>
              <th class="w-56">Item</th>
              <th>Description</th>
              <th class="w-24">Qty</th>
              <th class="w-32">Unit price</th>
              <th class="w-24">Tax %</th>
              <th class="w-28 text-right">Net</th>
              <th class="w-16"></th>
            </tr>
          </thead>
          <tbody>
            @for (line of lines; let i = $index; track i) {
              <tr>
                <td>
                  <select class="input" [name]="'item' + i" [(ngModel)]="line.itemId" (ngModelChange)="onItemChosen(line)">
                    <option [ngValue]="null" disabled>Choose…</option>
                    @for (item of items; track item.id) {
                      <option [ngValue]="item.id">{{ item.sku }} · {{ item.name }} ({{ item.available | number: '1.0-3' }} free)</option>
                    }
                  </select>
                </td>
                <td><input class="input" [name]="'description' + i" [(ngModel)]="line.description" /></td>
                <td><input class="input" type="number" min="0" step="any" [name]="'quantity' + i" [(ngModel)]="line.quantity" /></td>
                <td><input class="input" type="number" min="0" step="0.01" [name]="'price' + i" [(ngModel)]="line.unitPrice" /></td>
                <td><input class="input" type="number" min="0" max="100" step="0.01" [name]="'tax' + i" [(ngModel)]="line.taxRate" /></td>
                <td class="text-right tabular-nums">{{ (line.quantity || 0) * (line.unitPrice || 0) | number: '1.2-2' }}</td>
                <td class="text-right">
                  <button type="button" class="btn-link text-rose-600" [disabled]="lines.length === 1" (click)="removeLine(i)">Remove</button>
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
      <div class="flex items-center justify-between">
        <button type="button" class="btn btn-secondary" (click)="addLine()">Add line</button>
        <div class="text-sm text-slate-600">
          Net <span class="font-medium tabular-nums">{{ netTotal | number: '1.2-2' }}</span>
          · Tax <span class="font-medium tabular-nums">{{ taxTotal | number: '1.2-2' }}</span>
          · Total <span class="font-semibold tabular-nums text-slate-900">{{ netTotal + taxTotal | number: '1.2-2' }}</span>
        </div>
      </div>

      <label class="block">
        <span class="label">Notes</span>
        <textarea class="input" rows="2" name="notes" [(ngModel)]="notes"></textarea>
      </label>

      <div class="flex justify-end gap-2 border-t border-slate-100 pt-4">
        <button type="button" class="btn btn-secondary" (click)="cancelled.emit()">Cancel</button>
        <button class="btn btn-primary" [disabled]="saving">{{ saving ? 'Saving…' : submitLabel }}</button>
      </div>
    </form>
  `,
})
export class DocumentFormComponent implements OnInit {
  private readonly sales = inject(SalesService);
  private readonly inventory = inject(InventoryService);

  @Input() dateLabel = 'Date';
  @Input() withValidUntil = false;
  @Input() submitLabel = 'Save';
  @Input() saving = false;
  /** Set by the parent when saving failed. */
  @Input() error: string | null = null;
  @Output() readonly submitted = new EventEmitter<DocumentDraft>();
  @Output() readonly cancelled = new EventEmitter<void>();

  customers: Customer[] = [];
  items: Item[] = [];
  customerId: number | null = null;
  date = today();
  validUntil = '';
  notes = '';
  lines: LineDraft[] = [this.emptyLine()];

  ngOnInit(): void {
    this.sales.customers().subscribe({ next: (c) => (this.customers = c), error: (e: unknown) => (this.error = errorMessage(e)) });
    this.inventory.items().subscribe({
      next: (items) => (this.items = items.filter((i) => i.active)),
      error: (e: unknown) => (this.error = errorMessage(e)),
    });
  }

  get netTotal(): number {
    return this.lines.reduce((sum, l) => sum + (l.quantity || 0) * (l.unitPrice || 0), 0);
  }

  get taxTotal(): number {
    return this.lines.reduce((sum, l) => sum + ((l.quantity || 0) * (l.unitPrice || 0) * (l.taxRate || 0)) / 100, 0);
  }

  onItemChosen(line: LineDraft): void {
    const item = this.items.find((i) => i.id === line.itemId);
    if (item) {
      line.description = item.name;
      line.unitPrice = item.salePrice;
    }
  }

  addLine(): void {
    this.lines.push(this.emptyLine());
  }

  removeLine(index: number): void {
    this.lines.splice(index, 1);
  }

  submit(): void {
    if (this.customerId === null) {
      this.error = 'Choose a customer.';
      return;
    }
    if (this.lines.some((l) => l.itemId === null || !l.description.trim() || !(l.quantity > 0))) {
      this.error = 'Every line needs an item, a description and a quantity above zero.';
      return;
    }
    this.error = null;
    this.submitted.emit({
      customerId: this.customerId,
      date: this.date || null,
      validUntil: this.withValidUntil && this.validUntil ? this.validUntil : null,
      notes: this.notes,
      lines: this.lines.map((l) => ({
        itemId: l.itemId as number,
        description: l.description.trim(),
        quantity: l.quantity,
        unitPrice: l.unitPrice || 0,
        taxRate: l.taxRate || 0,
      })),
    });
  }

  private emptyLine(): LineDraft {
    return { itemId: null, description: '', quantity: 1, unitPrice: 0, taxRate: DEFAULT_TAX_RATE };
  }
}
