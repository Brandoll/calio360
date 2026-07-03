package com.calio.exercise.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RutinaCompletadaPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private static final String EXCHANGE = "calio.events";
    private static final String ROUTING_KEY = "rutina.completada";

    public void publishRutinaCompletada(Long userId, String fecha, int caloriasQuemadas) {
        try {
            Map<String, Object> payload = Map.of(
                "userId", userId,
                "fecha", fecha,
                "caloriasQuemadas", caloriasQuemadas
            );
            String jsonMessage = objectMapper.writeValueAsString(payload);
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, jsonMessage);
            log.info("Evento publicado: {}", jsonMessage);
        } catch (Exception e) {
            log.error("Error al publicar evento de rutina completada", e);
        }
    }
}
