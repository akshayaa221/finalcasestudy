import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http'; // <-- Update this import

import { routes } from './app.routes';
import { AuthInterceptor } from './interceptors/auth-interceptor'; // <-- Import AuthInterceptor

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    // Configure HttpClient to use interceptors
    provideHttpClient(withInterceptorsFromDi()), // <-- Add this to enable DI-based interceptors
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true // <-- Essential for multiple interceptors
    }
  ]
};