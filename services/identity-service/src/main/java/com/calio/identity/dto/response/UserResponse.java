package com.calio.identity.dto.response;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String gender;
    private OffsetDateTime createdAt;
    private Boolean active;
    private Boolean emailVerified;
}
