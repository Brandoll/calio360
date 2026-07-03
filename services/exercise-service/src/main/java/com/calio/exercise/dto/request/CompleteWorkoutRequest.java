package com.calio.exercise.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CompleteWorkoutRequest {
    private String diaEntrenamiento;
    private LocalDate fecha;
}
