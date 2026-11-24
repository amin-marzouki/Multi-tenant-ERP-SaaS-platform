import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import {
  Account,
  AccountRequest,
  AgingReport,
  JournalEntry,
  JournalEntryRequest,
  JournalEntrySummary,
  Payment,
  PaymentRequest,
  Receivable,
  ReceivableStatus,
  TrialBalance,
} from '../core/models';

@Injectable({ providedIn: 'root' })
export class FinanceService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/finance';

  accounts(): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.base}/accounts`);
  }

  createAccount(request: AccountRequest): Observable<Account> {
    return this.http.post<Account>(`${this.base}/accounts`, request);
  }

  journalEntries(): Observable<JournalEntrySummary[]> {
    return this.http.get<JournalEntrySummary[]>(`${this.base}/journal-entries`);
  }

  journalEntry(id: number): Observable<JournalEntry> {
    return this.http.get<JournalEntry>(`${this.base}/journal-entries/${id}`);
  }

  createJournalEntry(request: JournalEntryRequest): Observable<JournalEntry> {
    return this.http.post<JournalEntry>(`${this.base}/journal-entries`, request);
  }

  receivables(status?: ReceivableStatus): Observable<Receivable[]> {
    const params = status ? new HttpParams().set('status', status) : undefined;
    return this.http.get<Receivable[]>(`${this.base}/receivables`, { params });
  }

  aging(): Observable<AgingReport> {
    return this.http.get<AgingReport>(`${this.base}/receivables/aging`);
  }

  payments(): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.base}/payments`);
  }

  recordPayment(request: PaymentRequest): Observable<Payment> {
    return this.http.post<Payment>(`${this.base}/payments`, request);
  }

  trialBalance(): Observable<TrialBalance> {
    return this.http.get<TrialBalance>(`${this.base}/trial-balance`);
  }
}
