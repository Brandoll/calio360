package com.calio.tracking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class DailySummaryResponse {
    private int totalCalorias;
    private BigDecimal totalProteinas;
    private BigDecimal totalGrasas;
    private BigDecimal totalCarbohidratos;
    private int aguaVasos;
    private List<MealRecordDto> comidas;
}
