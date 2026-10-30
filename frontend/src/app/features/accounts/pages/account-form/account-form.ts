import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { apiErrorMessage } from '../../../../core/http/api-error';
import { Client } from '../../../clients/models/client.model';
import { ClientService } from '../../../clients/services/client.service';
import { ACCOUNT_TYPE_LABELS, AccountType } from '../../models/account.model';
import { AccountService } from '../../services/account.service';

/** Formulario para abrir una cuenta (/accounts/new). Solo se eligen cliente, tipo y exención GMF. */
@Component({
  selector: 'app-account-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './account-form.html',
  styleUrl: './account-form.css',
})
export class AccountForm {
  private readonly accountService = inject(AccountService);
  private readonly router = inject(Router);

  protected readonly typeLabels = ACCOUNT_TYPE_LABELS;
  protected readonly clients = signal<Client[] | null>(null);
  protected readonly saving = signal(false);
  protected readonly formError = signal('');

  protected readonly form = inject(FormBuilder).nonNullable.group({
    // El <select> entrega texto; se convierte a número al enviar.
    clientId: ['', Validators.required],
    type: ['' as AccountType | '', Validators.required],
    gmfExempt: [false],
  });

  constructor() {
    inject(ClientService).getAll().subscribe({
      next: (clients) => this.clients.set(clients),
      error: (error) => this.formError.set(apiErrorMessage(error)),
    });
  }

  protected showError(name: 'clientId' | 'type'): boolean {
    const control = this.form.controls[name];
    return control.touched && control.invalid;
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.formError.set('');
    const { clientId, type, gmfExempt } = this.form.getRawValue();

    this.accountService.open({ clientId: Number(clientId), type: type as AccountType, gmfExempt }).subscribe({
      next: (account) => {
        const owner = this.clients()?.find((c) => c.id === account.clientId);
        const ownerName = owner ? ` para ${owner.firstNames} ${owner.lastName}` : '';
        const typeName = this.typeLabels[account.type].toLowerCase();
        this.router.navigate(['/accounts'], {
          state: { notice: `Cuenta de ${typeName} ${account.number} abierta${ownerName}.` },
        });
      },
      error: (error) => {
        this.saving.set(false);
        this.formError.set(apiErrorMessage(error));
      },
    });
  }
}
