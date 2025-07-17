import { Component, OnInit } from '@angular/core';
import { BookingService, Booking, BookingStatus } from '@app-services/booking'; // Import BookingService, Booking, BookingStatus
import { CommonModule } from '@angular/common'; // For *ngFor, *ngIf, DatePipe
import { FormsModule } from '@angular/forms'; // For ngModel in dropdowns
import { HttpErrorResponse } from '@angular/common/http'; // For error typing

@Component({
  selector: 'app-booking-management',
  standalone: true,
  imports: [CommonModule, FormsModule], // Add CommonModule and FormsModule
  templateUrl: './booking-management.html',
  styleUrl: './booking-management.scss'
})
export class BookingManagementComponent implements OnInit {
  bookings: Booking[] = [];
  isLoading: boolean = true;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  // For status filtering/updating
  selectedStatusFilter: string = 'ALL'; // Default filter
  bookingStatuses: string[] = Object.values(BookingStatus); // Get all enum values as strings

  constructor(private bookingService: BookingService) {}

  ngOnInit(): void {
    this.loadAllBookings();
  }

  loadAllBookings(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.successMessage = null; // Clear messages on reload
    this.bookingService.getAllBookings().subscribe({
      next: (data: Booking[]) => {
        this.bookings = data;
        this.isLoading = false;
        console.log('All bookings loaded:', this.bookings);
      },
      error: (err: HttpErrorResponse) => {
        console.error('Failed to load all bookings:', err);
        if (err.status === 403) {
          this.errorMessage = 'Access Denied: You do not have permission to view all bookings. (Requires ADMIN role)';
        } else if (err.error && err.error.message) {
          this.errorMessage = `Failed to load bookings: ${err.error.message}`;
        } else {
          this.errorMessage = 'Failed to load bookings. Please try again later.';
        }
        this.isLoading = false;
      }
    });
  }

  // Filter bookings based on selected status
  get filteredBookings(): Booking[] {
    if (this.selectedStatusFilter === 'ALL') {
      return this.bookings;
    }
    return this.bookings.filter(booking => booking.bookingStatus === this.selectedStatusFilter);
  }

  // Update booking status
  updateBookingStatus(bookingId: number, newStatus: BookingStatus): void {
    this.errorMessage = null;
    this.successMessage = null;
    // Find the booking to get its current details
    const bookingToUpdate = this.bookings.find(b => b.id === bookingId);

    if (!bookingToUpdate) {
        this.errorMessage = 'Booking not found for update.';
        return;
    }

    // Create a partial booking object with only the ID and the new status
    // The backend should handle updating only the status based on ID
    const updatePayload = {
        id: bookingId,
        bookingStatus: newStatus
    };

    this.bookingService.updateBooking(bookingId, updatePayload as Booking).subscribe({ // Cast to Booking
      next: (updatedBooking) => {
        this.successMessage = `Booking ${updatedBooking.id} status updated to ${updatedBooking.bookingStatus}!`;
        console.log('Booking status updated:', updatedBooking);
        this.loadAllBookings(); // Reload all bookings to reflect changes
      },
      error: (err: HttpErrorResponse) => {
        console.error('Failed to update booking status:', err);
        this.errorMessage = `Failed to update status: ${err.error?.message || 'Server error'}`;
      }
    });
  }

  // Delete booking (optional, usually only for specific cases or test data)
  deleteBooking(bookingId: number): void {
    if (confirm(`Are you sure you want to delete booking ${bookingId}? This action cannot be undone.`)) {
      this.bookingService.deleteBooking(bookingId).subscribe({
        next: () => {
          this.successMessage = `Booking ${bookingId} deleted successfully!`;
          console.log('Booking deleted:', bookingId);
          this.loadAllBookings(); // Reload list
        },
        error: (err: HttpErrorResponse) => {
          console.error('Failed to delete booking:', err);
          this.errorMessage = `Failed to delete booking: ${err.error?.message || 'Server error'}`;
        }
      });
    }
  }
}