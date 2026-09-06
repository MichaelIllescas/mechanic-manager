import { Routes } from '@angular/router';

export const APPOINTMENT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./appointment-calendar/appointment-calendar').then(
        (component) => component.AppointmentCalendar,
      ),
  },
  {
    path: 'nuevo',
    loadComponent: () =>
      import('./appointment-form/appointment-form').then(
        (component) => component.AppointmentForm,
      ),
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./appointment-form/appointment-form').then(
        (component) => component.AppointmentForm,
      ),
  },
];
