import { Account, AccountStatus } from '../../accounts/models/account.model';

export interface DashboardSummary {
  clientCount: number;
  totalBalance: number;
  savingsBalance: number;
  checkingBalance: number;
  byStatus: Record<AccountStatus, number>;
  accountsByBalance: Account[];
}

/**
 * Calcula las cifras del panel a partir de los datos del backend.
 * Es una función pura (sin HTTP ni pantalla) para poder probarla de forma sencilla.
 * Solo sirve para mostrar: los saldos oficiales siempre los calcula el backend.
 */
export function buildDashboardSummary(clientCount: number, accounts: Account[]): DashboardSummary {
  const sumOf = (list: Account[]) => list.reduce((total, a) => total + a.balance, 0);
  const byStatus: Record<AccountStatus, number> = { ACTIVE: 0, INACTIVE: 0, CANCELLED: 0 };
  accounts.forEach((a) => byStatus[a.status]++);

  return {
    clientCount,
    totalBalance: sumOf(accounts),
    savingsBalance: sumOf(accounts.filter((a) => a.type === 'SAVINGS')),
    checkingBalance: sumOf(accounts.filter((a) => a.type === 'CHECKING')),
    byStatus,
    accountsByBalance: [...accounts].sort((a, b) => b.balance - a.balance),
  };
}
