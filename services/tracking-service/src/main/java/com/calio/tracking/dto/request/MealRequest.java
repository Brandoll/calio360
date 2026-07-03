package com.calio.tracking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MealRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Long alimentoId;
    @NotNull
    private Integer porcionGramos;
    @NotNull
    private String momento;
    @NotNull
    private LocalDate fecha;
    @NotNull
    private Integer calorias;
    @NotNull
    private BigDecimal proteinas;
    @NotNull
    private BigDecimal grasas;
    @NotNull
    private BigDecimal carbohidratos;
}
