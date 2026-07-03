package com.calio.exercise.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class GenerateWorkoutRequest {
    private Long userId;
    private Integer diasPorSemana;
    private List<String> equipoDisponible;
}
