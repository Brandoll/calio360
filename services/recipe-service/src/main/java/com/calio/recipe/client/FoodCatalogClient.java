package com.calio.recipe.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class FoodCatalogClient {

    private final RestTemplate restTemplate;
    private final String catalogServiceUrl;

    public FoodCatalogClient(RestTemplate restTemplate, @Value("${calio.catalog.url}") String catalogServiceUrl) {
        this.restTemplate = restTemplate;
        this.catalogServiceUrl = catalogServiceUrl;
    }

    public String getAvailableFoodsSummary() {
        try {
            // Mock de alimentos nativos disponibles para inyectar a la IA
            // En prod, haríamos un GET a catalogServiceUrl + "/catalogo/alimentos"
            return "Alimentos disponibles: Quinua, Arepa de Choclo, Papa, Yuca, Camote, Lulo, Pollo, Res, Tilapia, Frijoles, Lentejas, Aguacate, Plátano maduro, Ceviche de pescado.";
        } catch (Exception e) {
            log.warn("No se pudo conectar al S-03 Food Catalog. Usando lista default.");
            return "Quinua, Papa, Yuca, Pollo, Lentejas, Aguacate.";
        }
    }
}
