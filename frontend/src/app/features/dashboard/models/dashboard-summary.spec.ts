import { Account } from '../../accounts/models/account.model';
import { buildDashboardSummary } from './dashboard-summary';

function account(id: number, type: Account['type'], status: Account['status'], balance: number): Account {
  return {
    id,
    type,
    number: (type === 'SAVINGS' ? '53' : '33') + String(id).padStart(8, '0'),
    status,
    balance,
    availableBalance: balance,
    gmfExempt: false,
    clientId: 1,
    createdAt: '2026-10-30T10:00:00',
    updatedAt: '2026-10-30T10:00:00',
  };
}

describe('buildDashboardSummary', () => {
  it('suma el dinero por tipo de cuenta y cuenta las cuentas por estado', () => {
    const summary = buildDashboardSummary(3, [
      account(1, 'SAVINGS', 'ACTIVE', 100000),
      account(2, 'SAVINGS', 'INACTIVE', 50000),
      account(3, 'CHECKING', 'ACTIVE', 250000),
      account(4, 'CHECKING', 'CANCELLED', 0),
    ]);

    expect(summary.clientCount).toBe(3);
    expect(summary.totalBalance).toBe(400000);
    expect(summary.savingsBalance).toBe(150000);
    expect(summary.checkingBalance).toBe(250000);
    expect(summary.byStatus).toEqual({ ACTIVE: 2, INACTIVE: 1, CANCELLED: 1 });
  });

  it('ordena las cuentas de mayor a menor saldo', () => {
    const summary = buildDashboardSummary(1, [
      account(1, 'SAVINGS', 'ACTIVE', 10),
      account(2, 'CHECKING', 'ACTIVE', 30),
      account(3, 'SAVINGS', 'ACTIVE', 20),
    ]);

    expect(summary.accountsByBalance.map((a) => a.id)).toEqual([2, 3, 1]);
  });

  it('funciona sin cuentas', () => {
    const summary = buildDashboardSummary(0, []);

    expect(summary.totalBalance).toBe(0);
    expect(summary.byStatus).toEqual({ ACTIVE: 0, INACTIVE: 0, CANCELLED: 0 });
  });
});
