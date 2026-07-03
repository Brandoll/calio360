package com.calio.identity.repository;
import com.calio.identity.entity.BiometricRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;
public interface BiometricRecordRepository extends JpaRepository<BiometricRecord, Long> {
    List<BiometricRecord> findByUserIdOrderByRecordedAtDesc(Long userId, Pageable pageable);
}
