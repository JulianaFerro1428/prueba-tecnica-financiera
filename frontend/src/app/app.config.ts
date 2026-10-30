import { registerLocaleData } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import localeEsCo from '@angular/common/locales/es-CO';
import { ApplicationConfig, LOCALE_ID, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';

// Formatos de Colombia: "$ 1.250.000", "jueves 30 de octubre".
registerLocaleData(localeEsCo);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    // Habilita HttpClient para que los servicios puedan llamar a la API del backend.
    provideHttpClient(),
    { provide: LOCALE_ID, useValue: 'es-CO' },
  ],
};
