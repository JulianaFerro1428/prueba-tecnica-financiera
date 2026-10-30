import { Account } from './account.model';

export type TransactionType = 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER';
/** CREDIT: entra dinero a la cuenta. DEBIT: sale dinero. */
export type MovementType = 'CREDIT' | 'DEBIT';

/** Una línea del estado de cuenta, tal como la devuelve el backend. */
export interface Movement {
  id: number;
  /** Las dos líneas de una misma transferencia comparten esta referencia. */
  reference: string;
  accountId: number;
  transactionType: TransactionType;
  movementType: MovementType;
  amount: number;
  balanceAfter: number;
  counterpartyAccountId: number | null;
  createdAt: string;
}

/** Respuesta de GET /api/accounts/{id}/statement. */
export interface Statement {
  account: Account;
  movements: Movement[];
}
