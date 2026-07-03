package com.calio.wearable.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WearableSyncRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Integer steps;
    @NotNull
    private Integer caloriesBurned;
    @NotNull
    private BigDecimal distanceKm;
}
