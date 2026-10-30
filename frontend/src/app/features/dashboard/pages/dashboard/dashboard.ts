import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { apiErrorMessage } from '../../../../core/http/api-error';
import { ACCOUNT_STATUS_LABELS, ACCOUNT_TYPE_LABELS, AccountStatus } from '../../../accounts/models/account.model';
import { AccountService } from '../../../accounts/services/account.service';
import { ClientService } from '../../../clients/services/client.service';
import { DashboardSummary, buildDashboardSummary } from '../../models/dashboard-summary';

type StatusFilter = AccountStatus | 'ALL';

const DONUT_RADIUS = 52;
const DONUT_CIRCUMFERENCE = 2 * Math.PI * DONUT_RADIUS;
const STATUSES: AccountStatus[] = ['ACTIVE', 'INACTIVE', 'CANCELLED'];
const TOP_ACCOUNTS = 5;

/** Panel de inicio: cuánto dinero maneja el banco, cómo se reparte y qué cuentas pesan más. */
@Component({
  selector: 'app-dashboard',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  private readonly clientService = inject(ClientService);
  private readonly accountService = inject(AccountService);

  protected readonly today = new Date();
  protected readonly typeLabels = ACCOUNT_TYPE_LABELS;
  protected readonly statusLabels = ACCOUNT_STATUS_LABELS;
  protected readonly statuses = STATUSES;
  protected readonly circumference = DONUT_CIRCUMFERENCE;

  protected readonly summary = signal<DashboardSummary | null>(null);
  protected readonly loadError = signal('');
  protected readonly statusFilter = signal<StatusFilter>('ALL');
  /** Parte de la banda o de la dona que el usuario está señalando, para resaltarla. */
  protected readonly highlighted = signal<string | null>(null);

  protected readonly savingsShare = computed(() => this.share(this.summary()?.savingsBalance ?? 0));

  protected readonly accountCount = computed(() => {
    const s = this.summary();
    return s ? s.byStatus.ACTIVE + s.byStatus.INACTIVE + s.byStatus.CANCELLED : 0;
  });

  /** Arcos de la dona: cada estado ocupa una porción proporcional a su cantidad de cuentas. */
  protected readonly donutArcs = computed(() => {
    const s = this.summary();
    const total = this.accountCount();
    let offset = 0;
    return STATUSES.map((status) => {
      const length = total === 0 ? 0 : ((s?.byStatus[status] ?? 0) / total) * DONUT_CIRCUMFERENCE;
      const arc = { status, length, offset };
      offset += length;
      return arc;
    });
  });

  protected readonly topAccounts = computed(() => {
    const filter = this.statusFilter();
    const accounts = this.summary()?.accountsByBalance ?? [];
    return accounts.filter((a) => filter === 'ALL' || a.status === filter).slice(0, TOP_ACCOUNTS);
  });

  constructor() {
    this.load();
  }

  protected load(): void {
    this.loadError.set('');
    forkJoin({ clients: this.clientService.getAll(), accounts: this.accountService.getAll() }).subscribe({
      next: ({ clients, accounts }) => this.summary.set(buildDashboardSummary(clients.length, accounts)),
      error: (error) => this.loadError.set(apiErrorMessage(error)),
    });
  }

  protected share(amount: number): number {
    const total = this.summary()?.totalBalance ?? 0;
    return total === 0 ? 0 : Math.round((amount / total) * 100);
  }

  protected toggleFilter(status: AccountStatus): void {
    this.statusFilter.update((current) => (current === status ? 'ALL' : status));
  }
}
