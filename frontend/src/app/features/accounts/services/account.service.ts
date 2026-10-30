import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Account, OpenAccountRequest } from '../models/account.model';
import { Statement } from '../models/movement.model';

/** Única puerta del frontend hacia /api/accounts. */
@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/accounts';

  getAll(): Observable<Account[]> {
    return this.http.get<Account[]>(this.baseUrl);
  }

  getStatement(id: number): Observable<Statement> {
    return this.http.get<Statement>(`${this.baseUrl}/${id}/statement`);
  }

  open(request: OpenAccountRequest): Observable<Account> {
    return this.http.post<Account>(this.baseUrl, request);
  }

  activate(id: number): Observable<Account> {
    return this.changeStatus(id, 'activate');
  }

  deactivate(id: number): Observable<Account> {
    return this.changeStatus(id, 'deactivate');
  }

  cancel(id: number): Observable<Account> {
    return this.changeStatus(id, 'cancel');
  }

  private changeStatus(id: number, action: 'activate' | 'deactivate' | 'cancel'): Observable<Account> {
    return this.http.patch<Account>(`${this.baseUrl}/${id}/${action}`, null);
  }
}
