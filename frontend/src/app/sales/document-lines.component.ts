import { DecimalPipe } from '@angular/common';
import { Component, Input } from '@angular/core';

import { SalesDocument } from '../core/models';

/** The lines and totals of a quote, order or invoice. */
@Component({
  selector: 'app-document-lines',
  standalone: true,
  imports: [DecimalPipe],
  template: `
    <div class="card overflow-hidden">
      <table class="data-table">
        <thead>
          <tr>
            <th class="w-12">#</th>
            <th>Description</th>
            <th class="text-right">Qty</th>
            <th class="text-right">Unit price</th>
            <th class="text-right">Tax %</th>
            <th class="text-right">Net</th>
          </tr>
        </thead>
        <tbody>
          @for (line of doc.lines; track line.id) {
            <tr>
              <td class="text-slate-400">{{ line.lineNo }}</td>
              <td>{{ line.description }}</td>
              <td class="text-right tabular-nums">{{ line.quantity | number: '1.0-3' }}</td>
              <td class="text-right tabular-nums">{{ line.unitPrice | number: '1.2-2' }}</td>
              <td class="text-right tabular-nums">{{ line.taxRate | number: '1.0-2' }}</td>
              <td class="text-right tabular-nums">{{ line.netAmount | number: '1.2-2' }}</td>
            </tr>
          }
        </tbody>
        <tfoot>
          <tr>
            <td colspan="5" class="text-right text-slate-500">Subtotal</td>
            <td class="text-right tabular-nums">{{ doc.subtotal | number: '1.2-2' }}</td>
          </tr>
          <tr>
            <td colspan="5" class="text-right text-slate-500">Tax</td>
            <td class="text-right tabular-nums">{{ doc.taxTotal | number: '1.2-2' }}</td>
          </tr>
          <tr>
            <td colspan="5" class="text-right font-semibold">Total</td>
            <td class="text-right font-semibold tabular-nums">{{ doc.total | number: '1.2-2' }}</td>
          </tr>
        </tfoot>
      </table>
    </div>
    @if (doc.notes) {
      <p class="mt-4 whitespace-pre-line text-sm text-slate-600"><span class="font-medium">Notes:</span> {{ doc.notes }}</p>
    }
  `,
})
export class DocumentLinesComponent {
  @Input({ required: true }) doc!: SalesDocument;
}
