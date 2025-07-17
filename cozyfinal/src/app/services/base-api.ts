import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export abstract class BaseApiService {

  protected API_BASE_URL = 'http://localhost:8080/api/v1'; // Ensure this matches your backend URL

  constructor(protected http: HttpClient) { }

  protected handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unknown error occurred!';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      if (typeof error.error === 'string') {
        errorMessage = `Server Error: ${error.error}`;
      } else if (error.error && error.error.message) {
        errorMessage = `Server Error (${error.status}): ${error.error.message}`;
      } else {
        errorMessage = `Server Error Code: ${error.status}\nMessage: ${error.message}`;
      }
    }
    console.error(errorMessage, error);
    return throwError(() => new Error(errorMessage));
  }
}