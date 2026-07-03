package com.calio.exercise.repository;

import com.calio.exercise.model.RutinaCompletada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RutinaCompletadaRepository extends JpaRepository<RutinaCompletada, Long> {
}
