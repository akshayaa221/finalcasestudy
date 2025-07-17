package com.cozyhavenstay.service;

import com.cozyhavenstay.model.Booking;
import com.cozyhavenstay.model.Booking.BookingStatus;
import com.cozyhavenstay.model.Room;
import com.cozyhavenstay.model.User;
import com.cozyhavenstay.repository.BookingRepository;
import com.cozyhavenstay.repository.RoomRepository;
import com.cozyhavenstay.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit; // Added for calculating days
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public BookingService(BookingRepository bookingRepository, UserRepository userRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    public Booking createBooking(Booking booking) {
        // Validate User and Room existence
        User user = userRepository.findById(booking.getUser().getId())
                                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + booking.getUser().getId()));
        Room room = roomRepository.findById(booking.getRoom().getId())
                                .orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + booking.getRoom().getId()));

        // Basic Date Validation
        if (booking.getCheckInDate().isAfter(booking.getCheckOutDate()) ||
            booking.getCheckInDate().isBefore(LocalDate.now())) { // Check if check-in is not in the past
            throw new IllegalArgumentException("Invalid check-in/check-out dates.");
        }

        // --- IMPORTANT: Room Availability Logic ---
        if (!isRoomAvailable(room.getId(), booking.getCheckInDate(), booking.getCheckOutDate())) {
            throw new IllegalStateException("Room is not available for the selected dates.");
        }

        booking.setUser(user); // Set managed entities
        booking.setRoom(room);
        booking.setBookingDate(LocalDateTime.now()); // Set booking timestamp
        booking.setStatus(BookingStatus.PENDING); // Initial status

        // --- CRUCIAL ADDITION: Set peopleCount ---
        // Ensure numberOfChildren is treated as 0 if null, for calculation
        int children = (booking.getNumberOfChildren() != null) ? booking.getNumberOfChildren() : 0;
        booking.setPeopleCount(booking.getNumberOfAdults() + children);

        booking.setTotalFare(calculateTotalFare(
            room.getBaseFare(),
            booking.getNumberOfAdults(),
            children, // Use the potentially adjusted children count
            room.getBedType(),
            booking.getNumberOfRooms(),
            booking.getCheckInDate(), // Pass dates for fare calculation
            booking.getCheckOutDate()
        ));

        return bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public List<Booking> getUserBookingHistory(Long userId) {
        return bookingRepository.findByUserIdOrderByBookingDateDesc(userId);
    }

    public List<Booking> getHotelBookings(Long hotelId) {
        return bookingRepository.findByRoomHotelIdOrderByBookingDateDesc(hotelId);
    }

    public Booking cancelBooking(Long id) {
        return bookingRepository.findById(id).map(booking -> {
            if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.PENDING) {
                booking.setStatus(BookingStatus.CANCELLED);
                // Implement refund logic here later if applicable (e.g., mark for refund)
                return bookingRepository.save(booking);
            } else {
                throw new IllegalStateException("Booking cannot be cancelled from status: " + booking.getStatus());
            }
        }).orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + id));
    }

    // Helper method for room availability check (simplified)
    public boolean isRoomAvailable(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        // Define statuses that count as "unavailable"
        List<BookingStatus> unavailableStatuses = Arrays.asList(BookingStatus.CONFIRMED, BookingStatus.PENDING);

        // Use the custom query to find overlapping bookings
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookingsForRoom(
            roomId, checkInDate, checkOutDate, unavailableStatuses
        );

        return overlappingBookings.isEmpty();
    }

    // Calculation logic as per your problem statement
    private Double calculateTotalFare(Double baseFare, Integer adults, Integer children, Room.BedType bedType, Integer numberOfRooms, LocalDate checkInDate, LocalDate checkOutDate) {
        long numberOfNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (numberOfNights <= 0) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }

        double totalFarePerRoom = baseFare; // Start with base fare for one room

        // Ensure children count is not null for calculation
        int actualChildren = (children != null) ? children : 0;
        int actualTotalGuests = adults + actualChildren;

        double fareForGuests = 0.0;
        if (bedType == Room.BedType.SINGLE) {
            if (actualTotalGuests > 1) {
                // If there's at least one adult and total guests are 2, the 2nd person is adult (40%)
                // Otherwise, if total guests is 2 and the 2nd is child (20%)
                fareForGuests += (adults >= 2) ? (baseFare * 0.40) : (baseFare * 0.20);
            }
        } else if (bedType == Room.BedType.DOUBLE) {
            if (actualTotalGuests > 2) {
                int extra = actualTotalGuests - 2;
                int remainingAdults = adults - 2; // Adults already covered by base fare

                // Charge for extra adults first (up to extra limit)
                int chargedAdults = Math.min(extra, Math.max(0, remainingAdults));
                fareForGuests += chargedAdults * baseFare * 0.40;

                // Charge for remaining extra guests as children (those not covered by adults)
                // This implicitly covers what 'extraChildren' was meant to track.
                fareForGuests += Math.max(0, extra - chargedAdults) * baseFare * 0.20;
            }
        } else if (bedType == Room.BedType.KING) {
            if (actualTotalGuests > 4) {
                int extra = actualTotalGuests - 4;
                int remainingAdults = adults - 4; // Adults already covered by base fare

                // Charge for extra adults first (up to extra limit)
                int chargedAdults = Math.min(extra, Math.max(0, remainingAdults));
                fareForGuests += chargedAdults * baseFare * 0.40;

                // Charge for remaining extra guests as children
                // This implicitly covers what 'extraChildren' was meant to track.
                fareForGuests += Math.max(0, extra - chargedAdults) * baseFare * 0.20;
            }
        }

        totalFarePerRoom = baseFare + fareForGuests;

        return totalFarePerRoom * numberOfRooms * numberOfNights; // Multiply by number of nights and rooms
    }
}
