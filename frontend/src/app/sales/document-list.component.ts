import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { DocumentSummary } from '../core/models';
import { StatusBadgeComponent } from '../shared/status-badge.component';

/** A table of quotes, orders or invoices; each row opens the document. */
@Component({
  selector: 'app-document-list',
  standalone: true,
  imports: [RouterLink, DatePipe, DecimalPipe, StatusBadgeComponent],
  template: `
    <div class="card overflow-hidden">
      <table class="data-table">
        <thead>
          <tr>
            <th>Number</th>
            <th>Customer</th>
            <th>{{ dateLabel }}</th>
            <th>Status</th>
            <th class="text-right">Total</th>
          </tr>
        </thead>
        <tbody>
          @for (doc of documents; track doc.id) {
            <tr class="hover:bg-slate-50">
              <td class="whitespace-nowrap"><a [routerLink]="[basePath, doc.id]" class="font-medium text-indigo-600 hover:text-indigo-500">{{ doc.number }}</a></td>
              <td>{{ doc.customerName }}</td>
              <td>{{ doc.date | date: 'mediumDate' }}</td>
              <td><app-status-badge [status]="doc.status" /></td>
              <td class="text-right tabular-nums">{{ doc.total | number: '1.2-2' }}</td>
            </tr>
          } @empty {
            <tr>
              <td colspan="5" class="py-10 text-center text-slate-500">{{ loading ? 'Loading…' : emptyText }}</td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `,
})
export class DocumentListComponent {
  @Input({ required: true }) documents: DocumentSummary[] = [];
  @Input({ required: true }) basePath = '';
  @Input() dateLabel = 'Date';
  @Input() emptyText = 'Nothing here yet.';
  @Input() loading = false;
}
