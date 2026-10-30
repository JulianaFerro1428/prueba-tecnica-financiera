import { formatCurrency } from '@angular/common';
import { Component, LOCALE_ID, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Observable } from 'rxjs';

import { apiErrorMessage } from '../../../../core/http/api-error';
import { Movement } from '../../../accounts/models/movement.model';
import { TransactionService } from '../../services/transaction.service';
import { AMOUNT_VALIDATORS, amountErrorMessage } from '../../validators/amount';

type CashKind = 'DEPOSIT' | 'WITHDRAWAL';

/**
 * Formulario para consignar o retirar en una cuenta. No sabe nada del estado de cuenta:
 * cuando el backend confirma, avisa con `completed` y la pantalla que lo contiene decide qué hacer.
 */
@Component({
  selector: 'app-cash-movement-form',
  imports: [ReactiveFormsModule],
  templateUrl: './cash-movement-form.html',
  styleUrl: './cash-movement-form.css',
})
export class CashMovementForm {
  private readonly transactionService = inject(TransactionService);
  private readonly fb = inject(FormBuilder);
  private readonly locale = inject(LOCALE_ID);

  readonly accountId = input.required<number>();
  readonly completed = output<Movement>();

  protected readonly form = this.fb.group({
    kind: this.fb.nonNullable.control<CashKind>('DEPOSIT'),
    amount: this.fb.control<number | null>(null, AMOUNT_VALIDATORS),
  });
  protected readonly saving = signal(false);
  protected readonly success = signal('');
  protected readonly serverError = signal('');

  protected amountError(): string | null {
    return amountErrorMessage(this.form.controls.amount);
  }

  protected submit(): void {
    // Cada intento empieza limpio: no se mezcla el aviso anterior con el resultado nuevo.
    this.success.set('');
    this.serverError.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { kind, amount } = this.form.getRawValue();
    const request: Observable<Movement> =
      kind === 'DEPOSIT'
        ? this.transactionService.deposit(this.accountId(), amount!)
        : this.transactionService.withdraw(this.accountId(), amount!);

    this.saving.set(true);
    request.subscribe({
      next: (movement) => {
        this.saving.set(false);
        this.form.reset({ kind });
        const amount = formatCurrency(movement.amount, this.locale, '$', 'COP', '1.2-2');
        this.success.set(kind === 'DEPOSIT' ? `Consignación de ${amount} registrada.` : `Retiro de ${amount} registrado.`);
        this.completed.emit(movement);
      },
      error: (error) => {
        // Ej.: 422 por saldo insuficiente; el mensaje del backend ya explica el motivo.
        this.saving.set(false);
        this.serverError.set(apiErrorMessage(error));
      },
    });
  }
}
