package com.calio.wearable.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wearable_syncs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WearableSync {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer steps;

    @Column(name = "calories_burned", nullable = false)
    private Integer caloriesBurned;

    @Column(name = "distance_km", precision = 5, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "sync_date", nullable = false)
    private LocalDateTime syncDate;
}
