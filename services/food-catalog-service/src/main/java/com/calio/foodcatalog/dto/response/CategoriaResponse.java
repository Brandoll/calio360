package com.calio.foodcatalog.dto.response;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CategoriaResponse {
    private Integer id;
    private String codigoLetra;
    private String nombre;
    private Long totalAlimentos;
}
