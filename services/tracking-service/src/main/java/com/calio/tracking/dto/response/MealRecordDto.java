package com.calio.tracking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealRecordDto {
    private Long id;
    private Long userId;
    private Long alimentoId;
    private String nombre;
    private Integer porcionGramos;
    private String momento;
    private String imageUrl;
    private LocalDate fecha;
    private Integer calorias;
    private BigDecimal proteinas;
    private BigDecimal grasas;
    private BigDecimal carbohidratos;
}
