# OrderHub web app

Angular 22 front end for OrderHub. See the [project README](../README.md) for the full picture.

```bash
npm ci
npm start          # http://localhost:4200, proxies /api to http://localhost:8080
npm test           # unit tests (Vitest)
npm run build      # production build in dist/frontend
npm run format     # Prettier
```

## Structure

```
src/app
├── core/          models, API clients, auth (service, interceptor, guards), error mapping
├── layout/        application shell with navigation
├── shared/        status badge, pager, toasts
└── features/      one folder per page: dashboard, orders, products, customers, login
```

Every page is a lazily loaded standalone component. State is held in signals; HTTP calls go through typed API services, and every error is turned into one readable sentence by `core/errors.ts`.
