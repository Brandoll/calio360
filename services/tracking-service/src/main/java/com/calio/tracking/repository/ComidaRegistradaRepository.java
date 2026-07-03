package com.calio.tracking.repository;

import com.calio.tracking.model.ComidaRegistrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ComidaRegistradaRepository extends JpaRepository<ComidaRegistrada, Long> {
    List<ComidaRegistrada> findByUserIdAndFecha(Long userId, LocalDate fecha);
}
