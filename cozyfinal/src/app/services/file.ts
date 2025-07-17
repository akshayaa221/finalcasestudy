import { Injectable } from '@angular/core';
import { BaseApiService } from './base-api';
import { HttpClient, HttpEvent, HttpRequest } from '@angular/common/http'; // Import HttpEvent, HttpRequest
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class FileService extends BaseApiService {

  private fileApiUrl = `${this.API_BASE_URL}/files`; // Matches your backend's FileController @RequestMapping

  constructor(http: HttpClient) {
    super(http);
  }

  /**
   * Uploads a single file to the backend.
   * @param file The File object to upload.
   * @returns An Observable of the HTTP event, allowing progress tracking.
   */
  uploadFile(file: File): Observable<HttpEvent<any>> {
    const formData: FormData = new FormData();
    formData.append('file', file); // 'file' must match the @RequestParam name in your Spring Boot controller

    // Create a custom HttpRequest to report upload progress
    const req = new HttpRequest('POST', `${this.fileApiUrl}/upload`, formData, {
      reportProgress: true, // Enable progress tracking
      responseType: 'text' // Expecting a string response ("File uploaded successfully! Download URL: ...")
    });

    return this.http.request(req).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Uploads multiple files to the backend.
   * @param files An array of File objects to upload.
   * @returns An Observable of the HTTP event.
   */
  uploadMultipleFiles(files: File[]): Observable<HttpEvent<any>> {
    const formData: FormData = new FormData();
    files.forEach(file => {
      formData.append('files', file); // 'files' must match the @RequestParam name in your Spring Boot controller
    });

    const req = new HttpRequest('POST', `${this.fileApiUrl}/uploadMultiple`, formData, {
      reportProgress: true,
      responseType: 'json' // Assuming your backend returns a list of JSON responses
    });

    return this.http.request(req).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Downloads a file from the backend.
   * @param fileName The name of the file to download.
   * @returns An Observable of type Blob, representing the file data.
   */
  downloadFile(fileName: string): Observable<Blob> {
    // responseType: 'blob' is crucial for downloading binary data
    return this.http.get(`${this.fileApiUrl}/download/${fileName}`, { responseType: 'blob' })
      .pipe(
        catchError(this.handleError)
      );
  }
}