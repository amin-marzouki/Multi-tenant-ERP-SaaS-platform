import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import {
  Customer,
  CustomerRequest,
  DocumentSummary,
  Invoice,
  InvoiceRequest,
  OrderRequest,
  Quote,
  QuoteRequest,
  SalesOrder,
} from '../core/models';

@Injectable({ providedIn: 'root' })
export class SalesService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/sales';

  customers(): Observable<Customer[]> {
    return this.http.get<Customer[]>(`${this.base}/customers`);
  }

  createCustomer(request: CustomerRequest): Observable<Customer> {
    return this.http.post<Customer>(`${this.base}/customers`, request);
  }

  updateCustomer(id: number, request: CustomerRequest): Observable<Customer> {
    return this.http.put<Customer>(`${this.base}/customers/${id}`, request);
  }

  quotes(): Observable<DocumentSummary[]> {
    return this.http.get<DocumentSummary[]>(`${this.base}/quotes`);
  }

  quote(id: number): Observable<Quote> {
    return this.http.get<Quote>(`${this.base}/quotes/${id}`);
  }

  createQuote(request: QuoteRequest): Observable<Quote> {
    return this.http.post<Quote>(`${this.base}/quotes`, request);
  }

  convertQuote(id: number): Observable<SalesOrder> {
    return this.http.post<SalesOrder>(`${this.base}/quotes/${id}/convert`, {});
  }

  rejectQuote(id: number): Observable<Quote> {
    return this.http.post<Quote>(`${this.base}/quotes/${id}/reject`, {});
  }

  orders(): Observable<DocumentSummary[]> {
    return this.http.get<DocumentSummary[]>(`${this.base}/orders`);
  }

  order(id: number): Observable<SalesOrder> {
    return this.http.get<SalesOrder>(`${this.base}/orders/${id}`);
  }

  createOrder(request: OrderRequest): Observable<SalesOrder> {
    return this.http.post<SalesOrder>(`${this.base}/orders`, request);
  }

  orderAction(id: number, action: 'confirm' | 'ship' | 'cancel'): Observable<SalesOrder> {
    return this.http.post<SalesOrder>(`${this.base}/orders/${id}/${action}`, {});
  }

  invoiceOrder(id: number, request: InvoiceRequest): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.base}/orders/${id}/invoice`, request);
  }

  invoices(): Observable<DocumentSummary[]> {
    return this.http.get<DocumentSummary[]>(`${this.base}/invoices`);
  }

  invoice(id: number): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.base}/invoices/${id}`);
  }

  postInvoice(id: number): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.base}/invoices/${id}/post`, {});
  }
}
