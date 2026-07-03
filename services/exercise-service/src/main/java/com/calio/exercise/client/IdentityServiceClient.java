package com.calio.exercise.client;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class IdentityServiceClient {

    // En un entorno real se usaría RestTemplate o WebClient apuntando al Gateway o al identity-service
    // Por simplicidad para el prototipo, devolvemos valores mock
    public Map<String, Object> getProfile(Long userId) {
        return Map.of(
            "objetivo", "ganar musculo",
            "nivelActividad", "moderado"
        );
    }
}
