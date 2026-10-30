import { inject } from '@angular/core';
import { Router } from '@angular/router';

/**
 * Lee el mensaje que un formulario envía al volver a una lista, por ejemplo "Cliente María Gómez registrado.".
 * Debe llamarse en el constructor de la pantalla, mientras la navegación todavía está en curso.
 */
export function readNavigationNotice(): string | null {
  const notice = inject(Router).currentNavigation()?.extras.state?.['notice'];
  return typeof notice === 'string' ? notice : null;
}
