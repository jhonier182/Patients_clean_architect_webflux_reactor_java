package sura.pruebalegoback;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class ConnectionTest {
    
    @Autowired
    private ConnectionFactory connectionFactory;

    public static void main(String[] args) {
        SpringApplication.run(ConnectionTest.class, args);
    }

    @Bean
    public CommandLineRunner run() {
        return args -> {
            Mono.from(connectionFactory.create())
                .flatMapMany(connection ->
                    Mono.from(connection.createStatement("SELECT 1 as test").execute())
                        .flatMapMany(result -> result.map((row, rowMetadata) -> row.get("test", Integer.class)))
                        .doFinally(signalType -> Mono.from(connection.close()).subscribe())
                )
                .doOnNext(result -> System.out.println("✅ Conexión exitosa: {test=" + result + "}"))
                .doOnError(error -> System.err.println("❌ Error de conexión: " + error.getMessage()))
                .subscribe();
        };
    }
}
