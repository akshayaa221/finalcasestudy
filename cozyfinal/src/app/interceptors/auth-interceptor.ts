import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth'; // Import your AuthService

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) {} // Inject AuthService

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const authToken = this.authService.getToken(); // Get the token from AuthService

    // Only add the token if it exists and the request is to your backend API
    // This prevents sending your JWT to external APIs like Google Fonts etc.
    if (authToken && request.url.startsWith(this.authService['API_BASE_URL'])) { // Access protected API_BASE_URL
      // Clone the request and add the Authorization header
      const authRequest = request.clone({
        setHeaders: {
          Authorization: `Bearer ${authToken}`
        }
      });
      return next.handle(authRequest); // Send the cloned request
    }

    // If no token or not an API request to your backend, just pass the original request
    return next.handle(request);
  }
}