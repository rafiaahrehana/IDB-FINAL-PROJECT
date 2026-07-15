import { ApplicationConfig, DEFAULT_CURRENCY_CODE, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import {
  provideHttpClient,
  withInterceptorsFromDi,
  HTTP_INTERCEPTORS,
  withXhr,
} from '@angular/common/http';
import { provideCharts, withDefaultRegisterables } from 'ng2-charts';

import { routes } from './app.routes';
import { AuthInterceptor } from './core/interceptors/auth.interceptor';
import { ErrorInterceptor } from './core/interceptors/error.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withXhr(), withInterceptorsFromDi()),
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
    // Backend defaults every amount (expenses, invoices, payroll, etc.) to BDT -
    // without this, the bare `| currency` pipe used throughout the app silently
    // formats every amount as USD ($), which doesn't match what's actually stored.
    { provide: DEFAULT_CURRENCY_CODE, useValue: 'BDT' },
    provideCharts(withDefaultRegisterables()),
  ],
};
