import { Movement } from '../../accounts/models/movement.model';

/** Datos que envía el formulario de transferencia a POST /api/transfers. */
export interface TransferRequest {
  sourceAccountId: number;
  targetAccountId: number;
  amount: number;
}

/** Comprobante de la transferencia: el débito en el origen y el crédito en el destino. */
export interface TransferResult {
  reference: string;
  debit: Movement;
  credit: Movement;
}
