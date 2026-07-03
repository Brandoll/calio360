package com.calio.wearable.repository;

import com.calio.wearable.model.WearableSync;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WearableSyncRepository extends JpaRepository<WearableSync, Long> {
    List<WearableSync> findByUserIdOrderBySyncDateDesc(Long userId);
}
