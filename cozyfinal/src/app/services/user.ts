import { Injectable } from '@angular/core';
import { BaseApiService } from './base-api';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';

export type UserRole = 'GUEST' | 'HOTEL_OWNER' | 'ADMIN';

export interface User {
  id?: number;
  username: string;
  email: string;
  password?: string;
  name?: string;
  gender?: string;
  contactNumber?: string;
  address?: string;
  roles?: UserRole[];
}

@Injectable({
  providedIn: 'root'
})
export class UserService extends BaseApiService {

  private userApiUrl = `${this.API_BASE_URL}/users`;
  private authRegisterUrl = `${this.API_BASE_URL}/auth/register`;

  constructor(protected override http: HttpClient) {
    super(http);
  }

  registerUser(user: User): Observable<User> {
    const registrationPayload = {
      username: user.username,
      email: user.email,
      password: user.password,
      name: user.name,
      gender: user.gender,
      contactNumber: user.contactNumber,
      address: user.address,
    };
    return this.http.post<User>(this.authRegisterUrl, registrationPayload)
      .pipe(
        catchError(this.handleError)
      );
  }

  createUser(user: User): Observable<User> {
    return this.registerUser(user);
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.userApiUrl}/${id}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.userApiUrl)
      .pipe(
        catchError(this.handleError)
      );
  }

  updateUser(id: number, userDetails: User): Observable<User> {
    return this.http.put<User>(`${this.userApiUrl}/${id}`, userDetails)
      .pipe(
        catchError(this.handleError)
      );
  }

  deleteUser(id: number): Observable<any> {
    return this.http.delete<any>(`${this.userApiUrl}/${id}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  // If you need this, ensure your backend has a /users/role/{role} endpoint
  getUsersByRole(role: UserRole): Observable<User[]> {
    return this.http.get<User[]>(`${this.userApiUrl}/role/${role}`)
      .pipe(
        catchError(this.handleError)
      );
  }
}