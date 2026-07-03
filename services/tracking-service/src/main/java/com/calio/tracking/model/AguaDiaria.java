package com.calio.tracking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "agua_diaria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(AguaDiariaId.class)
public class AguaDiaria {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "vasos", nullable = false)
    private Integer vasos = 0;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class AguaDiariaId implements Serializable {
    private Long userId;
    private LocalDate fecha;
}
