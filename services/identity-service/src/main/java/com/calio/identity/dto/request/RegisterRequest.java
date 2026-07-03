package com.calio.identity.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
@Data
public class RegisterRequest {
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    private LocalDate birthDate;
    private String gender; // MALE, FEMALE, OTHER
}
