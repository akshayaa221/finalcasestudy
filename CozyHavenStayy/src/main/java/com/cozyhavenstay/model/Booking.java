package com.cozyhavenstay.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // Foreign key to User (the guest)
    private User user;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false) // Foreign key to Room
    private Room room;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = false)
    private Integer numberOfRooms;

    @Column(nullable = false)
    private Integer numberOfAdults;

    private Integer numberOfChildren;

    @Column(name = "people_count", nullable = false) // This maps to the database column 'people_count'
    private Integer peopleCount; // This field will store the total (adults + children)

    @Column(nullable = false)
    private Double totalFare;

    @Column(nullable = false)
    private LocalDateTime bookingDate; // When the booking was made

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    // Enum for Booking Status
    public enum BookingStatus {
        PENDING,
        CONFIRMED,
        CANCELLED,
        COMPLETED
    }
}
