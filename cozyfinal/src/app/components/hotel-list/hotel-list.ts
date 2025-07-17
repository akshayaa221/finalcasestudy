import { Component, OnInit } from '@angular/core';
import { HotelService, Hotel } from '../../services/hotel'; // Corrected import path
import { CommonModule } from '@angular/common'; // For *ngFor, *ngIf
import { RouterLink } from '@angular/router'; // For routerLink in template
import { FormsModule } from '@angular/forms'; // <<< IMPORTANT: For ngModel

@Component({
  selector: 'app-hotel-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule], // Add FormsModule
  templateUrl: './hotel-list.html',
  styleUrl: './hotel-list.scss'
})
export class HotelListComponent implements OnInit {
  hotels: Hotel[] = []; // Stores all hotels or current search results
  isLoading: boolean = true;
  errorMessage: string | null = null;

  // --- New properties for search functionality ---
  searchLocation: string = ''; // Binds to the search input field
  hasSearched: boolean = false; // Flag to indicate if a search has been performed
  // --- End new properties ---

  constructor(private hotelService: HotelService) {}

  ngOnInit(): void {
    this.loadHotels(); // Load all hotels initially
  }

  /**
   * Loads all available hotels or performs a search based on `searchLocation`.
   */
  loadHotels(): void {
    this.isLoading = true;
    this.errorMessage = null;

    if (this.searchLocation.trim()) {
      // If there's a search term, perform a search
      this.hasSearched = true;
      this.hotelService.searchHotels(this.searchLocation.trim()).subscribe({
        next: (data: Hotel[]) => {
          this.hotels = data; // Update hotels with search results
          this.isLoading = false;
          if (this.hotels.length === 0) {
            this.errorMessage = `No hotels found for location: "${this.searchLocation}".`;
          }
        },
        error: (err) => {
          console.error('Search failed:', err);
          this.errorMessage = `Search failed: ${err.error?.message || 'Server error'}`;
          this.isLoading = false;
        }
      });
    } else {
      // If no search term, load all hotels
      this.hasSearched = false; // Reset search flag
      this.hotelService.getAllHotels().subscribe({
        next: (data: Hotel[]) => {
          this.hotels = data;
          this.isLoading = false;
          if (this.hotels.length === 0) {
            console.info('No hotels found.');
          }
        },
        error: (err) => {
          console.error('Failed to load hotels:', err);
          this.errorMessage = `Failed to load hotels: ${err.error?.message || 'Server error'}`;
          this.isLoading = false;
        }
      });
    }
  }

  /**
   * Triggers the search when the search button is clicked or Enter is pressed.
   */
  onSearch(): void {
    this.loadHotels(); // Re-call loadHotels, which will now perform a search
  }

  /**
   * Clears the search input and reloads all hotels.
   */
  clearSearch(): void {
    this.searchLocation = ''; // Clear the input field
    this.loadHotels(); // Reload all hotels
  }

  // You might add methods for viewing hotel details, or managing hotels (for admin) here later
  viewHotelDetails(hotelId: number): void {
    // This is handled by routerLink in the HTML, but keeping it here as a placeholder
    console.log('Navigate to hotel details for ID:', hotelId);
  }
}