package com.calio.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity @Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    @Column(name = "birth_date")
    private LocalDate birthDate;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
    @Column(nullable = false) @Builder.Default private Boolean active = true;
    @Column(name = "email_verified", nullable = false) @Builder.Default private Boolean emailVerified = false;
    @PrePersist void prePersist() { this.createdAt = OffsetDateTime.now(); }
    @PreUpdate void preUpdate() { this.updatedAt = OffsetDateTime.now(); }
    public enum Gender { MALE, FEMALE, OTHER }
}
