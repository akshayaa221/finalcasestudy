import { Component, OnInit } from '@angular/core';
import { HotelService, Hotel } from '@app-services/hotel';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-hotel-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './hotel-management.html',
  styleUrl: './hotel-management.scss'
})
export class HotelManagementComponent implements OnInit {
  hotels: Hotel[] = [];
  selectedHotel: Hotel | null = null;
  newHotel: Hotel = {
    name: '',
    location: '',
    description: '',
    amenities: '',
    imageUrls: [],
    rating: 0 // <-- ADDED: Default rating
  };

  isLoading: boolean = true;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  isEditing: boolean = false;

  constructor(private hotelService: HotelService) {}

  ngOnInit(): void {
    this.loadHotels();
  }

  loadHotels(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.hotelService.getAllHotels().subscribe({
      next: (data: Hotel[]) => {
        this.hotels = data;
        this.isLoading = false;
        console.log('Hotels loaded:', this.hotels);
      },
      error: (err: HttpErrorResponse) => {
        console.error('Failed to load hotels:', err);
        this.errorMessage = `Failed to load hotels: ${err.error?.message || 'Server error'}`;
        this.isLoading = false;
      }
    });
  }

  initiateAddHotel(): void {
    this.selectedHotel = null;
    this.newHotel = {
      name: '',
      location: '',
      description: '',
      amenities: '',
      imageUrls: [],
      rating: 0 // <-- ADDED: Default rating
    };
    this.isEditing = false;
    this.errorMessage = null;
    this.successMessage = null;
  }

  initiateEditHotel(hotel: Hotel): void {
    this.selectedHotel = { ...hotel };
    this.newHotel = {
      ...hotel,
      imageUrls: hotel.imageUrls ? [...hotel.imageUrls] : [], // Deep copy imageUrls
      rating: hotel.rating // <-- ADDED: Copy rating
    };
    this.isEditing = true;
    this.errorMessage = null;
    this.successMessage = null;
  }

  getImageUrlsAsString(): string {
    return this.newHotel.imageUrls ? this.newHotel.imageUrls.join(', ') : '';
  }

  setImageUrlsFromString(event: Event): void {
    const inputElement = event.target as HTMLInputElement;
    const value = inputElement.value;
    this.newHotel.imageUrls = value ? value.split(',').map(url => url.trim()).filter(url => url.length > 0) : [];
  }

  saveHotel(): void {
    this.errorMessage = null;
    this.successMessage = null;

    if (this.isEditing && this.newHotel.id) {
      this.hotelService.updateHotel(this.newHotel.id, this.newHotel).subscribe({
        next: (updatedHotel: Hotel) => { // <-- ADDED TYPE
          this.successMessage = `Hotel '${updatedHotel.name}' updated successfully!`;
          console.log('Hotel updated:', updatedHotel);
          this.loadHotels();
          this.initiateAddHotel();
        },
        error: (err: HttpErrorResponse) => {
          console.error('Failed to update hotel:', err);
          this.errorMessage = `Failed to update hotel: ${err.error?.message || 'Server error'}`;
        }
      });
    } else {
      // Changed 'createHotel' to 'addHotel' and explicitly typed 'createdHotel'
      this.hotelService.addHotel(this.newHotel).subscribe({
        next: (createdHotel: Hotel) => { // <-- FIXED: Changed method to addHotel and added type
          this.successMessage = `Hotel '${createdHotel.name}' added successfully!`;
          console.log('Hotel created:', createdHotel);
          this.loadHotels();
          this.initiateAddHotel();
        },
        error: (err: HttpErrorResponse) => {
          console.error('Failed to create hotel:', err);
          this.errorMessage = `Failed to create hotel: ${err.error?.message || 'Server error'}`;
        }
      });
    }
  }

  deleteHotel(id: number, name: string): void {
    if (confirm(`Are you sure you want to delete hotel '${name}'? This action cannot be undone.`)) {
      this.hotelService.deleteHotel(id).subscribe({
        next: () => {
          this.successMessage = `Hotel '${name}' deleted successfully!`;
          console.log('Hotel deleted:', name);
          this.loadHotels();
          this.initiateAddHotel();
        },
        error: (err: HttpErrorResponse) => {
          console.error('Failed to delete hotel:', err);
          this.errorMessage = `Failed to delete hotel: ${err.error?.message || 'Server error'}`;
        }
      });
    }
  }
}
