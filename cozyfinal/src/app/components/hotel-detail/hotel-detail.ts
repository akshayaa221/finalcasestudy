import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink, Router } from '@angular/router'; // <-- Import Router
import { HotelService, Hotel } from '@app-services/hotel';
import { RoomService, Room } from '@app-services/room';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-hotel-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './hotel-detail.html',
  styleUrl: './hotel-detail.scss'
})
export class HotelDetailComponent implements OnInit {
  hotel: Hotel | undefined;
  rooms: Room[] = [];
  isLoadingHotel: boolean = true;
  isLoadingRooms: boolean = true;
  hotelErrorMessage: string | null = null;
  roomsErrorMessage: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router, // <-- Inject Router
    private hotelService: HotelService,
    private roomService: RoomService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const hotelId = params.get('id');
      if (hotelId) {
        this.loadHotelDetails(+hotelId);
        this.loadRoomsForHotel(+hotelId);
      } else {
        this.hotelErrorMessage = 'Hotel ID not provided in the URL.';
        this.isLoadingHotel = false;
        this.isLoadingRooms = false;
      }
    });
  }

  loadHotelDetails(id: number): void {
    this.isLoadingHotel = true;
    this.hotelErrorMessage = null;
    this.hotelService.getHotelById(id).subscribe({
      next: (data) => {
        this.hotel = data;
        this.isLoadingHotel = false;
        console.log('Hotel details loaded:', this.hotel);
      },
      error: (err) => {
        console.error('Failed to load hotel details:', err);
        this.hotelErrorMessage = 'Failed to load hotel details. It might not exist or there was a server error.';
        this.isLoadingHotel = false;
      }
    });
  }

  loadRoomsForHotel(hotelId: number): void {
    this.isLoadingRooms = true;
    this.roomsErrorMessage = null;
    this.roomService.getRoomsByHotelId(hotelId).subscribe({
      next: (data) => {
        this.rooms = data;
        this.isLoadingRooms = false;
        console.log('Rooms for hotel loaded:', this.rooms);
      },
      error: (err) => {
        console.error('Failed to load rooms for hotel:', err);
        this.roomsErrorMessage = 'Failed to load rooms for this hotel.';
        this.isLoadingRooms = false;
      }
    });
  }

  bookRoom(roomId: number): void {
    console.log('Initiate booking for room ID:', roomId);
    this.router.navigate(['/book-room', roomId]); // <-- Uncommented and using router
  }
}
