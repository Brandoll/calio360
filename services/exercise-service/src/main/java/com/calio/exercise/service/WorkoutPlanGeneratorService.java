package com.calio.exercise.service;

import com.calio.exercise.config.WorkoutRulesConfig;
import com.calio.exercise.model.Ejercicio;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkoutPlanGeneratorService {

    private final ExerciseCatalogService catalogService;
    private final ObjectMapper objectMapper;

    public String generarRutinaJson(String objetivo, int diasPorSemana, List<String> equipoDisponible, String nivelActividad) {
        
        int diasAjustados = diasPorSemana;
        if (diasAjustados < 3) diasAjustados = 3;
        if (diasAjustados > 6) diasAjustados = 6;
        
        Map<String, List<String>> split = WorkoutRulesConfig.SPLIT_TEMPLATES.get(diasAjustados);
        WorkoutRulesConfig.ObjectiveRules rules = WorkoutRulesConfig.OBJECTIVE_RULES.getOrDefault(objetivo.toLowerCase(), WorkoutRulesConfig.OBJECTIVE_RULES.get("mantenimiento"));
        String dificultad = WorkoutRulesConfig.getDificultadFromActividad(nivelActividad);
        
        // Estructura para armar el JSON final
        Map<String, Object> rutinaMap = new LinkedHashMap<>();
        rutinaMap.put("objetivo", objetivo);
        rutinaMap.put("diasPorSemana", diasAjustados);
        
        List<Map<String, Object>> diasList = new ArrayList<>();
        
        List<String> equiposBusqueda = new ArrayList<>(equipoDisponible);
        if (!equiposBusqueda.contains("ninguno")) equiposBusqueda.add("ninguno");

        for (Map.Entry<String, List<String>> entry : split.entrySet()) {
            String diaKey = entry.getKey();
            List<String> musculos = entry.getValue();
            
            Map<String, Object> diaDef = new LinkedHashMap<>();
            diaDef.put("dia", diaKey);
            diaDef.put("enfoque", String.join(" + ", musculos));
            
            List<Map<String, Object>> ejerciciosDia = new ArrayList<>();
            for (String musculo : musculos) {
                List<Ejercicio> recomendados = catalogService.findEjerciciosParaGeneracion(musculo, equiposBusqueda, dificultad, WorkoutRulesConfig.EJERCICIOS_POR_GRUPO);
                for (Ejercicio ej : recomendados) {
                    Map<String, Object> ejMap = new LinkedHashMap<>();
                    ejMap.put("ejercicioId", ej.getId());
                    ejMap.put("nombre", ej.getNombre());
                    ejMap.put("grupoMuscular", ej.getGrupoMuscular());
                    ejMap.put("gifUrl", ej.getGifUrl());
                    ejMap.put("series", rules.getSeries());
                    ejMap.put("repeticiones", rules.getRepeticiones());
                    ejMap.put("descansoSegundos", rules.getDescansoSegundos());
                    ejerciciosDia.add(ejMap);
                }
            }
            diaDef.put("ejercicios", ejerciciosDia);
            diasList.add(diaDef);
        }
        
        rutinaMap.put("dias", diasList);
        
        try {
            return objectMapper.writeValueAsString(rutinaMap);
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar rutina JSON", e);
        }
    }
}
