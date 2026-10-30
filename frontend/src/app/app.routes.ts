import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'clients' },
  {
    path: 'clients',
    title: 'Clientes · Ventanilla',
    // Carga diferida: el código de la pantalla se descarga solo cuando se visita.
    loadComponent: () => import('./features/clients/pages/client-list/client-list').then((m) => m.ClientList),
  },
];
