package com.calio.identity.dto.response;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BiometricResponse {
    private Long id;
    private BigDecimal weightKg;
    private BigDecimal heightCm;
    private BigDecimal bodyFatPct;
    private BigDecimal bmi;
    private OffsetDateTime recordedAt;
}
