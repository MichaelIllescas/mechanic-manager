import { Routes } from '@angular/router';

export const SERVICE_TYPE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./service-type-list/service-type-list').then((m) => m.ServiceTypeList),
  },
  {
    path: 'nuevo',
    loadComponent: () =>
      import('./service-type-form/service-type-form').then((m) => m.ServiceTypeForm),
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./service-type-form/service-type-form').then((m) => m.ServiceTypeForm),
  },
];
