import { Routes } from '@angular/router';

// Carga diferida: el código de cada pantalla se descarga solo cuando se visita.
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'clients' },
  {
    path: 'clients',
    title: 'Clientes · Ventanilla',
    loadComponent: () => import('./features/clients/pages/client-list/client-list').then((m) => m.ClientList),
  },
];
