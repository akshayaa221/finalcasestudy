package com.cozyhavenstay.repository;

import com.cozyhavenstay.model.Booking;
import com.cozyhavenstay.model.Booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <--- MAKE SURE THIS IMPORT IS PRESENT
import org.springframework.data.repository.query.Param; // <--- MAKE SURE THIS IMPORT IS PRESENT
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Custom query method: Find bookings by user ID (for guest's booking history)
    List<Booking> findByUserIdOrderByBookingDateDesc(Long userId);

    // Custom query method: Find bookings by room ID (for hotel owner to see bookings for a room)
    List<Booking> findByRoomIdOrderByBookingDateDesc(Long roomId);

    // Custom query method: Find bookings by hotel ID (via room, for hotel owner to see all bookings for their hotel)
    List<Booking> findByRoomHotelIdOrderByBookingDateDesc(Long hotelId);

    // Find bookings by status for a user
    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);

    // Find bookings by status for a hotel (via its rooms)
    List<Booking> findByRoomHotelIdAndStatus(Long hotelId, BookingStatus status);


    // --- ADDED/UPDATED FOR OVERLAP CHECK ---
    // Custom query to find overlapping bookings for a specific room and status.
    // This is crucial for room availability logic.
    // The overlap condition (b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate)
    // assumes checkOutDate is the departure date, and the room becomes available on that day.
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId " +
           "AND b.status IN :statuses " +
           "AND (" +
           "   (b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate)" +
           ")")
    List<Booking> findOverlappingBookingsForRoom(
        @Param("roomId") Long roomId,
        @Param("checkInDate") LocalDate checkInDate,
        @Param("checkOutDate") LocalDate checkOutDate,
        @Param("statuses") List<BookingStatus> statuses
    );

    // Removed the complex `findByRoomIdAndCheckInDateBetweenOrCheckOutDateBetween`
    // because `findOverlappingBookingsForRoom` provides more precise control for availability.
}