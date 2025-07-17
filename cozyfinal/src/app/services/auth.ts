import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { BaseApiService } from './base-api';
import { jwtDecode } from 'jwt-decode'; // Make sure you have 'jwt-decode' installed (npm install jwt-decode)

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  id: number;
  email: string;
  roles: string[];
  tokenType: string;
}

export interface DecodedToken {
  sub: string;
  roles: string[];
  exp: number;
  iat: number;
  userId?: number; // Added userId to decoded token
}

@Injectable({
  providedIn: 'root'
})
export class AuthService extends BaseApiService {

  private authApiUrl = `${this.API_BASE_URL}/auth`;

  private currentUserSubject: BehaviorSubject<DecodedToken | null>;
  public currentUser: Observable<DecodedToken | null>;

  constructor(protected override http: HttpClient) {
    super(http);
    const storedToken = localStorage.getItem('jwt_token');
    let initialUser: DecodedToken | null = null;
    if (storedToken) {
      try {
        const decoded = jwtDecode<DecodedToken>(storedToken);
        if (decoded.exp * 1000 > Date.now()) {
          initialUser = decoded;
        } else {
          localStorage.removeItem('jwt_token');
          console.warn('Stored JWT token expired. User logged out.');
        }
      } catch (error) {
        console.error('Failed to decode stored token:', error);
        localStorage.removeItem('jwt_token');
      }
    }
    this.currentUserSubject = new BehaviorSubject<DecodedToken | null>(initialUser);
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): DecodedToken | null {
    return this.currentUserSubject.value;
  }

  getToken(): string | null {
    return localStorage.getItem('jwt_token');
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }
    try {
      const decoded = jwtDecode<DecodedToken>(token);
      return decoded.exp * 1000 > Date.now();
    } catch (error) {
      console.error('Invalid token during isLoggedIn check:', error);
      this.logout();
      return false;
    }
  }

  getRoles(): string[] {
    const user = this.currentUserValue;
    return user && user.roles ? user.roles : [];
  }

  hasRole(role: string): boolean {
    const springRole = role.startsWith('ROLE_') ? role : `ROLE_${role.toUpperCase()}`;
    return this.getRoles().includes(springRole);
  }

  login(credentials: LoginCredentials): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.authApiUrl}/login`, credentials)
      .pipe(
        tap(response => {
          if (response.accessToken) {
            localStorage.setItem('jwt_token', response.accessToken);
            const decoded = jwtDecode<DecodedToken>(response.accessToken);
            this.currentUserSubject.next(decoded);
            console.log('User logged in:', decoded.sub, 'Roles:', decoded.roles);
          }
        }),
        catchError(this.handleError)
      );
  }

  logout(): void {
    localStorage.removeItem('jwt_token');
    this.currentUserSubject.next(null);
    console.log('User logged out.');
  }
}