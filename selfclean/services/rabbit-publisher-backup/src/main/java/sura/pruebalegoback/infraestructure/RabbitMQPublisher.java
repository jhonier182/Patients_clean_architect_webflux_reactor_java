package sura.pruebalegoback.infraestructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;
import reactor.util.retry.Retry;
import sura.pruebalegoback.domain.common.Event;
import sura.pruebalegoback.domain.common.EventsGateway;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RabbitMQPublisher implements EventsGateway {
    
    private static final Logger log = LoggerFactory.getLogger(RabbitMQPublisher.class);
    
    private final Sender sender;
    private final ObjectMapper objectMapper;
    private final Mono<Connection> connectionMono;

    @Override
    public Mono<Void> emit(Event event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(event))
                .map(body -> new OutboundMessage("", event.name(), 
                        new AMQP.BasicProperties.Builder().build(), body))
                .flatMap(sender::send)
                .doOnSuccess(v -> log.info("Evento {} publicado exitosamente para: {}", event.name(), event))
                .doOnError(e -> log.error("Error al publicar evento {}: {}", event.name(), e.getMessage(), e))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(this::isTransientError)
                        .doBackoff((retrySignal -> log.warn("Reintentando publicación de evento. Intento: {}, Error: {}",
                                retrySignal.totalRetriesInCurrentSequence(), retrySignal.failure().getMessage()))))
                .onErrorResume(e -> {
                    log.error("Fallo definitivo al publicar evento {} después de reintentos: {}", event.name(), e.getMessage());
                    return Mono.error(new RuntimeException("Fallo al publicar evento en RabbitMQ", e));
                });
    }

    private boolean isTransientError(Throwable throwable) {
        return throwable instanceof java.net.ConnectException ||
               throwable instanceof java.io.IOException; // Consider IOException as transient for RabbitMQ
    }
}
