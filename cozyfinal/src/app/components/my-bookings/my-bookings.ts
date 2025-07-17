import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BookingService, Booking, BookingStatus } from '../../services/booking';
import { AuthService, DecodedToken } from '../../services/auth';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-my-bookings',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './my-bookings.html', // <-- FIXED: Changed to .html
  styleUrl: './my-bookings.scss' // <-- FIXED: Changed to .scss
})
export class MyBookingsComponent implements OnInit {
  bookings: Booking[] = [];
  currentUser: DecodedToken | null = null;
  isLoading: boolean = true;
  errorMessage: string | null = null;
  successMessage: string | null = null; // <-- ADDED: successMessage property

  constructor(
    private bookingService: BookingService,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;
    if (this.currentUser && this.currentUser.userId) {
      this.loadMyBookings(this.currentUser.userId);
    } else {
      this.errorMessage = 'User not logged in or user ID not found.';
      console.error(this.errorMessage);
      this.isLoading = false;
    }
  }

  loadMyBookings(userId: number): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.successMessage = null; // Clear success message on new load
    this.bookingService.getUserBookings(userId).subscribe({
      next: (data: Booking[]) => {
        this.bookings = data;
        this.isLoading = false;
        if (this.bookings.length === 0) {
          console.info('You have no bookings yet.');
        }
      },
      error: (err: HttpErrorResponse) => {
        console.error('Failed to load bookings:', err);
        if (err.status === 403) {
          this.errorMessage = 'Forbidden: You are not authorized to view these bookings.';
          console.error('Authorization Error:', this.errorMessage);
        } else if (err.error && err.error.message) {
          this.errorMessage = `Failed to load bookings: ${err.error.message}`;
          console.error(this.errorMessage);
        } else {
          this.errorMessage = 'Failed to load bookings. Server error.';
          console.error(this.errorMessage);
        }
        this.isLoading = false;
      }
    });
  }

  cancelBooking(bookingId: number): void {
    if (confirm('Are you sure you want to cancel this booking?')) {
      this.bookingService.cancelBooking(bookingId).subscribe({
        next: (response: Booking) => {
          this.successMessage = `Booking ${response.id} cancelled successfully!`; // Set success message
          console.log(this.successMessage);
          const index = this.bookings.findIndex(b => b.id === bookingId);
          if (index !== -1) {
            this.bookings[index].bookingStatus = BookingStatus.CANCELLED; // <-- FIXED: Changed 'status' to 'bookingStatus'
          }
        },
        error: (err: HttpErrorResponse) => {
          console.error('Failed to cancel booking:', err);
          let msg = 'Failed to cancel booking. Please try again.';
          if (err.error && err.error.message) {
            msg = err.error.message;
          } else if (typeof err.error === 'string') {
            msg = err.error;
          }
          this.errorMessage = msg; // Set error message
          console.error('Cancellation Failed:', msg);
        }
      });
    }
  }

  // Method to determine if a booking can be cancelled
  canCancel(status: BookingStatus | undefined): boolean {
    return status === BookingStatus.PENDING || status === BookingStatus.CONFIRMED;
  }
}
