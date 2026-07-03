package com.calio.foodcatalog.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
@Entity @Table(name = "alimentos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Alimento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "codigo_ins", nullable = false, unique = true, length = 10)
    private String codigoIns;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
    @Column(nullable = false, length = 150)
    private String nombre;
    @Column(name = "porcion_ref", length = 20)
    private String porcionRef;
    @Column(name = "energia_kcal", precision = 6, scale = 2)
    private BigDecimal energiaKcal;
    @Column(name = "proteinas_g", precision = 6, scale = 2)
    private BigDecimal proteinasG;
    @Column(name = "grasa_total_g", precision = 6, scale = 2)
    private BigDecimal grasaTotalG;
    @Column(name = "carbohidratos_totales_g", precision = 6, scale = 2)
    private BigDecimal carbohidratosTotalesG;
    @Column(name = "fibra_dietaria_g", precision = 6, scale = 2)
    private BigDecimal fibraDietariaG;
    @Column(name = "calcio_mg", precision = 6, scale = 2)
    private BigDecimal calcioMg;
    @Column(name = "hierro_mg", precision = 6, scale = 2)
    private BigDecimal hierroMg;
    @Column(name = "vitamina_c_mg", precision = 6, scale = 2)
    private BigDecimal vitaminaCMg;
}
