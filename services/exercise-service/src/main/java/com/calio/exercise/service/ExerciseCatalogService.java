package com.calio.exercise.service;

import com.calio.exercise.model.Ejercicio;
import com.calio.exercise.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseCatalogService {

    private final ExerciseRepository repository;

    public List<Ejercicio> getEjercicios(String grupoMuscular, String equipo, String dificultad) {
        return repository.findEjerciciosFiltrados(grupoMuscular, equipo, dificultad);
    }

    public Ejercicio getEjercicioById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Ejercicio no encontrado"));
    }

    public List<Ejercicio> findEjerciciosParaGeneracion(String grupoMuscular, List<String> equipos, String dificultad, int maxEjercicios) {
        List<Ejercicio> results = repository.findByGrupoAndEquiposAndDificultad(grupoMuscular, equipos, dificultad);

        // Relajación de filtros si no hay suficientes
        if (results.size() < maxEjercicios) {
            String dificultadInferior = relajarDificultad(dificultad);
            if (!dificultadInferior.equals(dificultad)) {
                List<Ejercicio> relaxedDif = repository.findByGrupoAndEquiposAndDificultad(grupoMuscular, equipos, dificultadInferior);
                results.addAll(relaxedDif);
                results = results.stream().distinct().collect(Collectors.toList());
            }
        }

        if (results.size() < maxEjercicios && !equipos.contains("ninguno")) {
            equipos.add("ninguno");
            List<Ejercicio> relaxedEq = repository.findByGrupoAndEquiposAndDificultad(grupoMuscular, equipos, "principiante"); // Fallback
            results.addAll(relaxedEq);
            results = results.stream().distinct().collect(Collectors.toList());
        }

        Collections.shuffle(results);
        return results.stream().limit(maxEjercicios).collect(Collectors.toList());
    }

    private String relajarDificultad(String dificultad) {
        return switch (dificultad) {
            case "avanzado" -> "intermedio";
            case "intermedio" -> "principiante";
            default -> "principiante";
        };
    }
}
