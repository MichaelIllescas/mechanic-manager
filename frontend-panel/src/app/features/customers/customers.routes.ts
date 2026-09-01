import { Routes } from '@angular/router';

export const CUSTOMER_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./customer-list/customer-list').then((component) => component.CustomerList),
  },
  {
    path: 'nuevo',
    loadComponent: () =>
      import('./customer-form/customer-form').then((component) => component.CustomerForm),
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./customer-form/customer-form').then((component) => component.CustomerForm),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./customer-detail/customer-detail').then((component) => component.CustomerDetail),
  },
];
