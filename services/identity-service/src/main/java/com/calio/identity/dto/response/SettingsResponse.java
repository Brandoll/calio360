package com.calio.identity.dto.response;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SettingsResponse {
    private String unitSystem;
    private String language;
    private Boolean notificationsEnabled;
}
