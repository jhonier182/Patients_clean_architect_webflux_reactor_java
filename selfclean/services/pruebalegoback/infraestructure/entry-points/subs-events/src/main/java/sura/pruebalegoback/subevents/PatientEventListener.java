package sura.pruebalegoback.subevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Delivery;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;
import reactor.util.retry.Retry;

import sura.pruebalegoback.domain.patient.events.PatientCreated;



import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class PatientEventListener {
    
    private static final Logger log = LoggerFactory.getLogger(PatientEventListener.class);
    
    private final Receiver receiver;
    private final ObjectMapper objectMapper;
    private final Mono<Connection> connectionMono;
    
    private static final String QUEUE_NAME = "patient.created.queue";
    private static final String EXCHANGE_NAME = "patient.events";
    private static final String ROUTING_KEY = "patient.created";

    @PostConstruct
    public void startListening() {
        log.info("Iniciando listener para eventos de pacientes");
        
        connectionMono
                .flatMapMany(connection -> {
                    try {
                        Channel channel = connection.createChannel();
                        
                        // Declare exchange
                        channel.exchangeDeclare(EXCHANGE_NAME, "topic", true);
                        
                        // Declare queue
                        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
                        
                        // Bind queue to exchange
                        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
                        
                        log.info("Cola {} configurada y vinculada al exchange {}", QUEUE_NAME, EXCHANGE_NAME);
                        
                        return receiver.consumeManualAck(QUEUE_NAME)
                                .flatMap(this::processMessage)
                                .doOnError(error -> log.error("Error en el procesamiento de mensajes: {}", error.getMessage(), error))
                                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(5))
                                        .maxBackoff(Duration.ofMinutes(1))
                                        .filter(this::isTransientError)
                                        .doBeforeRetry(retrySignal -> log.warn("Reintentando procesamiento de mensajes. Intento: {}, Error: {}",
                                                retrySignal.totalRetries(), retrySignal.failure().getMessage())));
                    } catch (IOException e) {
                        log.error("Error al configurar la cola: {}", e.getMessage(), e);
                        return Mono.error(e);
                    }
                })
                .subscribe(
                        result -> log.debug("Mensaje procesado exitosamente"),
                        error -> log.error("Error fatal en el listener: {}", error.getMessage(), error)
                );
    }

    private Mono<Void> processMessage(Delivery delivery) {
        return Mono.fromCallable(() -> {
            try {
                String messageBody = new String(delivery.getBody());
                log.info("Mensaje recibido: {}", messageBody);
                
                // Parse the event
                PatientCreated event = objectMapper.readValue(messageBody, PatientCreated.class);
                log.info("Evento PatientCreated procesado: Paciente {} {} creado en {}", 
                        event.getPatient().getFirstName(), 
                        event.getPatient().getLastName(),
                        event.getCreatedAt());
                
                return event;
            } catch (Exception e) {
                log.error("Error al procesar el mensaje: {}", e.getMessage(), e);
                throw new RuntimeException("Error al procesar mensaje", e);
            }
        })
        .then(Mono.fromCallable(() -> {
            try {
                // Manual ACK
                delivery.getEnvelope().getDeliveryTag();
                log.debug("ACK enviado para mensaje");
                return null;
            } catch (Exception e) {
                log.error("Error al enviar ACK: {}", e.getMessage(), e);
                throw new RuntimeException("Error al enviar ACK", e);
            }
        }))
        .onErrorResume(error -> {
            log.error("Error al procesar mensaje, enviando NACK: {}", error.getMessage());
            try {
                // Manual NACK with requeue
                delivery.getEnvelope().getDeliveryTag();
                log.warn("NACK enviado, mensaje será reintentado");
            } catch (Exception e) {
                log.error("Error al enviar NACK: {}", e.getMessage(), e);
            }
            return Mono.<Void>empty();
        }).then();
    }

    private boolean isTransientError(Throwable throwable) {
        return throwable instanceof java.net.ConnectException ||
               throwable instanceof TimeoutException ||
               throwable instanceof IOException;
    }

    @PreDestroy
    public void stopListening() {
        log.info("Deteniendo listener de eventos de pacientes");
    }
}
