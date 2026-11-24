import { Component, OnInit, inject } from '@angular/core';

import { DocumentSummary } from '../core/models';
import { errorMessage } from '../core/util';
import { ErrorAlertComponent } from '../shared/error-alert.component';
import { PageHeaderComponent } from '../shared/page-header.component';
import { DocumentListComponent } from './document-list.component';
import { SalesService } from './sales.service';

@Component({
  selector: 'app-invoices',
  standalone: true,
  imports: [PageHeaderComponent, ErrorAlertComponent, DocumentListComponent],
  template: `
    <app-page-header title="Invoices" subtitle="Created from shipped orders. Posting an invoice books it in finance." />
    <app-error-alert [message]="error" />
    <app-document-list [documents]="invoices" basePath="/sales/invoices" dateLabel="Issue date" [loading]="loading" emptyText="No invoices yet. Invoice a shipped order to create one." />
  `,
})
export class InvoicesComponent implements OnInit {
  private readonly sales = inject(SalesService);

  invoices: DocumentSummary[] = [];
  loading = true;
  error: string | null = null;

  ngOnInit(): void {
    this.sales.invoices().subscribe({
      next: (invoices) => {
        this.invoices = invoices;
        this.loading = false;
      },
      error: (e: unknown) => {
        this.error = errorMessage(e);
        this.loading = false;
      },
    });
  }
}
