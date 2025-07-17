import { Injectable } from '@angular/core';
import { BaseApiService } from './base-api';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { User } from './user';

export interface Hotel {
  id?: number;
  name: string;
  location: string;
  description?: string;
  amenities?: string;
  owner?: User;
  imageUrls?: string[];
  rating?: number;
}

@Injectable({
  providedIn: 'root'
})
export class HotelService extends BaseApiService {

  private hotelApiUrl = `${this.API_BASE_URL}/hotels`;

  constructor(protected override http: HttpClient) {
    super(http);
  }

  getAllHotels(): Observable<Hotel[]> {
    return this.http.get<Hotel[]>(this.hotelApiUrl)
      .pipe(
        catchError(this.handleError)
      );
  }

  getHotelById(id: number): Observable<Hotel> {
    return this.http.get<Hotel>(`${this.hotelApiUrl}/${id}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  searchHotels(location: string): Observable<Hotel[]> {
    return this.http.get<Hotel[]>(`${this.hotelApiUrl}/search`, { params: { location } })
      .pipe(
        catchError(this.handleError)
      );
  }

  addHotel(hotel: Hotel): Observable<Hotel> {
    return this.http.post<Hotel>(this.hotelApiUrl, hotel)
      .pipe(
        catchError(this.handleError)
      );
  }

  updateHotel(id: number, hotel: Hotel): Observable<Hotel> {
    return this.http.put<Hotel>(`${this.hotelApiUrl}/${id}`, hotel)
      .pipe(
        catchError(this.handleError)
      );
  }

  deleteHotel(id: number): Observable<any> {
    return this.http.delete<any>(`${this.hotelApiUrl}/${id}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  getHotelsByOwner(ownerId: number): Observable<Hotel[]> {
    return this.http.get<Hotel[]>(`${this.hotelApiUrl}/owner/${ownerId}`)
      .pipe(
        catchError(this.handleError)
      );
  }
}