package com.calio.exercise.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rutinas_completadas")
public class RutinaCompletada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rutina_id", referencedColumnName = "id")
    private Rutina rutina;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "dia_entrenamiento", length = 20)
    private String diaEntrenamiento;

    @Column(name = "calorias_quemadas_estimadas")
    private Integer caloriasQuemadasEstimadas;

    @Column(name = "completado_en", insertable = false, updatable = false)
    private LocalDateTime completadoEn;
}
