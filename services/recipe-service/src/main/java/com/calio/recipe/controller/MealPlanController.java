package com.calio.recipe.controller;

import com.calio.recipe.dto.request.GenerateMealPlanRequest;
import com.calio.recipe.model.MealPlan;
import com.calio.recipe.repository.MealPlanRepository;
import com.calio.recipe.service.MealPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/meal-plan")
@RequiredArgsConstructor
public class MealPlanController {

    private final MealPlanService mealPlanService;
    private final MealPlanRepository repository;

    @PostMapping("/generate")
    public ResponseEntity<MealPlan> generatePlan(@Valid @RequestBody GenerateMealPlanRequest request) {
        return ResponseEntity.ok(mealPlanService.generateWeeklyPlan(request));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<MealPlan>> getPlanHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(repository.findByUserIdOrderBySemanaDesc(userId));
    }
}
