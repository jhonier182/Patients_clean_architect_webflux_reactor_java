package sura.pruebalegoback.subevents;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.ReceiverOptions;
import reactor.util.retry.Retry;


import java.time.Duration;

@Configuration
public class RabbitMQReceiverConfig {
    
    private static final Logger log = LoggerFactory.getLogger(RabbitMQReceiverConfig.class);
    
    @Value("${spring.rabbitmq.host}")
    private String host;
    
    @Value("${spring.rabbitmq.port}")
    private int port;
    
    @Value("${spring.rabbitmq.username:guest}")
    private String username;
    
    @Value("${spring.rabbitmq.password:guest}")
    private String password;

    @Bean
    Mono<Connection> connectionMono() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.useNio();
        
        return Mono.fromCallable(connectionFactory::newConnection)
                .cache(Duration.ofMinutes(10)) // Cache connection for 10 minutes
                .doOnSubscribe(s -> log.info("Creando nueva conexión a RabbitMQ para receiver"))
                .doOnError(e -> log.error("Error al crear conexión a RabbitMQ para receiver: {}", e.getMessage(), e))
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(5))
                        .maxBackoff(Duration.ofMinutes(1))
                        .filter(throwable -> throwable instanceof java.net.ConnectException)
                        .doBeforeRetry(retrySignal -> log.warn("Reintentando conexión a RabbitMQ para receiver. Intento: {}, Error: {}",
                                retrySignal.totalRetries(), retrySignal.failure().getMessage())));
    }

    @Bean
    Receiver receiver(Mono<Connection> connectionMono) {
        ReceiverOptions receiverOptions = new ReceiverOptions()
                .connectionMono(connectionMono);
        return new Receiver(receiverOptions);
    }
}
