import { Routes } from '@angular/router';

import { adminGuard } from './core/guards/admin-guard';
import { authGuard } from './core/guards/auth-guard';
import { guestGuard } from './core/guards/guest-guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login/login').then((component) => component.Login),
  },
  {
    path: 'recuperar-password',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/forgot-password/forgot-password').then(
        (component) => component.ForgotPassword,
      ),
  },
  {
    path: 'reset-password',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/reset-password/reset-password').then(
        (component) => component.ResetPassword,
      ),
  },
  {
    path: 'admin',
    canMatch: [adminGuard],
    loadChildren: () => import('./features/admin/admin.routes').then((m) => m.ADMIN_ROUTES),
  },
  {
    path: '',
    canMatch: [authGuard],
    loadComponent: () =>
      import('./layout/main-layout/main-layout').then((component) => component.MainLayout),
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard').then((component) => component.Dashboard),
      },
      {
        path: 'workshop',
        loadComponent: () =>
          import('./features/workshop/workshop/workshop').then((component) => component.Workshop),
      },
      {
        path: 'customers',
        loadChildren: () =>
          import('./features/customers/customers.routes').then((module) => module.CUSTOMER_ROUTES),
      },
      {
        path: 'vehicles',
        loadChildren: () =>
          import('./features/vehicles/vehicles.routes').then((module) => module.VEHICLE_ROUTES),
      },
      {
        path: 'appointments',
        loadChildren: () =>
          import('./features/appointments/appointments.routes').then(
            (module) => module.APPOINTMENT_ROUTES,
          ),
      },
      {
        path: 'servicios',
        loadChildren: () =>
          import('./features/service-types/service-types.routes').then(
            (m) => m.SERVICE_TYPE_ROUTES,
          ),
      },
      {
        path: 'mi-cuenta',
        loadComponent: () =>
          import('./features/account/account').then((component) => component.Account),
      },
      {
        path: 'insumos',
        loadChildren: () =>
          import('./features/supplies/supplies.routes').then((m) => m.SUPPLY_ROUTES),
      },
      {
        path: 'quotes',
        loadChildren: () =>
          import('./features/quotes/quotes.routes').then((module) => module.QUOTE_ROUTES),
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard',
      },
    ],
  },
  {
    path: '**',
    redirectTo: 'dashboard',
  },
];
