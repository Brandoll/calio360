package com.calio.tracking.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrackingEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private static final String EXCHANGE = "calio.events";

    public void publishComidaRegistrada(Long userId, String fecha, int calorias) {
        publishEvent("comida.registrada", Map.of(
                "userId", userId,
                "fecha", fecha,
                "caloriasConsumidas", calorias
        ));
    }

    public void publishAguaActualizada(Long userId, String fecha, int vasosTotales) {
        publishEvent("agua.actualizada", Map.of(
                "userId", userId,
                "fecha", fecha,
                "vasosAgua", vasosTotales
        ));
    }

    private void publishEvent(String routingKey, Map<String, Object> payload) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(payload);
            rabbitTemplate.convertAndSend(EXCHANGE, routingKey, jsonMessage);
            log.info("Evento publicado en [{}] con routingKey [{}]: {}", EXCHANGE, routingKey, jsonMessage);
        } catch (Exception e) {
            log.error("Error publicando evento {} en RabbitMQ", routingKey, e);
        }
    }
}
