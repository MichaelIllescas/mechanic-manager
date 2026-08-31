import { Routes } from '@angular/router';

export const VEHICLE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./vehicle-list/vehicle-list').then((component) => component.VehicleList),
  },
  {
    path: 'nuevo',
    loadComponent: () =>
      import('./vehicle-form/vehicle-form').then((component) => component.VehicleForm),
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./vehicle-form/vehicle-form').then((component) => component.VehicleForm),
  },
];
