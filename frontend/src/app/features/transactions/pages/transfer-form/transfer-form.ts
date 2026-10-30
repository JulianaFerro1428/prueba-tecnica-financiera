import { CurrencyPipe, formatCurrency } from '@angular/common';
import { Component, LOCALE_ID, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { apiErrorMessage } from '../../../../core/http/api-error';
import { Account, ACCOUNT_TYPE_LABELS } from '../../../accounts/models/account.model';
import { AccountService } from '../../../accounts/services/account.service';
import { ClientService } from '../../../clients/services/client.service';
import { TransactionService } from '../../services/transaction.service';
import { AMOUNT_VALIDATORS, amountErrorMessage } from '../../validators/amount';

/** Una cuenta lista para mostrarse en las listas desplegables. */
interface AccountOption {
  account: Account;
  ownerName: string;
}

/** El backend también lo rechaza; aquí se avisa antes de enviar. */
function differentAccounts(group: AbstractControl): ValidationErrors | null {
  const { sourceAccountId, targetAccountId } = group.value;
  return sourceAccountId && sourceAccountId === targetAccountId ? { sameAccount: true } : null;
}

/** Transferencia entre dos cuentas activas (/transfers). El débito y el crédito los registra el backend juntos. */
@Component({
  selector: 'app-transfer-form',
  imports: [CurrencyPipe, ReactiveFormsModule, RouterLink],
  templateUrl: './transfer-form.html',
  styleUrl: './transfer-form.css',
})
export class TransferForm {
  private readonly accountService = inject(AccountService);
  private readonly clientService = inject(ClientService);
  private readonly transactionService = inject(TransactionService);
  private readonly fb = inject(FormBuilder);
  private readonly locale = inject(LOCALE_ID);

  protected readonly typeLabels = ACCOUNT_TYPE_LABELS;

  protected readonly form = this.fb.group(
    {
      // Las listas desplegables entregan texto; se convierte a número al enviar.
      sourceAccountId: this.fb.nonNullable.control('', Validators.required),
      targetAccountId: this.fb.nonNullable.control('', Validators.required),
      amount: this.fb.control<number | null>(null, AMOUNT_VALIDATORS),
    },
    { validators: differentAccounts },
  );
  /** null mientras carga. Solo cuentas activas: las demás no pueden mover dinero. */
  protected readonly options = signal<AccountOption[] | null>(null);
  protected readonly loadError = signal('');
  protected readonly saving = signal(false);
  protected readonly success = signal<{ text: string; sourceId: number; targetId: number } | null>(null);
  protected readonly serverError = signal('');

  constructor() {
    this.loadAccounts();
  }

  protected showError(name: 'sourceAccountId' | 'targetAccountId'): boolean {
    const control = this.form.controls[name];
    return control.touched && control.invalid;
  }

  protected sameAccountError(): boolean {
    return this.form.hasError('sameAccount') && this.form.controls.targetAccountId.touched;
  }

  protected amountError(): string | null {
    return amountErrorMessage(this.form.controls.amount);
  }

  protected submit(): void {
    this.success.set(null);
    this.serverError.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const request = {
      sourceAccountId: Number(value.sourceAccountId),
      targetAccountId: Number(value.targetAccountId),
      amount: value.amount!,
    };

    this.saving.set(true);
    this.transactionService.transfer(request).subscribe({
      next: () => {
        this.saving.set(false);
        this.success.set({
          text: this.successText(request.sourceAccountId, request.targetAccountId, request.amount),
          sourceId: request.sourceAccountId,
          targetId: request.targetAccountId,
        });
        this.form.reset();
        // Los saldos cambiaron: se vuelven a pedir para que las listas muestren los valores nuevos.
        this.loadAccounts();
      },
      error: (error) => {
        this.saving.set(false);
        this.serverError.set(apiErrorMessage(error));
      },
    });
  }

  private loadAccounts(): void {
    forkJoin({ accounts: this.accountService.getAll(), clients: this.clientService.getAll() }).subscribe({
      next: ({ accounts, clients }) => {
        const names = new Map(clients.map((c) => [c.id, `${c.firstNames} ${c.lastName}`]));
        this.options.set(
          accounts
            .filter((account) => account.status === 'ACTIVE')
            .map((account) => ({ account, ownerName: names.get(account.clientId) ?? `Cliente ${account.clientId}` })),
        );
      },
      error: (error) => this.loadError.set(apiErrorMessage(error)),
    });
  }

  private successText(sourceId: number, targetId: number, amount: number): string {
    const numberOf = (id: number) => this.options()?.find((o) => o.account.id === id)?.account.number ?? id;
    const formatted = formatCurrency(amount, this.locale, '$', 'COP', '1.2-2');
    return `Transferencia de ${formatted} de la cuenta ${numberOf(sourceId)} a la cuenta ${numberOf(targetId)} registrada.`;
  }
}
