package com.calio.exercise.service;

import com.calio.exercise.model.Ejercicio;
import org.springframework.stereotype.Service;

@Service
public class CalorieEstimatorService {

    public int estimateCalories(Ejercicio ejercicio, int series, int descansoSegundos) {
        // formula: duracionEstimadaMinutos = series * ((30 + descansoSegundos) / 60.0)
        // calorias = duracionEstimadaMinutos * calorias_por_minuto
        double duracionEstimadaMinutos = series * ((30.0 + descansoSegundos) / 60.0);
        double cal = duracionEstimadaMinutos * ejercicio.getCaloriasPorMinuto().doubleValue();
        return (int) Math.round(cal);
    }
}
