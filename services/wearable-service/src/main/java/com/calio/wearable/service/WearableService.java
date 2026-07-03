package com.calio.wearable.service;

import com.calio.wearable.dto.request.WearableSyncRequest;
import com.calio.wearable.messaging.WearableEventPublisher;
import com.calio.wearable.model.WearableSync;
import com.calio.wearable.repository.WearableSyncRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WearableService {

    private final WearableSyncRepository repository;
    private final WearableEventPublisher eventPublisher;

    @Transactional
    public WearableSync syncActivity(WearableSyncRequest request) {
        log.info("Sincronizando datos de wearable para usuario {}", request.getUserId());

        WearableSync sync = new WearableSync();
        sync.setUserId(request.getUserId());
        sync.setSteps(request.getSteps());
        sync.setCaloriesBurned(request.getCaloriesBurned());
        sync.setDistanceKm(request.getDistanceKm());
        sync.setSyncDate(LocalDateTime.now());

        WearableSync saved = repository.save(sync);

        // Publicar evento asíncrono para que Analytics y Tracking lo procesen
        eventPublisher.publishActivityUpdated(
                saved.getUserId(),
                saved.getSteps(),
                saved.getCaloriesBurned(),
                saved.getDistanceKm().toString()
        );

        log.info("Sincronización guardada exitosamente (ID: {})", saved.getId());
        return saved;
    }

    public List<WearableSync> getHistory(Long userId) {
        return repository.findByUserIdOrderBySyncDateDesc(userId);
    }
}
