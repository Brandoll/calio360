package com.calio.identity.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class BiometricRequest {
    @NotNull @DecimalMin("20.0") @DecimalMax("500.0")
    private BigDecimal weightKg;
    @NotNull @DecimalMin("50.0") @DecimalMax("300.0")
    private BigDecimal heightCm;
    @DecimalMin("1.0") @DecimalMax("70.0")
    private BigDecimal bodyFatPct;
}
