export type AccountType = 'SAVINGS' | 'CHECKING';
export type AccountStatus = 'ACTIVE' | 'INACTIVE' | 'CANCELLED';

export const ACCOUNT_TYPE_LABELS: Record<AccountType, string> = {
  SAVINGS: 'Ahorros',
  CHECKING: 'Corriente',
};

export const ACCOUNT_STATUS_LABELS: Record<AccountStatus, string> = {
  ACTIVE: 'Activa',
  INACTIVE: 'Inactiva',
  CANCELLED: 'Cancelada',
};

/** Cuenta tal como la devuelve GET /api/accounts. El dinero llega como número JSON. */
export interface Account {
  id: number;
  type: AccountType;
  number: string;
  status: AccountStatus;
  balance: number;
  availableBalance: number;
  gmfExempt: boolean;
  clientId: number;
  createdAt: string;
  updatedAt: string;
}

/** Datos para abrir una cuenta. El número, el estado y el saldo los decide el backend. */
export interface OpenAccountRequest {
  clientId: number;
  type: AccountType;
  gmfExempt: boolean;
}
