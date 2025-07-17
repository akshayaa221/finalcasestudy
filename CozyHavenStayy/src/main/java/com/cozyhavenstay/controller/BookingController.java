package com.cozyhavenstay.controller;

import com.cozyhavenstay.model.Booking;
import com.cozyhavenstay.service.BookingService;
import com.cozyhavenstay.service.HotelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.cozyhavenstay.security.services.UserDetailsImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/bookings")
@CrossOrigin(origins = "http://localhost:4200")
public class BookingController {

    private final BookingService bookingService;
    private final HotelService hotelService;

    public BookingController(BookingService bookingService, HotelService hotelService) {
        this.bookingService = bookingService;
        this.hotelService = hotelService;
    }

    @PostMapping
    @PreAuthorize("hasRole('GUEST') or hasRole('ADMIN')") // Allow admin to create bookings too
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        // Ensure the booking is for the authenticated user unless it's an ADMIN
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && (booking.getUser() == null || !booking.getUser().getId().equals(authenticatedUserId))) {
            return new ResponseEntity<>("Forbidden: Cannot create booking for another user. User ID must match authenticated user.", HttpStatus.FORBIDDEN);
        }

        try {
            Booking newBooking = bookingService.createBooking(booking);
            return new ResponseEntity<>(newBooking, HttpStatus.CREATED);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create booking: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('GUEST') and (#bookingService.getBookingById(#id).isPresent() ? #bookingService.getBookingById(#id).get().getUser().getId() == authentication.principal.id : false)) or " +
                  "(hasRole('HOTEL_OWNER') and (#bookingService.getBookingById(#id).isPresent() ? #bookingService.getBookingById(#id).get().getRoom().getHotel().getOwner().getId() == authentication.principal.id : false))")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        Optional<Booking> bookingOptional = bookingService.getBookingById(id);
        if (bookingOptional.isEmpty()) {
            return new ResponseEntity<>("Booking not found with id: " + id, HttpStatus.NOT_FOUND);
        }

        Booking booking = bookingOptional.get();

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isGuestOwner = booking.getUser().getId().equals(authenticatedUserId) && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_GUEST"));
        boolean isHotelOwner = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_HOTEL_OWNER")) &&
                               booking.getRoom().getHotel().getOwner().getId().equals(authenticatedUserId);

        if (isAdmin || isGuestOwner || isHotelOwner) {
            return new ResponseEntity<>(booking, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Forbidden: You are not authorized to view this booking.", HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('GUEST') and #userId == authentication.principal.id)")
    public ResponseEntity<?> getUserBookingHistory(@PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ||
            userId.equals(authenticatedUserId)) {
            List<Booking> bookings = bookingService.getUserBookingHistory(userId);
            return new ResponseEntity<>(bookings, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Forbidden: You can only view your own booking history.", HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/hotel/{hotelId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('HOTEL_OWNER') and (#hotelService.getHotelById(#hotelId).isPresent() ? #hotelService.getHotelById(#hotelId).get().getOwner().getId() == authentication.principal.id : false))")
    public ResponseEntity<?> getHotelBookings(@PathVariable Long hotelId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isHotelOwner = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_HOTEL_OWNER")) &&
                               hotelService.getHotelById(hotelId).map(h -> h.getOwner().getId()).orElse(0L).equals(authenticatedUserId);

        if (isAdmin || isHotelOwner) {
            List<Booking> bookings = bookingService.getHotelBookings(hotelId);
            return new ResponseEntity<>(bookings, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Forbidden: You are not authorized to view bookings for this hotel.", HttpStatus.FORBIDDEN);
        }
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('GUEST') and (#bookingService.getBookingById(#id).isPresent() ? #bookingService.getBookingById(#id).get().getUser().getId() == authentication.principal.id : false)) or " +
                  "(hasRole('HOTEL_OWNER') and (#bookingService.getBookingById(#id).isPresent() ? #bookingService.getBookingById(#id).get().getRoom().getHotel().getOwner().getId() == authentication.principal.id : false))")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long authenticatedUserId = ((UserDetailsImpl) authentication.getPrincipal()).getId();

        Optional<Booking> bookingOptional = bookingService.getBookingById(id);
        if (bookingOptional.isEmpty()) {
            return new ResponseEntity<>("Booking not found with id: " + id, HttpStatus.NOT_FOUND);
        }

        Booking booking = bookingOptional.get();

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isGuestOwner = booking.getUser().getId().equals(authenticatedUserId) && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_GUEST"));
        boolean isHotelOwner = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_HOTEL_OWNER")) &&
                               booking.getRoom().getHotel().getOwner().getId().equals(authenticatedUserId);

        if (isAdmin || isGuestOwner || isHotelOwner) {
            try {
                Booking cancelledBooking = bookingService.cancelBooking(id);
                return new ResponseEntity<>(cancelledBooking, HttpStatus.OK);
            } catch (IllegalArgumentException | IllegalStateException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            } catch (Exception e) {
                return new ResponseEntity<>("Failed to cancel booking: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Forbidden: You are not authorized to cancel this booking.", HttpStatus.FORBIDDEN);
        }
    }
}
