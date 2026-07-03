package com.calio.foodcatalog.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "categorias")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Categoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "codigo_letra", nullable = false, unique = true, length = 2)
    private String codigoLetra;
    @Column(nullable = false, length = 100)
    private String nombre;
}
