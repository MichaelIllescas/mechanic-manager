import { Routes } from '@angular/router';

import { adminGuard } from './core/guards/admin-guard';
import { authGuard } from './core/guards/auth-guard';
import { Login } from './features/auth/login/login';

export const routes: Routes = [
  { path: 'login', component: Login },
  {
    path: 'admin',
    canMatch: [adminGuard],
    loadChildren: () => import('./features/admin/admin.routes').then((m) => m.ADMIN_ROUTES),
  },
  {
    path: 'inicio',
    canMatch: [authGuard],
    loadComponent: () => import('./features/workshop/inicio/inicio').then((m) => m.Inicio),
  },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' },
];
