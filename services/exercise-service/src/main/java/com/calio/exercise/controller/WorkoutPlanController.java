package com.calio.exercise.controller;

import com.calio.exercise.dto.request.CompleteWorkoutRequest;
import com.calio.exercise.dto.request.GenerateWorkoutRequest;
import com.calio.exercise.model.Rutina;
import com.calio.exercise.model.RutinaCompletada;
import com.calio.exercise.repository.RutinaCompletadaRepository;
import com.calio.exercise.repository.RutinaRepository;
import com.calio.exercise.service.WorkoutPlanGeneratorService;
import com.calio.exercise.client.IdentityServiceClient;
import com.calio.exercise.messaging.RutinaCompletadaPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workout-plan")
@RequiredArgsConstructor
public class WorkoutPlanController {

    private final WorkoutPlanGeneratorService generatorService;
    private final RutinaRepository rutinaRepository;
    private final RutinaCompletadaRepository completadaRepository;
    private final IdentityServiceClient identityClient;
    private final RutinaCompletadaPublisher publisher;
    private final ObjectMapper objectMapper;

    @PostMapping("/generate")
    public ResponseEntity<Rutina> generateWorkoutPlan(@RequestBody GenerateWorkoutRequest request) {
        // Consultar identity-service
        Map<String, Object> profile = identityClient.getProfile(request.getUserId());
        String objetivo = (String) profile.getOrDefault("objetivo", "mantenimiento");
        String nivelActividad = (String) profile.getOrDefault("nivelActividad", "moderado");

        String rutinaJson = generatorService.generarRutinaJson(
                objetivo,
                request.getDiasPorSemana(),
                request.getEquipoDisponible(),
                nivelActividad
        );

        Rutina rutina = new Rutina();
        rutina.setUserId(request.getUserId());
        rutina.setSemana(LocalDate.now());
        rutina.setObjetivo(objetivo);
        rutina.setDiasPorSemana(request.getDiasPorSemana());
        try {
            rutina.setEquipoDisponible(objectMapper.writeValueAsString(request.getEquipoDisponible()));
        } catch (Exception e) {}
        rutina.setRutinaJson(rutinaJson);

        return ResponseEntity.ok(rutinaRepository.save(rutina));
    }

    @GetMapping("/{userId}/current")
    public ResponseEntity<Rutina> getCurrentWorkoutPlan(@PathVariable Long userId) {
        List<Rutina> rutinas = rutinaRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (rutinas.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(rutinas.get(0));
    }

    @GetMapping("/{userId}/history")
    public ResponseEntity<List<Rutina>> getWorkoutHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(rutinaRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @PostMapping("/{rutinaId}/complete")
    public ResponseEntity<Map<String, Object>> completeWorkout(
            @PathVariable Long rutinaId,
            @RequestBody CompleteWorkoutRequest request) {
        
        Rutina rutina = rutinaRepository.findById(rutinaId)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada"));

        // Lógica de cálculo de calorías simulada temporalmente o extraer del JSON
        // Por simplicidad para cumplir la firma:
        int caloriasQuemadas = 280; // Debería extraerse del día específico en rutina.rutinaJson

        RutinaCompletada completada = new RutinaCompletada();
        completada.setRutina(rutina);
        completada.setUserId(rutina.getUserId());
        completada.setFecha(request.getFecha());
        completada.setDiaEntrenamiento(request.getDiaEntrenamiento());
        completada.setCaloriasQuemadasEstimadas(caloriasQuemadas);
        completadaRepository.save(completada);

        publisher.publishRutinaCompletada(rutina.getUserId(), request.getFecha().toString(), caloriasQuemadas);

        return ResponseEntity.ok(Map.of("status", "success", "caloriasQuemadas", caloriasQuemadas));
    }
}
