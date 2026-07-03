package com.calio.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity @Table(name = "biometric_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BiometricRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;
    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;
    @Column(name = "body_fat_pct", precision = 4, scale = 1)
    private BigDecimal bodyFatPct;
    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;
    @PrePersist void prePersist() { this.recordedAt = OffsetDateTime.now(); }
}
