import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { apiErrorMessage } from '../../../../core/http/api-error';
import { ClientService } from '../../../clients/services/client.service';
import { CashMovementForm } from '../../../transactions/components/cash-movement-form/cash-movement-form';
import { ACCOUNT_STATUS_LABELS, ACCOUNT_TYPE_LABELS, Account } from '../../models/account.model';
import { Movement } from '../../models/movement.model';
import { AccountService } from '../../services/account.service';

type LoadState = 'loading' | 'ready' | 'error';

/** Estado de cuenta (/accounts/:id): saldo actual y movimientos, del más reciente al más antiguo. */
@Component({
  selector: 'app-account-statement',
  imports: [CashMovementForm, CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './account-statement.html',
  styleUrl: './account-statement.css',
})
export class AccountStatement {
  private readonly accountService = inject(AccountService);
  private readonly clientService = inject(ClientService);
  private readonly accountId = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));

  protected readonly typeLabels = ACCOUNT_TYPE_LABELS;
  protected readonly statusLabels = ACCOUNT_STATUS_LABELS;

  protected readonly account = signal<Account | null>(null);
  protected readonly movements = signal<Movement[]>([]);
  protected readonly ownerName = signal('');
  /** Número de cuenta por id, para mostrar a dónde fue o de dónde vino una transferencia. */
  private readonly accountNumbers = signal<Partial<Record<number, string>>>({});
  protected readonly loadState = signal<LoadState>('loading');
  protected readonly loadError = signal('');

  constructor() {
    this.load();
  }

  protected load(): void {
    // Al recargar después de un movimiento no se oculta la pantalla: solo se actualizan los datos.
    if (!this.account()) {
      this.loadState.set('loading');
    }
    forkJoin({
      statement: this.accountService.getStatement(this.accountId),
      accounts: this.accountService.getAll(),
      clients: this.clientService.getAll(),
    }).subscribe({
      next: ({ statement, accounts, clients }) => {
        const owner = clients.find((c) => c.id === statement.account.clientId);
        this.account.set(statement.account);
        this.movements.set(statement.movements);
        this.ownerName.set(owner ? `${owner.firstNames} ${owner.lastName}` : `Cliente ${statement.account.clientId}`);
        this.accountNumbers.set(Object.fromEntries(accounts.map((a) => [a.id, a.number])));
        this.loadState.set('ready');
      },
      error: (error) => {
        this.loadError.set(apiErrorMessage(error));
        this.loadState.set('error');
      },
    });
  }

  protected describe(movement: Movement): string {
    switch (movement.transactionType) {
      case 'DEPOSIT':
        return 'Consignación';
      case 'WITHDRAWAL':
        return 'Retiro';
      case 'TRANSFER':
        return movement.movementType === 'DEBIT' ? 'Transferencia enviada' : 'Transferencia recibida';
    }
  }

  /** "A la cuenta 3300000002" o "Desde la cuenta ...". Solo las transferencias tienen otra cuenta. */
  protected counterparty(movement: Movement): string | null {
    if (movement.counterpartyAccountId === null) {
      return null;
    }
    const number = this.accountNumbers()[movement.counterpartyAccountId] ?? `id ${movement.counterpartyAccountId}`;
    return `${movement.movementType === 'DEBIT' ? 'A' : 'Desde'} la cuenta ${number}`;
  }
}
