package com.calio.wearable.controller;

import com.calio.wearable.dto.request.WearableSyncRequest;
import com.calio.wearable.model.WearableSync;
import com.calio.wearable.service.WearableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wearables")
@RequiredArgsConstructor
public class WearableController {

    private final WearableService wearableService;

    @PostMapping("/sync")
    public ResponseEntity<WearableSync> syncActivity(@Valid @RequestBody WearableSyncRequest request) {
        return ResponseEntity.ok(wearableService.syncActivity(request));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<WearableSync>> getHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(wearableService.getHistory(userId));
    }
}
