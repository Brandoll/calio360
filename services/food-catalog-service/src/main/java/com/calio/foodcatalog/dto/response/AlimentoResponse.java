package com.calio.foodcatalog.dto.response;
import lombok.*;
import java.math.BigDecimal;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AlimentoResponse {
    private Integer id;
    private String codigoIns;
    private String nombre;
    private String porcionRef;
    private String categoria;
    private BigDecimal energiaKcal;
    private BigDecimal proteinasG;
    private BigDecimal grasaTotalG;
    private BigDecimal carbohidratosTotalesG;
    private BigDecimal fibraDietariaG;
    private BigDecimal calcioMg;
    private BigDecimal hierroMg;
    private BigDecimal vitaminaCMg;
    private Boolean esFavorito;
}
