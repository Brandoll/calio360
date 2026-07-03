package com.calio.exercise.repository;

import com.calio.exercise.model.Ejercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Ejercicio, Long> {

    @Query("SELECT e FROM Ejercicio e WHERE " +
           "(:grupoMuscular IS NULL OR e.grupoMuscular = :grupoMuscular) AND " +
           "(:equipo IS NULL OR e.equipo = :equipo) AND " +
           "(:dificultad IS NULL OR e.dificultad = :dificultad) AND " +
           "e.activo = true")
    List<Ejercicio> findEjerciciosFiltrados(
            @Param("grupoMuscular") String grupoMuscular,
            @Param("equipo") String equipo,
            @Param("dificultad") String dificultad
    );

    @Query("SELECT e FROM Ejercicio e WHERE e.grupoMuscular = :grupoMuscular AND e.equipo IN :equipos AND e.dificultad = :dificultad AND e.activo = true")
    List<Ejercicio> findByGrupoAndEquiposAndDificultad(
            @Param("grupoMuscular") String grupoMuscular,
            @Param("equipos") List<String> equipos,
            @Param("dificultad") String dificultad
    );
}
