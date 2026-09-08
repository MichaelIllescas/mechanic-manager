import { Routes } from '@angular/router';

export const QUOTE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./quote-list/quote-list').then((component) => component.QuoteList),
  },
  {
    path: 'nuevo',
    loadComponent: () =>
      import('./quote-form/quote-form').then((component) => component.QuoteForm),
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./quote-form/quote-form').then((component) => component.QuoteForm),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./quote-detail/quote-detail').then((component) => component.QuoteDetail),
  },
];
