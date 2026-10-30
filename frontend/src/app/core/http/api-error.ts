import { HttpErrorResponse } from '@angular/common/http';

/**
 * Convierte un error HTTP en un mensaje para el trabajador del banco.
 * El backend responde con ProblemDetail: el texto útil viene en "detail".
 */
export function apiErrorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'No hay conexión con el servidor. Verifica que el backend esté encendido e intenta de nuevo.';
    }
    const detail = error.error?.detail;
    if (typeof detail === 'string' && detail.length > 0) {
      return detail;
    }
  }
  return 'Ocurrió un error inesperado. Intenta de nuevo.';
}
