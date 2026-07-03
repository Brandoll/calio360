package com.calio.exercise.repository;

import com.calio.exercise.model.Rutina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RutinaRepository extends JpaRepository<Rutina, Long> {
    List<Rutina> findByUserIdOrderByCreatedAtDesc(Long userId);
}
