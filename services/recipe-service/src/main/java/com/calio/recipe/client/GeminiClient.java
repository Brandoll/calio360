package com.calio.recipe.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GeminiClient {

    private final RestTemplate restTemplate;
    private final String geminiApiUrl;
    private final String geminiApiKey;

    public GeminiClient(RestTemplate restTemplate,
                        @Value("${calio.gemini.url}") String geminiApiUrl,
                        @Value("${calio.gemini.api-key}") String geminiApiKey) {
        this.restTemplate = restTemplate;
        this.geminiApiUrl = geminiApiUrl;
        this.geminiApiKey = geminiApiKey;
    }

    public String generateMealPlan(String userProfile, String foodsAvailable) {
        String url = geminiApiUrl + "?key=" + geminiApiKey;

        String systemInstruction = "Eres un nutricionista experto. Crea un plan semanal detallado usando ingredientes nativos de Latinoamérica. Devuelve un JSON estructurado.";
        String prompt = "Perfil del usuario: " + userProfile + ". Alimentos locales sugeridos: " + foodsAvailable + 
                ". Genera un plan semanal y lista de compras.";

        Map<String, Object> requestBody = Map.of(
            "system_instruction", Map.of("parts", List.of(Map.of("text", systemInstruction))),
            "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
            "generationConfig", Map.of(
                "response_mime_type", "application/json",
                "temperature", 0.3
            )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            
            // Extraer el texto generado del JSON de respuesta de Google API
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            
            return (String) parts.get(0).get("text");

        } catch (Exception e) {
            log.error("Error comunicándose con Gemini API", e);
            throw new RuntimeException("No se pudo generar el plan con la IA.");
        }
    }
}
