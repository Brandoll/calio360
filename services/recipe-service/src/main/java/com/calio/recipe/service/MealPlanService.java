package com.calio.recipe.service;

import com.calio.recipe.client.FoodCatalogClient;
import com.calio.recipe.client.GeminiClient;
import com.calio.recipe.dto.request.GenerateMealPlanRequest;
import com.calio.recipe.model.MealPlan;
import com.calio.recipe.repository.MealPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final FoodCatalogClient catalogClient;
    private final GeminiClient geminiClient;

    @Transactional
    public MealPlan generateWeeklyPlan(GenerateMealPlanRequest request) {
        log.info("Iniciando orquestación de plan semanal para usuario {}", request.getUserId());

        // 1. Obtener alimentos del S-03 Food Catalog
        String foods = catalogClient.getAvailableFoodsSummary();

        // 2. Construir perfil del usuario
        String profile = String.format("Objetivo: %s, Dieta: %s, Alergias: %s",
                request.getObjetivo(), request.getTipoDieta(),
                request.getAlergias() != null ? String.join(", ", request.getAlergias()) : "Ninguna");

        // 3. Llamar a S-02 Gemini API (El cerebro)
        log.info("Llamando a Gemini API...");
        String generatedJson = geminiClient.generateMealPlan(profile, foods);

        // 4. Guardar en Postgres el JSONB resultante
        MealPlan mealPlan = new MealPlan();
        mealPlan.setUserId(request.getUserId());
        mealPlan.setSemana(LocalDate.now());
        mealPlan.setPlanJson(generatedJson);

        log.info("Plan generado y guardado exitosamente.");
        return mealPlanRepository.save(mealPlan);
    }
}
