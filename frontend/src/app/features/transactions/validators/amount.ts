import { AbstractControl, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';

/** Igual que el backend (@Digits fraction = 2): pesos con máximo dos decimales. */
function maxTwoDecimals(control: AbstractControl<number | null>): ValidationErrors | null {
  const value = control.value;
  return value === null || /^\d+(\.\d{1,2})?$/.test(String(value)) ? null : { decimals: true };
}

/** Las mismas reglas que el backend aplica a cualquier monto: obligatorio, mayor que $0 y dos decimales. */
export const AMOUNT_VALIDATORS: ValidatorFn[] = [Validators.required, Validators.min(0.01), maxTwoDecimals];

/** Mensaje para mostrar bajo el campo, o null si no hay que mostrar nada todavía. */
export function amountErrorMessage(control: AbstractControl): string | null {
  if (!control.touched || control.valid) {
    return null;
  }
  if (control.hasError('required')) {
    return 'Escribe el monto';
  }
  return control.hasError('decimals') ? 'El monto admite máximo 2 decimales' : 'El monto debe ser mayor que $ 0';
}
