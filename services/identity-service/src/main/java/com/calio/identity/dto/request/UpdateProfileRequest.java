package com.calio.identity.dto.request;
import lombok.Data;
import java.time.LocalDate;
@Data
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String gender;
    private String unitSystem;
    private String language;
    private Boolean notificationsEnabled;
}
