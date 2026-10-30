import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Observable, forkJoin } from 'rxjs';

import { apiErrorMessage } from '../../../../core/http/api-error';
import { readNavigationNotice } from '../../../../core/navigation/navigation-notice';
import { ClientService } from '../../../clients/services/client.service';
import { ACCOUNT_STATUS_LABELS, ACCOUNT_TYPE_LABELS, Account } from '../../models/account.model';
import { AccountService } from '../../services/account.service';

type LoadState = 'loading' | 'ready' | 'error';

interface Notice {
  kind: 'success' | 'error';
  text: string;
}

/** Pantalla con todas las cuentas: muestra su dueño y permite activarlas, inactivarlas o cancelarlas. */
@Component({
  selector: 'app-account-list',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './account-list.html',
  styleUrl: './account-list.css',
})
export class AccountList {
  private readonly accountService = inject(AccountService);
  private readonly clientService = inject(ClientService);

  protected readonly typeLabels = ACCOUNT_TYPE_LABELS;
  protected readonly statusLabels = ACCOUNT_STATUS_LABELS;

  protected readonly accounts = signal<Account[]>([]);
  /** Nombre del dueño por id de cliente, para no mostrar solo un número. */
  protected readonly ownerNames = signal<Partial<Record<number, string>>>({});
  protected readonly loadState = signal<LoadState>('loading');
  protected readonly loadError = signal('');
  protected readonly notice = signal<Notice | null>(null);
  protected readonly confirmingCancelId = signal<number | null>(null);

  constructor() {
    const savedNotice = readNavigationNotice();
    if (savedNotice) {
      this.notice.set({ kind: 'success', text: savedNotice });
    }
    this.load();
  }

  protected load(): void {
    this.loadState.set('loading');
    forkJoin({ accounts: this.accountService.getAll(), clients: this.clientService.getAll() }).subscribe({
      next: ({ accounts, clients }) => {
        this.accounts.set(accounts);
        this.ownerNames.set(Object.fromEntries(clients.map((c) => [c.id, `${c.firstNames} ${c.lastName}`])));
        this.loadState.set('ready');
      },
      error: (error) => {
        this.loadError.set(apiErrorMessage(error));
        this.loadState.set('error');
      },
    });
  }

  protected activate(account: Account): void {
    this.applyChange(this.accountService.activate(account.id), 'activada');
  }

  protected deactivate(account: Account): void {
    this.applyChange(this.accountService.deactivate(account.id), 'inactivada');
  }

  protected askToCancel(account: Account): void {
    this.notice.set(null);
    this.confirmingCancelId.set(account.id);
  }

  protected keepAccount(): void {
    this.confirmingCancelId.set(null);
  }

  protected cancel(account: Account): void {
    this.applyChange(this.accountService.cancel(account.id), 'cancelada');
  }

  /** Las tres acciones siguen el mismo camino: llamar al backend y reemplazar la fila con la cuenta que devuelve. */
  private applyChange(request: Observable<Account>, verb: string): void {
    this.notice.set(null);
    request.subscribe({
      next: (updated) => {
        this.accounts.update((list) => list.map((a) => (a.id === updated.id ? updated : a)));
        this.confirmingCancelId.set(null);
        this.notice.set({ kind: 'success', text: `Cuenta ${updated.number} ${verb}.` });
      },
      error: (error) => {
        // Ej.: el backend responde 422 si se intenta cancelar una cuenta con saldo.
        this.confirmingCancelId.set(null);
        this.notice.set({ kind: 'error', text: apiErrorMessage(error) });
      },
    });
  }
}
