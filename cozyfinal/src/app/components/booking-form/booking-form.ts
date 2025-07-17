import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RoomService, Room } from '../../services/room';
import { AuthService, DecodedToken } from '../../services/auth';
import { BookingService, Booking, BookingStatus } from '../../services/booking'; // Added BookingStatus
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-booking-form',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './booking-form.html', // <-- FIXED: Changed to .html
  styleUrl: './booking-form.scss' // <-- FIXED: Changed to .scss
})
export class BookingFormComponent implements OnInit {
  roomId: number | null = null;
  room: Room | undefined;
  currentUser: DecodedToken | null = null;

  checkInDate: string = '';
  checkOutDate: string = '';
  numberOfRooms: number = 1;
  numberOfAdults: number = 1;
  numberOfChildren: number = 0;
  totalFare: number = 0;

  isLoadingRoom: boolean = true;
  isBooking: boolean = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  availabilityMessage: string | null = null;

  minCheckInDate: string = new Date().toISOString().split('T')[0];
  minCheckOutDate: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private roomService: RoomService,
    private authService: AuthService,
    private bookingService: BookingService,
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;
    if (!this.currentUser || !this.authService.isLoggedIn()) {
      console.warn('You must be logged in to book a room.', 'Unauthorized');
      this.router.navigate(['/login']);
      return;
    }

    this.route.paramMap.subscribe(params => {
      const id = params.get('roomId');
      if (id) {
        this.roomId = +id;
        this.loadRoomDetails(this.roomId);
      } else {
        this.errorMessage = 'Room ID not provided in the URL.';
        this.isLoadingRoom = false;
      }
    });
  }

  onCheckInDateChange(): void {
    if (this.checkInDate) {
      const checkIn = new Date(this.checkInDate);
      const nextDay = new Date(checkIn);
      nextDay.setDate(checkIn.getDate() + 1);
      this.minCheckOutDate = nextDay.toISOString().split('T')[0];

      if (this.checkOutDate && new Date(this.checkOutDate) <= checkIn) {
        this.checkOutDate = this.minCheckOutDate;
      }
    }
    this.calculateTotalFare();
  }

  onCheckOutDateChange(): void {
    this.calculateTotalFare();
  }

  loadRoomDetails(id: number): void {
    this.isLoadingRoom = true;
    this.errorMessage = null;
    this.roomService.getRoomById(id).subscribe({
      next: (data: Room) => {
        this.room = data;
        this.isLoadingRoom = false;
        this.calculateTotalFare();
      },
      error: (err: HttpErrorResponse) => {
        console.error('Failed to load room details:', err);
        if (err.status === 404) {
          this.errorMessage = 'Room not found. It might have been deleted or never existed.';
        } else if (err.error && err.error.message) {
          this.errorMessage = `Failed to load room details: ${err.error.message}`;
        } else {
          this.errorMessage = 'Failed to load room details. Server error.';
        }
        console.error(this.errorMessage);
        this.isLoadingRoom = false;
      }
    });
  }

  calculateTotalFare(): void {
    this.totalFare = 0;
    if (this.room && this.checkInDate && this.checkOutDate && this.numberOfRooms > 0 && this.numberOfAdults > 0) {
      const checkIn = new Date(this.checkInDate);
      const checkOut = new Date(this.checkOutDate);

      if (checkOut > checkIn) {
        const diffTime = Math.abs(checkOut.getTime() - checkIn.getTime());
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

        let farePerRoom = this.room.baseFare;
        let totalGuests = this.numberOfAdults + this.numberOfChildren;

        if (this.room.bedType === 'SINGLE') {
          if (totalGuests > 1) {
            farePerRoom += (this.numberOfAdults >= 2) ? (this.room.baseFare * 0.40) : (this.room.baseFare * 0.20);
          }
        } else if (this.room.bedType === 'DOUBLE') {
          if (totalGuests > 2) {
            let extra = totalGuests - 2;
            let remainingAdults = this.numberOfAdults - 2;
            let chargedAdults = Math.min(extra, Math.max(0, remainingAdults));
            farePerRoom += chargedAdults * this.room.baseFare * 0.40;
            farePerRoom += Math.max(0, extra - chargedAdults) * this.room.baseFare * 0.20;
          }
        } else if (this.room.bedType === 'KING') {
          if (totalGuests > 4) {
            let extra = totalGuests - 4;
            let remainingAdults = this.numberOfAdults - 4;
            let chargedAdults = Math.min(extra, Math.max(0, remainingAdults));
            farePerRoom += chargedAdults * this.room.baseFare * 0.40;
            farePerRoom += Math.max(0, extra - chargedAdults) * this.room.baseFare * 0.20;
          }
        }
        this.totalFare = farePerRoom * this.numberOfRooms * diffDays;
      } else {
        this.totalFare = 0;
      }
    } else {
      this.totalFare = 0;
    }
  }

  checkAvailability(): void {
    this.availabilityMessage = null;
    if (this.roomId && this.checkInDate && this.checkOutDate) {
      this.bookingService.checkRoomAvailability(this.roomId, this.checkInDate, this.checkOutDate).subscribe({
        next: (isAvailable: boolean) => {
          if (isAvailable) {
            this.availabilityMessage = 'Room is available for the selected dates!';
            console.log(this.availabilityMessage);
            this.calculateTotalFare();
          } else {
            this.availabilityMessage = 'Room is NOT available for the selected dates. Please choose different dates.';
            console.warn(this.availabilityMessage);
            this.totalFare = 0;
          }
        },
        error: (err: HttpErrorResponse) => {
          console.error('Availability check failed:', err);
          this.availabilityMessage = `Could not check availability: ${err.error?.message || 'Server error'}`;
          console.error(this.availabilityMessage);
          this.totalFare = 0;
        }
      });
    } else {
      this.availabilityMessage = 'Please select check-in and check-out dates.';
      console.info(this.availabilityMessage);
    }
  }

  submitBooking(): void {
    this.errorMessage = null;
    this.successMessage = null;
    this.isBooking = true;

    if (!this.room || !this.currentUser || !this.roomId) {
      this.errorMessage = 'Missing room or user information for booking.';
      console.error(this.errorMessage);
      this.isBooking = false;
      return;
    }

    if (this.numberOfAdults <= 0 || !this.checkInDate || !this.checkOutDate || this.totalFare <= 0 || this.numberOfRooms <= 0) {
      this.errorMessage = 'Please fill all required booking details correctly.';
      console.error(this.errorMessage);
      this.isBooking = false;
      return;
    }

    const userPayload = { id: this.currentUser.userId };
    const roomPayload = { id: this.roomId };

    const newBooking: Booking = {
      user: userPayload as any,
      room: roomPayload as any,
      checkInDate: this.checkInDate,
      checkOutDate: this.checkOutDate,
      numberOfRooms: this.numberOfRooms,
      numberOfAdults: this.numberOfAdults,
      numberOfChildren: this.numberOfChildren,
      totalFare: this.totalFare,
      bookingDate: new Date().toISOString().split('T')[0],
      bookingStatus: BookingStatus.PENDING, // Ensure this is imported and used
      peopleCount: this.numberOfAdults + this.numberOfChildren
    };

    this.bookingService.createBooking(newBooking).subscribe({
      next: (response: Booking) => {
        this.successMessage = `Booking successful! Booking ID: ${response.id}`;
        console.log('Booking Confirmed:', this.successMessage);
        this.isBooking = false;
        console.log('Booking response:', response);
        this.router.navigate(['/my-bookings']);
      },
      error: (err: HttpErrorResponse) => {
        console.error('Booking failed:', err);
        if (err && err.error && err.error.message) {
          this.errorMessage = err.error.message;
        } else if (typeof err.error === 'string') {
          this.errorMessage = err.error;
        }
        else {
          this.errorMessage = 'Booking failed. Please try again.';
        }
        console.error('Booking Failed:', this.errorMessage);
        this.isBooking = false;
      }
    });
  }
}
