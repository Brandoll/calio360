package com.calio.exercise.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ejercicios")
public class Ejercicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "grupo_muscular", nullable = false, length = 30)
    private String grupoMuscular;

    @Column(nullable = false, length = 50)
    private String equipo;

    @Column(nullable = false, length = 20)
    private String dificultad;

    @Column(name = "calorias_por_minuto", precision = 5, scale = 2)
    private BigDecimal caloriasPorMinuto;

    @Column(name = "gif_url", nullable = false, length = 255)
    private String gifUrl;

    @Column(columnDefinition = "TEXT")
    private String instrucciones;

    @Column(name = "series_recomendadas")
    private Integer seriesRecomendadas;

    @Column(name = "repeticiones_recomendadas", length = 20)
    private String repeticionesRecomendadas;

    private Boolean activo = true;
}
