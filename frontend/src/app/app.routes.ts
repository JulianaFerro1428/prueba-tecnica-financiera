import { Routes } from '@angular/router';

const clientForm = () => import('./features/clients/pages/client-form/client-form').then((m) => m.ClientForm);

// Carga diferida: el código de cada pantalla se descarga solo cuando se visita.
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'clients' },
  {
    path: 'clients',
    title: 'Clientes · Ventanilla',
    loadComponent: () => import('./features/clients/pages/client-list/client-list').then((m) => m.ClientList),
  },
  { path: 'clients/new', title: 'Registrar cliente · Ventanilla', loadComponent: clientForm },
  { path: 'clients/:id/edit', title: 'Editar cliente · Ventanilla', loadComponent: clientForm },
  {
    path: 'accounts',
    title: 'Cuentas · Ventanilla',
    loadComponent: () => import('./features/accounts/pages/account-list/account-list').then((m) => m.AccountList),
  },
  {
    path: 'accounts/new',
    title: 'Abrir cuenta · Ventanilla',
    loadComponent: () => import('./features/accounts/pages/account-form/account-form').then((m) => m.AccountForm),
  },
  {
    path: 'accounts/:id',
    title: 'Estado de cuenta · Ventanilla',
    loadComponent: () =>
      import('./features/accounts/pages/account-statement/account-statement').then((m) => m.AccountStatement),
  },
];
