import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth/auth.guards';
import { Shell } from './layout/shell';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    title: 'Sign in | OrderHub',
    loadComponent: () => import('./features/login/login-page').then((m) => m.LoginPage),
  },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        title: 'Overview | OrderHub',
        loadComponent: () =>
          import('./features/dashboard/dashboard-page').then((m) => m.DashboardPage),
      },
      {
        path: 'orders',
        title: 'Orders | OrderHub',
        loadComponent: () => import('./features/orders/orders-page').then((m) => m.OrdersPage),
      },
      {
        path: 'orders/new',
        title: 'New order | OrderHub',
        loadComponent: () =>
          import('./features/orders/order-create-page').then((m) => m.OrderCreatePage),
      },
      {
        path: 'orders/:id',
        title: 'Order | OrderHub',
        loadComponent: () =>
          import('./features/orders/order-detail-page').then((m) => m.OrderDetailPage),
      },
      {
        path: 'products',
        title: 'Products | OrderHub',
        loadComponent: () =>
          import('./features/products/products-page').then((m) => m.ProductsPage),
      },
      {
        path: 'customers',
        title: 'Customers | OrderHub',
        loadComponent: () =>
          import('./features/customers/customers-page').then((m) => m.CustomersPage),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
