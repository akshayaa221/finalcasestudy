import { Injectable } from '@angular/core';
import { BaseApiService } from './base-api';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { User } from './user';
import { Room } from './room';

// Define BookingStatus enum to match your backend
export enum BookingStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  CANCELLED = 'CANCELLED',
  COMPLETED = 'COMPLETED'
}

// Define the Booking interface to match your backend's Booking entity/DTO
export interface Booking {
  id?: number;
  user?: User;
  room?: Room;
  bookingDate?: string;
  checkInDate: string;
  checkOutDate: string;
  numberOfRooms: number;
  numberOfAdults: number;
  numberOfChildren?: number;
  peopleCount?: number;
  totalFare: number;
  bookingStatus: BookingStatus; // <-- FIXED: Changed from 'status' to 'bookingStatus'
}

@Injectable({
  providedIn: 'root'
})
export class BookingService extends BaseApiService {

  private bookingApiUrl = `${this.API_BASE_URL}/bookings`;

  constructor(protected override http: HttpClient) {
    super(http);
  }

  createBooking(booking: Booking): Observable<Booking> {
    return this.http.post<Booking>(this.bookingApiUrl, booking)
      .pipe(
        catchError(this.handleError)
      );
  }

  getAllBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(this.bookingApiUrl)
      .pipe(
        catchError(this.handleError)
      );
  }

  getBookingById(id: number): Observable<Booking> {
    return this.http.get<Booking>(`${this.bookingApiUrl}/${id}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  getUserBookings(userId: number): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.API_BASE_URL}/users/${userId}/bookings`)
      .pipe(
        catchError(this.handleError)
      );
  }

  getHotelBookings(hotelId: number): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.bookingApiUrl}/hotel/${hotelId}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  updateBooking(id: number, booking: Partial<Booking>): Observable<Booking> {
    return this.http.put<Booking>(`${this.bookingApiUrl}/${id}`, booking)
      .pipe(
        catchError(this.handleError)
      );
  }

  cancelBooking(id: number): Observable<Booking> {
    return this.updateBooking(id, { bookingStatus: BookingStatus.CANCELLED });
  }

  deleteBooking(id: number): Observable<any> {
    return this.http.delete<any>(`${this.bookingApiUrl}/${id}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  checkRoomAvailability(roomId: number, checkInDate: string, checkOutDate: string): Observable<boolean> {
    const params = { checkIn: checkInDate, checkOut: checkOutDate };
    return this.http.get<boolean>(`${this.API_BASE_URL}/rooms/${roomId}/availability`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }
}
