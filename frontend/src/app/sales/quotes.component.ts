import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';

import { DocumentSummary } from '../core/models';
import { errorMessage } from '../core/util';
import { ErrorAlertComponent } from '../shared/error-alert.component';
import { ModalComponent } from '../shared/modal.component';
import { PageHeaderComponent } from '../shared/page-header.component';
import { DocumentDraft, DocumentFormComponent } from './document-form.component';
import { DocumentListComponent } from './document-list.component';
import { SalesService } from './sales.service';

@Component({
  selector: 'app-quotes',
  standalone: true,
  imports: [PageHeaderComponent, ModalComponent, ErrorAlertComponent, DocumentFormComponent, DocumentListComponent],
  template: `
    <app-page-header title="Quotes" subtitle="Offers to customers. An accepted quote becomes a sales order.">
      <button class="btn btn-primary" (click)="showForm = true; formError = null">New quote</button>
    </app-page-header>
    <app-error-alert [message]="error" />
    <app-document-list [documents]="quotes" basePath="/sales/quotes" dateLabel="Quote date" [loading]="loading" emptyText="No quotes yet." />

    <app-modal [open]="showForm" title="New quote" size="xl" (closed)="showForm = false">
      @if (showForm) {
        <app-document-form
          dateLabel="Quote date"
          [withValidUntil]="true"
          submitLabel="Create quote"
          [saving]="saving"
          [error]="formError"
          (submitted)="create($event)"
          (cancelled)="showForm = false"
        />
      }
    </app-modal>
  `,
})
export class QuotesComponent implements OnInit {
  private readonly sales = inject(SalesService);
  private readonly router = inject(Router);

  quotes: DocumentSummary[] = [];
  loading = true;
  error: string | null = null;
  showForm = false;
  saving = false;
  formError: string | null = null;

  ngOnInit(): void {
    this.sales.quotes().subscribe({
      next: (quotes) => {
        this.quotes = quotes;
        this.loading = false;
      },
      error: (e: unknown) => {
        this.error = errorMessage(e);
        this.loading = false;
      },
    });
  }

  create(draft: DocumentDraft): void {
    this.saving = true;
    this.sales
      .createQuote({ customerId: draft.customerId, quoteDate: draft.date, validUntil: draft.validUntil, notes: draft.notes, lines: draft.lines })
      .subscribe({
        next: (quote) => void this.router.navigate(['/sales/quotes', quote.id]),
        error: (e: unknown) => {
          this.saving = false;
          this.formError = errorMessage(e);
        },
      });
  }
}
