package com.calio.identity.dto.response;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GoalResponse {
    private Long id;
    private String goalType;
    private String activityLevel;
    private BigDecimal targetWeightKg;
    private Integer dailyCalories;
    private BigDecimal proteinGrams;
    private BigDecimal carbsGrams;
    private BigDecimal fatGrams;
    private LocalDate startDate;
    private OffsetDateTime createdAt;
}
