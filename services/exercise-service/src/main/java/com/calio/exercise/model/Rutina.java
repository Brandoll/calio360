package com.calio.exercise.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rutinas")
public class Rutina {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate semana;

    @Column(nullable = false, length = 50)
    private String objetivo;

    @Column(name = "dias_por_semana", nullable = false)
    private Integer diasPorSemana;

    @Type(JsonType.class)
    @Column(name = "equipo_disponible", columnDefinition = "jsonb")
    private String equipoDisponible;

    @Type(JsonType.class)
    @Column(name = "rutina_json", nullable = false, columnDefinition = "jsonb")
    private String rutinaJson;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
