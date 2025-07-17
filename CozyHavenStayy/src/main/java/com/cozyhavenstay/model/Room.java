package com.cozyhavenstay.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(nullable = false)
    private String roomNumber;

    @Column(name = "room_size_sqm")
    private String roomSize; // e.g., "70 m²/753 ft²"

    @Column(name = "size", nullable = true) // Added as it caused a NOT NULL error in your database
    private String size; // Another size field, if your DB has it

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BedType bedType;

    @Column(name = "max_occupancy", nullable = false) // Mapped to 'max_occupancy'
    private Integer maxOccupancy;

    @Column(name = "max_people", nullable = true) // Added as it caused a NOT NULL error in your database
    private Integer maxPeople; // Another occupancy field, if your DB has it

    @Column(nullable = false)
    private Double baseFare;

    @Column(name = "ac", nullable = true) // Mapped to 'ac' and made nullable
    private Boolean acNonAc; // true for AC, false for Non-AC

    @Column(name = "available", nullable = false) // Added as it caused a NOT NULL error in your database
    private Boolean available = true; // Default to true for new rooms

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "room_images",
                     joinColumns = @JoinColumn(name = "room_id", referencedColumnName = "id"))
    @Column(name = "image_url", length = 500)
    private List<String> imageUrls = new ArrayList<>();

    public enum BedType {
        SINGLE,
        DOUBLE,
        KING; // Ensure this is 'KING' to match frontend

        @JsonCreator
        public static BedType fromString(String value) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException("BedType string cannot be null or empty.");
            }
            for (BedType type : BedType.values()) {
                if (type.name().equalsIgnoreCase(value.trim())) {
                    return type;
                }
            }
            throw new IllegalArgumentException("No enum constant for BedType: '" + value + "'");
        }
    }
}
