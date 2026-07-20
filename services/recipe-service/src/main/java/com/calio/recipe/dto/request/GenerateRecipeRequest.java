package com.calio.recipe.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class GenerateRecipeRequest {
    @NotNull
    private Long userId;
    
    @NotEmpty
    private List<String> ingredientes;
    
    private String tipoReceta; // Opcional: "desayuno", "almuerzo", "cena", "snack"
}
