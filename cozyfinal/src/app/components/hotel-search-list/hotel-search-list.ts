import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HotelService, Hotel } from '../../services/hotel';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-hotel-search-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './hotel-search-list.html',
  styleUrl: './hotel-search-list.scss'
})
export class HotelSearchListComponent implements OnInit {
  searchResults: Hotel[] = [];
  searchLocation: string | null = null;
  isLoading: boolean = true;
  errorMessage: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private hotelService: HotelService
  ) {}

  ngOnInit(): void {
    // Subscribe to route parameter changes to get the search location
    this.route.paramMap.subscribe(params => {
      this.searchLocation = params.get('location');
      if (this.searchLocation) {
        this.performSearch(this.searchLocation);
      } else {
        this.errorMessage = 'No search location provided.';
        this.isLoading = false;
      }
    });
  }

  /**
   * Performs the hotel search based on the provided location.
   * @param location The location string to search for.
   */
  performSearch(location: string): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.searchResults = []; // Clear previous results

    this.hotelService.searchHotels(location).subscribe({
      next: (data: Hotel[]) => {
        this.searchResults = data;
        this.isLoading = false;
        if (this.searchResults.length === 0) {
          this.errorMessage = `No hotels found for "${location}".`;
        }
      },
      error: (err: HttpErrorResponse) => {
        console.error('Search failed:', err);
        this.errorMessage = `Failed to load search results: ${err.error?.message || 'Server error'}`;
        this.isLoading = false;
      }
    });
  }
}