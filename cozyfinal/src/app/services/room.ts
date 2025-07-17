import { Injectable } from '@angular/core';
import { BaseApiService } from './base-api';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Hotel } from './hotel';

export type BedType = 'SINGLE' | 'DOUBLE' | 'KING' | 'QUEEN' | 'TWIN' | 'FULL' | 'STUDIO';

export interface Room {
  id?: number;
  roomNumber: string;
  bedType: BedType;
  maxOccupancy: number;
  baseFare: number;
  acNonAc: boolean;
  roomSize?: string;
  size?: string;
  maxPeople?: number;
  available?: boolean;
  imageUrls?: string[];
  hotel?: Hotel | null;
}

@Injectable({
  providedIn: 'root'
})
export class RoomService extends BaseApiService {

  private roomApiUrl = `${this.API_BASE_URL}/rooms`;

  constructor(protected override http: HttpClient) {
    super(http);
  }

  createRoom(hotelId: number, room: Room): Observable<Room> {
    return this.http.post<Room>(`${this.roomApiUrl}/hotel/${hotelId}`, room)
      .pipe(
        catchError(this.handleError)
      );
  }

  getRoomById(id: number): Observable<Room> {
    return this.http.get<Room>(`${this.roomApiUrl}/${id}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  getRoomsByHotelId(hotelId: number): Observable<Room[]> {
    return this.http.get<Room[]>(`${this.roomApiUrl}/hotel/${hotelId}`)
      .pipe(
        catchError(this.handleError)
      );
  }

  getAllRooms(): Observable<Room[]> {
    return this.http.get<Room[]>(this.roomApiUrl)
      .pipe(
        catchError(this.handleError)
      );
  }

  updateRoom(id: number, room: Room): Observable<Room> {
    return this.http.put<Room>(`${this.roomApiUrl}/${id}`, room)
      .pipe(
        catchError(this.handleError)
      );
  }

  deleteRoom(id: number): Observable<any> {
    return this.http.delete<any>(`${this.roomApiUrl}/${id}`)
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
