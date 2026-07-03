package com.calio.exercise.config;

import lombok.Data;
import java.util.List;
import java.util.Map;

public class WorkoutRulesConfig {

    public static final Map<Integer, Map<String, List<String>>> SPLIT_TEMPLATES = Map.of(
        3, Map.of(
            "dia1", List.of("pecho", "espalda", "piernas"),
            "dia2", List.of("hombros", "brazos", "core"),
            "dia3", List.of("pecho", "espalda", "piernas")
        ),
        4, Map.of(
            "dia1", List.of("pecho", "espalda", "hombros", "brazos"),
            "dia2", List.of("piernas", "core"),
            "dia3", List.of("pecho", "espalda", "hombros", "brazos"),
            "dia4", List.of("piernas", "core")
        ),
        5, Map.of(
            "dia1", List.of("pecho"),
            "dia2", List.of("espalda"),
            "dia3", List.of("piernas"),
            "dia4", List.of("hombros", "core"),
            "dia5", List.of("brazos", "core")
        ),
        6, Map.of(
            "dia1", List.of("pecho", "hombros", "brazos"),
            "dia2", List.of("espalda", "brazos"),
            "dia3", List.of("piernas", "core"),
            "dia4", List.of("pecho", "hombros", "brazos"),
            "dia5", List.of("espalda", "brazos"),
            "dia6", List.of("piernas", "core")
        )
    );

    @Data
    public static class ObjectiveRules {
        private final int series;
        private final String repeticiones;
        private final int descansoSegundos;
    }

    public static final Map<String, ObjectiveRules> OBJECTIVE_RULES = Map.of(
        "perder peso", new ObjectiveRules(3, "15-20", 30),
        "ganar musculo", new ObjectiveRules(4, "8-12", 90),
        "mantenimiento", new ObjectiveRules(3, "10-15", 60)
    );

    public static final int EJERCICIOS_POR_GRUPO = 3;

    // Traducir nivel actividad a dificultad
    public static String getDificultadFromActividad(String nivelActividad) {
        if (nivelActividad == null) return "intermedio";
        return switch (nivelActividad.toLowerCase()) {
            case "sedentario", "ligero" -> "principiante";
            case "moderado" -> "intermedio";
            case "activo", "muy_activo" -> "avanzado";
            default -> "intermedio";
        };
    }
}
