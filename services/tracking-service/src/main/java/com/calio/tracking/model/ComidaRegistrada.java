package com.calio.tracking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "comidas_registradas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComidaRegistrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "alimento_id", nullable = false)
    private Long alimentoId;

    @Column(name = "porcion_gramos", nullable = false)
    private Integer porcionGramos;

    @Column(name = "momento", length = 20)
    private String momento; // desayuno, almuerzo, cena, snack

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private Integer calorias;

    @Column(precision = 5, scale = 2)
    private BigDecimal proteinas;

    @Column(precision = 5, scale = 2)
    private BigDecimal grasas;

    @Column(precision = 5, scale = 2)
    private BigDecimal carbohidratos;
}
