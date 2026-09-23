import { Routes } from '@angular/router';

export const WORK_ORDER_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./work-order-list/work-order-list').then((component) => component.WorkOrderList),
  },
  {
    path: 'nuevo',
    loadComponent: () =>
      import('./work-order-form/work-order-form').then((component) => component.WorkOrderForm),
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./work-order-form/work-order-form').then((component) => component.WorkOrderForm),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./work-order-detail/work-order-detail').then(
        (component) => component.WorkOrderDetail,
      ),
  },
];
