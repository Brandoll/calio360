package com.calio.exercise.controller;

import com.calio.exercise.model.Ejercicio;
import com.calio.exercise.service.ExerciseCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseCatalogService catalogService;

    @GetMapping
    public ResponseEntity<List<Ejercicio>> getExercises(
            @RequestParam(required = false) String grupoMuscular,
            @RequestParam(required = false) String equipo,
            @RequestParam(required = false) String dificultad) {
        return ResponseEntity.ok(catalogService.getEjercicios(grupoMuscular, equipo, dificultad));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ejercicio> getExerciseById(@PathVariable Long id) {
        return ResponseEntity.ok(catalogService.getEjercicioById(id));
    }
}
