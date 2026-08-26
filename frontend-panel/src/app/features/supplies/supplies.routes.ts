import { Routes } from '@angular/router';

export const SUPPLY_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./supply-list/supply-list').then((m) => m.SupplyList),
  },
  {
    path: 'nuevo',
    loadComponent: () => import('./supply-form/supply-form').then((m) => m.SupplyForm),
  },
  {
    path: ':id/editar',
    loadComponent: () => import('./supply-form/supply-form').then((m) => m.SupplyForm),
  },
];
