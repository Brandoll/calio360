package com.calio.wearable.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WearableEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private static final String EXCHANGE = "calio.events";

    public void publishActivityUpdated(Long userId, int steps, int caloriesBurned, String distanceKm) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(Map.of(
                    "userId", userId,
                    "steps", steps,
                    "caloriasQuemadas", caloriesBurned,
                    "distanciaKm", distanceKm,
                    "fecha", java.time.LocalDate.now().toString()
            ));
            rabbitTemplate.convertAndSend(EXCHANGE, "activity.updated", jsonMessage);
            log.info("Evento activity.updated publicado para usuario {}", userId);
        } catch (Exception e) {
            log.error("Error publicando evento activity.updated", e);
        }
    }
}
