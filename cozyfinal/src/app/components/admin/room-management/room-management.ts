    import { Component, OnInit } from '@angular/core';
    import { RoomService, Room, BedType } from '@app-services/room';
    import { HotelService, Hotel } from '@app-services/hotel';
    import { CommonModule } from '@angular/common';
    import { FormsModule } from '@angular/forms';
    import { HttpErrorResponse } from '@angular/common/http';

    @Component({
      selector: 'app-room-management',
      standalone: true,
      imports: [CommonModule, FormsModule],
      templateUrl: './room-management.html',
      styleUrl: './room-management.scss'
    })
    export class RoomManagementComponent implements OnInit {
      hotels: Hotel[] = [];
      selectedHotelId: number | null = null;
      rooms: Room[] = [];
      selectedRoom: Room | null = null;
      newRoom: Room = {
        roomNumber: '',
        bedType: 'SINGLE',
        maxOccupancy: 1,
        baseFare: 0,
        acNonAc: false,
        roomSize: '',
        imageUrls: [],
        hotel: null
      };

      bedTypes: BedType[] = ['SINGLE', 'DOUBLE', 'QUEEN', 'KING', 'TWIN', 'FULL', 'STUDIO'];

      isLoadingHotels: boolean = true;
      isLoadingRooms: boolean = false;
      errorMessage: string | null = null;
      successMessage: string | null = null;
      isEditing: boolean = false;

      constructor(
        private roomService: RoomService,
        private hotelService: HotelService
      ) {}

      get selectedHotelName(): string {
        const hotel = this.hotels.find(h => h.id === this.selectedHotelId);
        return hotel ? hotel.name : 'N/A';
      }

      ngOnInit(): void {
        this.loadHotels();
      }

      loadHotels(): void {
        this.isLoadingHotels = true;
        this.errorMessage = null;
        this.hotelService.getAllHotels().subscribe({
          next: (data: Hotel[]) => {
            this.hotels = data;
            this.isLoadingHotels = false;
            console.log('Hotels loaded for room management:', this.hotels);
            if (this.hotels.length > 0) {
              this.selectedHotelId = this.hotels[0].id!;
              this.onHotelSelect();
            }
          },
          error: (err: HttpErrorResponse) => {
            console.error('Failed to load hotels for room management:', err);
            this.errorMessage = 'Failed to load hotels. Cannot manage rooms without hotels.';
            this.isLoadingHotels = false;
          }
        });
      }

      onHotelSelect(): void {
        if (this.selectedHotelId) {
          this.loadRoomsForSelectedHotel(this.selectedHotelId);
          this.initiateAddRoom();
        } else {
          this.rooms = [];
          this.initiateAddRoom();
        }
      }

      loadRoomsForSelectedHotel(hotelId: number): void {
        this.isLoadingRooms = true;
        this.errorMessage = null;
        this.successMessage = null;
        this.roomService.getRoomsByHotelId(hotelId).subscribe({
          next: (data: Room[]) => {
            this.rooms = data;
            this.isLoadingRooms = false;
            console.log(`Rooms for hotel ${hotelId} loaded:`, this.rooms);
          },
          error: (err: HttpErrorResponse) => {
            console.error(`Failed to load rooms for hotel ${hotelId}:`, err);
            this.errorMessage = `Failed to load rooms for selected hotel: ${err.error?.message || 'Server error'}`;
            this.isLoadingRooms = false;
          }
        });
      }

      initiateAddRoom(): void {
        this.selectedRoom = null;
        this.newRoom = {
          roomNumber: '',
          bedType: 'SINGLE',
          maxOccupancy: 1,
          baseFare: 0,
          acNonAc: false,
          roomSize: '',
          imageUrls: [],
          hotel: null
        };
        this.isEditing = false;
        this.errorMessage = null;
        this.successMessage = null;
      }

      initiateEditRoom(room: Room): void {
        this.selectedRoom = { ...room };
        this.newRoom = { ...room };
        this.newRoom.imageUrls = room.imageUrls ? [...room.imageUrls] : [];
        this.isEditing = true;
        this.errorMessage = null;
        this.successMessage = null;
      }

      getImageUrlsAsString(): string {
        return this.newRoom.imageUrls ? this.newRoom.imageUrls.join(', ') : '';
      }

      setImageUrlsFromString(event: Event): void {
        const inputElement = event.target as HTMLInputElement;
        const value = inputElement.value;
        this.newRoom.imageUrls = value ? value.split(',').map(url => url.trim()).filter(url => url.length > 0) : [];
      }

      saveRoom(): void {
        this.errorMessage = null;
        this.successMessage = null;

        if (!this.selectedHotelId) {
          this.errorMessage = 'Please select a hotel first.';
          return;
        }

        this.newRoom.hotel = { id: this.selectedHotelId } as any;

        if (this.isEditing && this.newRoom.id) {
          this.roomService.updateRoom(this.newRoom.id, this.newRoom).subscribe({
            next: (updatedRoom) => {
              this.successMessage = `Room '${updatedRoom.roomNumber}' updated successfully!`;
              console.log('Room updated:', updatedRoom);
              this.loadRoomsForSelectedHotel(this.selectedHotelId!);
              this.initiateAddRoom();
            },
            error: (err: HttpErrorResponse) => {
              console.error('Failed to update room:', err);
              this.errorMessage = `Failed to update room: ${err.error?.message || 'Server error'}`;
            }
          });
        } else {
          // Create new room: Pass selectedHotelId to the service
          this.roomService.createRoom(this.selectedHotelId!, this.newRoom).subscribe({ // <-- This method call changed
            next: (createdRoom) => {
              this.successMessage = `Room '${createdRoom.roomNumber}' added successfully!`;
              console.log('Room created:', createdRoom);
              this.loadRoomsForSelectedHotel(this.selectedHotelId!);
              this.initiateAddRoom();
            },
            error: (err: HttpErrorResponse) => {
              console.error('Failed to create room:', err);
              this.errorMessage = `Failed to create room: ${err.error?.message || 'Server error'}`;
            }
          });
        }
      }

      deleteRoom(id: number, roomNumber: string): void {
        if (confirm(`Are you sure you want to delete room '${roomNumber}'? This action cannot be undone.`)) {
          this.roomService.deleteRoom(id).subscribe({
            next: () => {
              this.successMessage = `Room '${roomNumber}' deleted successfully!`;
              console.log('Room deleted:', roomNumber);
              this.loadRoomsForSelectedHotel(this.selectedHotelId!);
              this.initiateAddRoom();
            },
            error: (err: HttpErrorResponse) => {
              console.error('Failed to delete room:', err);
              this.errorMessage = `Failed to delete room: ${err.error?.message || 'Server error'}`;
            }
          });
        }
      }
    }
    