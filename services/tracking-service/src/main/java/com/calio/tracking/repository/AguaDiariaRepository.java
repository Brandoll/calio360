package com.calio.tracking.repository;

import com.calio.tracking.model.AguaDiaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AguaDiariaRepository extends JpaRepository<AguaDiaria, Object> {
    Optional<AguaDiaria> findByUserIdAndFecha(Long userId, LocalDate fecha);
}
