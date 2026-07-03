package com.calio.recipe.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class GenerateMealPlanRequest {
    @NotNull
    private Long userId;
    
    // Estos datos normalmente los extraeriamos del IdentityService, pero para
    // flexibilizar el request desde el móvil, los recibimos aquí
    private String objetivo; // "perder peso", "ganar musculo"
    private String tipoDieta; // "vegana", "tradicional", "keto"
    private List<String> alergias;
}
