package com.calio.tracking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class WaterRequest {
    @NotNull
    private Long userId;
    @NotNull
    private LocalDate fecha;
    @NotNull
    private Integer vasos;
}
