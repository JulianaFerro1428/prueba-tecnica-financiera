import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Movement } from '../../accounts/models/movement.model';
import { TransferRequest, TransferResult } from '../models/transfer.model';

/** Única puerta del frontend hacia los endpoints que mueven dinero. */
@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly http = inject(HttpClient);

  deposit(accountId: number, amount: number): Observable<Movement> {
    return this.http.post<Movement>(`/api/accounts/${accountId}/deposits`, { amount });
  }

  withdraw(accountId: number, amount: number): Observable<Movement> {
    return this.http.post<Movement>(`/api/accounts/${accountId}/withdrawals`, { amount });
  }

  transfer(request: TransferRequest): Observable<TransferResult> {
    return this.http.post<TransferResult>('/api/transfers', request);
  }
}
