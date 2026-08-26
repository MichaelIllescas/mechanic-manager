import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  {
    path: 'usuarios',
    loadComponent: () => import('./users/user-list/user-list').then((m) => m.UserList),
  },
  {
    path: 'usuarios/nuevo',
    loadComponent: () => import('./users/user-form/user-form').then((m) => m.UserForm),
  },
  {
    path: 'usuarios/:id/editar',
    loadComponent: () => import('./users/user-form/user-form').then((m) => m.UserForm),
  },
  {
    path: 'usuarios/:id',
    loadComponent: () => import('./users/user-detail/user-detail').then((m) => m.UserDetail),
  },
  { path: '', redirectTo: 'usuarios', pathMatch: 'full' },
];
