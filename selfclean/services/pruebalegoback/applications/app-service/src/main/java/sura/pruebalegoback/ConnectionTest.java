package sura.pruebalegoback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class ConnectionTest {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ConnectionTest.class, args);
        
        DatabaseClient databaseClient = context.getBean(DatabaseClient.class);
        
        // Probar conexión simple
        databaseClient.sql("SELECT 1 as test")
            .fetch()
            .first()
            .doOnNext(result -> System.out.println("✅ Conexión exitosa: " + result))
            .doOnError(error -> System.err.println("❌ Error de conexión: " + error.getMessage()))
            .subscribe();
        
        // Mantener la aplicación corriendo un poco
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        context.close();
    }
}
