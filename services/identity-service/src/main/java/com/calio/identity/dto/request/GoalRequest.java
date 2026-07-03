package com.calio.identity.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class GoalRequest {
    @NotBlank(message = "El tipo de objetivo es requerido")
    private String goalType;      // LOSE_WEIGHT, GAIN_MUSCLE, MAINTAIN, EAT_HEALTHY

    @NotBlank(message = "El nivel de actividad es requerido")
    private String activityLevel; // SEDENTARY, LIGHT, MODERATE, ACTIVE, VERY_ACTIVE

    @DecimalMin(value = "30.0", message = "El peso objetivo mínimo es 30 kg")
    @DecimalMax(value = "300.0", message = "El peso objetivo máximo es 300 kg")
    private BigDecimal targetWeightKg;

    @Min(value = 1000, message = "Las calorías mínimas son 1000")
    @Max(value = 6000, message = "Las calorías máximas son 6000")
    private Integer dailyCalories; // Opcional: si no se envía, se calcula automáticamente

    private BigDecimal proteinGrams;
    private BigDecimal carbsGrams;
    private BigDecimal fatGrams;
}
