package sura.pruebalegoback.infraestructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import sura.pruebalegoback.domain.patient.gateway.WeatherGateway;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class WeatherApiClient implements WeatherGateway {
    
    private static final Logger log = LoggerFactory.getLogger(WeatherApiClient.class);
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Value("${weather.api.base-url}")
    private String baseUrl;

    @Override
    public Mono<WeatherInfo> getWeatherByLocation(String city, String state) {
        log.info("Consultando clima para: {}, {}", city, state);
        
        // La API de weather.gov requiere coordenadas, esto es un placeholder
        // En un caso real, se usaría un servicio de geocodificación primero.
        // Para este ejemplo, simularemos una llamada a un endpoint fijo o con parámetros simples.
        String endpoint = "/points/39.7456,-104.9903"; // Ejemplo de coordenadas para Denver, CO
        
        return webClient.get()
                .uri(baseUrl + endpoint)
                .retrieve()
                .bodyToMono(String.class) // Obtener como String para mapeo manual con Jackson
                .flatMap(json -> {
                    try {
                        WeatherResponse response = objectMapper.readValue(json, WeatherResponse.class);
                        return Mono.just(toWeatherInfo(response, city, state));
                    } catch (JsonProcessingException e) {
                        log.error("Error al mapear respuesta JSON de clima: {}", e.getMessage());
                        return Mono.error(new RuntimeException("Error al procesar datos de clima", e));
                    }
                })
                .doOnNext(weather -> log.debug("Clima obtenido para {}, {}: {}", city, state, weather.condition()))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isTransientError)
                        .doBeforeRetry(retrySignal -> log.warn("Reintentando consulta de clima. Intento: {}, Error: {}",
                                retrySignal.totalRetries(), retrySignal.failure().getMessage())))
                .onErrorResume(TimeoutException.class, e -> {
                    log.error("Timeout al consultar API de clima: {}", e.getMessage());
                    return Mono.just(createDefaultWeather(city, state, "Timeout"));
                })
                .onErrorResume(RuntimeException.class, e -> {
                    log.error("Error general al consultar API de clima: {}", e.getMessage());
                    return Mono.just(createDefaultWeather(city, state, "Error"));
                })
                .onErrorReturn(createDefaultWeather(city, state, "Fallback"));
    }

    private boolean isTransientError(Throwable throwable) {
        return throwable instanceof java.net.ConnectException ||
               throwable instanceof TimeoutException;
    }

    private WeatherInfo toWeatherInfo(WeatherResponse response, String city, String state) {
        WeatherResponse.Properties props = response.getProperties();
        if (props == null) {
            return createDefaultWeather(city, state, "No properties");
        }
        
        return new WeatherInfo(
                city,
                state,
                props.getTemperature() != null ? props.getTemperature().getValue() + props.getTemperature().getUnitCode() : "N/A",
                props.getWeather() != null ? props.getWeather().getValue() : "N/A",
                props.getRelativeHumidity() != null ? props.getRelativeHumidity().getValue() + props.getRelativeHumidity().getUnitCode() : "N/A",
                props.getWindSpeed() != null ? props.getWindSpeed().getValue() + props.getWindSpeed().getUnitCode() : "N/A",
                props.getTextDescription() != null ? props.getTextDescription() : "Información no disponible"
        );
    }

    private WeatherInfo createDefaultWeather(String city, String state, String reason) {
        log.warn("Generando datos de clima por defecto para {}, {} debido a: {}", city, state, reason);
        return new WeatherInfo(
                city,
                state,
                "N/A",
                "No disponible",
                "N/A",
                "N/A",
                "Información no disponible (" + reason + ")"
        );
    }
}
