package com.calio.identity.messaging;

import com.calio.identity.config.RabbitMQConfig;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.time.OffsetDateTime;
import java.util.Map;

@Component @RequiredArgsConstructor @Slf4j
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserRegistered(Long userId, String email, String firstName) {
        var event = Map.of(
            "eventType", "USER_REGISTERED",
            "userId", userId,
            "email", email,
            "firstName", firstName,
            "timestamp", OffsetDateTime.now().toString()
        );
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.KEY_USER_REGISTERED, event);
            log.info("Evento USER_REGISTERED publicado para userId={}", userId);
        } catch (Exception e) {
            log.warn("No se pudo publicar evento RabbitMQ (servicio continúa): {}", e.getMessage());
        }
    }

    public void publishProfileUpdated(Long userId) {
        var event = Map.of(
            "eventType", "USER_PROFILE_UPDATED",
            "userId", userId,
            "timestamp", OffsetDateTime.now().toString()
        );
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.KEY_PROFILE_UPDATED, event);
        } catch (Exception e) {
            log.warn("No se pudo publicar evento RabbitMQ: {}", e.getMessage());
        }
    }
}
