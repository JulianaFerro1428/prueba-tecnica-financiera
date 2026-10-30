import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Movement } from '../../accounts/models/movement.model';

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
}
