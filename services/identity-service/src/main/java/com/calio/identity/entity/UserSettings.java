package com.calio.identity.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "user_settings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserSettings {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    @Column(name = "unit_system", nullable = false, length = 10)
    @Builder.Default private String unitSystem = "METRIC";
    @Column(nullable = false, length = 10)
    @Builder.Default private String language = "es-PE";
    @Column(name = "notifications_enabled", nullable = false)
    @Builder.Default private Boolean notificationsEnabled = true;
}
