package sura.pruebalegoback.infraestructure;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.ChannelPool;
import reactor.rabbitmq.ChannelPoolFactory;
import reactor.rabbitmq.ChannelPoolOptions;
import reactor.rabbitmq.Sender;
import reactor.rabbitmq.SenderOptions;
import reactor.util.retry.Retry;

import java.time.Duration;

@Configuration
public class RabbitMQConfig {
    
    private static final Logger log = LoggerFactory.getLogger(RabbitMQConfig.class);
    
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
                .doOnSubscribe(s -> log.info("Creando nueva conexión a RabbitMQ"))
                .doOnError(e -> log.error("Error al crear conexión a RabbitMQ: {}", e.getMessage(), e))
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(5))
                        .maxBackoff(Duration.ofMinutes(1))
                        .filter(throwable -> throwable instanceof java.net.ConnectException)
                        .doBackoff((retrySignal -> log.warn("Reintentando conexión a RabbitMQ. Intento: {}, Error: {}",
                                retrySignal.totalRetriesInCurrentSequence(), retrySignal.failure().getMessage()))));
    }

    @Bean
    ChannelPool channelPool(Mono<Connection> connectionMono) {
        ChannelPoolOptions poolOptions = new ChannelPoolOptions();
        poolOptions.setMaxCacheSize(20); // Max 20 channels in pool
        return ChannelPoolFactory.create(connectionMono, poolOptions);
    }

    @Bean
    Sender sender(Mono<Connection> connectionMono, ChannelPool channelPool) {
        SenderOptions senderOptions = new SenderOptions()
                .connectionMono(connectionMono)
                .channelPool(channelPool);
        return new Sender(senderOptions);
    }
}
