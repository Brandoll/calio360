package com.calio.tracking.controller;

import com.calio.tracking.dto.request.MealRequest;
import com.calio.tracking.dto.request.WaterRequest;
import com.calio.tracking.dto.response.DailySummaryResponse;
import com.calio.tracking.service.TrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping("/meals")
    public ResponseEntity<Map<String, Object>> registrarComida(@Valid @RequestBody MealRequest request) {
        return ResponseEntity.ok(trackingService.registrarComida(request));
    }

    @PostMapping("/water")
    public ResponseEntity<Map<String, Object>> registrarAgua(@Valid @RequestBody WaterRequest request) {
        return ResponseEntity.ok(trackingService.registrarAgua(request));
    }

    @GetMapping("/meals/summary/{userId}/{date}")
    public ResponseEntity<DailySummaryResponse> getResumenDiario(
            @PathVariable Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(trackingService.getResumenDiario(userId, date));
    }
}
